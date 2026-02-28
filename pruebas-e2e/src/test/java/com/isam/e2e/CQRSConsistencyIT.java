package com.isam.e2e;

import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.catalogo.CrearCategoriaRequest;
import com.isam.grpc.catalogo.CrearProductoRequest;
import com.isam.grpc.catalogo.DatosActualizar;
import com.isam.grpc.catalogo.ModificarProductoRequest;
import com.isam.grpc.common.UnidadMedida;
import com.isam.grpc.inventario.ConsultarInventarioRequest;
import com.isam.grpc.inventario.CrearInventarioRequest;
import com.isam.grpc.inventario.InventarioServiceGrpc;
import com.isam.grpc.usuarios.IniciarSesionRequest;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CQRSConsistencyIT {

    private static ManagedChannel usuariosChannel;
    private static ManagedChannel catalogoChannel;
    private static ManagedChannel inventarioChannel;

    private static CatalogoServiceGrpc.CatalogoServiceBlockingStub catalogoStub;
    private static InventarioServiceGrpc.InventarioServiceBlockingStub inventarioStub;

    @BeforeAll
    public static void setup() {
        usuariosChannel = ManagedChannelBuilder.forAddress("localhost", 9093).usePlaintext().build();
        UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub = UsuarioServiceGrpc.newBlockingStub(usuariosChannel);

        String jwtToken;
        try {
            var loginResponse = usuariosStub.iniciarSesion(IniciarSesionRequest.newBuilder()
                    .setNombreUsuario("admin")
                    .setPassword("admin123")
                    .build());
            jwtToken = loginResponse.getTokenJwt();
        } catch (StatusRuntimeException e) {
            System.err.println("Asegúrate de que usuarios-db y servicio-usuarios estén activos y con datos de ADMIN.");
            throw new RuntimeException("Error al loguearse para el test de consistencia", e);
        }

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);

        // Cliente para el Catálogo
        catalogoChannel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        catalogoStub = CatalogoServiceGrpc.newBlockingStub(catalogoChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

        // Cliente para Inventario
        inventarioChannel = ManagedChannelBuilder.forAddress("localhost", 9091).usePlaintext().build();
        inventarioStub = InventarioServiceGrpc.newBlockingStub(inventarioChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @AfterAll
    public static void teardown() throws InterruptedException {
        if (usuariosChannel != null) usuariosChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        if (catalogoChannel != null) catalogoChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        if (inventarioChannel != null) inventarioChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    public void testCQRSPropagatesCatologoChangesToInventario() throws InterruptedException {
        System.out.println("\n[Cache Test] Preparando datos iniciales...");

        long idCategoria = 1L; // Fallback
        try {
            var categoriaResponse = catalogoStub.crearCategoria(CrearCategoriaRequest.newBuilder()
                    .setNombreCategoria("Categoría cache " + UUID.randomUUID().toString().substring(0, 5))
                    .setDescripcion("Generada por test de consistencia")
                    .build());
            idCategoria = categoriaResponse.getCategoria().getIdCategoria();
        } catch (StatusRuntimeException e) {
            System.err.println("Aviso: No se pudo crear la categoría: " + e.getMessage());
        }

        String skuPrueba = "SKU-EVENTUAL-" + UUID.randomUUID().toString().substring(0, 5);
        String eanInicial = String.valueOf(System.currentTimeMillis()).substring(0, 13);
        String nombreInicial = "Nombre Base";

        catalogoStub.crearProducto(CrearProductoRequest.newBuilder()
                .setSku(skuPrueba)
                .setEan(eanInicial)
                .setNombre(nombreInicial)
                .setPrecioVenta("9.99")
                .setIdCategoria(idCategoria)
                .setUnidadMedida(UnidadMedida.UNIDAD)
                .build());

        // Esperar un momento a que el producto inicial se propague
        Thread.sleep(2000); 

        System.out.println("[Cache Test] Abriendo un expediente de Inventario Local para el SKU " + skuPrueba);
        inventarioStub.crearInventario(CrearInventarioRequest.newBuilder()
                .setSku(skuPrueba)
                .setEan(eanInicial)
                .setUnidadMedida(UnidadMedida.UNIDAD)
                .build());

        // Generamos un nombre aleatorio
        String nuevoNombre = "Nombre Modificado " + UUID.randomUUID().toString().substring(0, 6);
        System.out.println("[Cache Test] Modificando el producto " + skuPrueba + " en el Maestro (Catálogo) a: " + nuevoNombre);

        // Acción 1: Mutación en el servicio maestro
        catalogoStub.modificarProducto(ModificarProductoRequest.newBuilder()
                .setSku(skuPrueba)
                .setDatosActualizar(DatosActualizar.newBuilder()
                        .setNombre(nuevoNombre)
                        .build())
                .build());

        // Bucle de polling Vanilla Java para capturar el instante en que Kafka asíncronamete actualiza a Inventario
        long startTime = System.currentTimeMillis();
        boolean eventualConsistencyAchieved = false;

        System.out.println("[Cache Test] Cambio en maestro exitoso. Iniciando bucle de polling sobre Inventario (Max 10s)...");

        while (System.currentTimeMillis() - startTime <= 10000) {
            try {
                // Lectura en la cache de inventario
                var response = inventarioStub.consultarInventario(ConsultarInventarioRequest.newBuilder()
                        .setSku(skuPrueba)
                        .build());
                        
                String nombreEnInventario = response.getDetallesInventario().getNombreProducto();
                
                if (nuevoNombre.equals(nombreEnInventario)) {
                    eventualConsistencyAchieved = true;
                    long timeTaken = System.currentTimeMillis() - startTime;
                    System.out.println("[Cache Test] Consistencia Eventual consolidada en Inventario en " + timeTaken + " milisegundos.");
                    break;
                }
            } catch (StatusRuntimeException ignored) {
                // Puede fallar si hay problemas de transaccionalidad
            }

            try {
                Thread.sleep(500); // Backoff simple
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertThat(eventualConsistencyAchieved)
                .as("La red Kafka no propagó el Evento de Dominio a tiempo. La proyección en Inventario sigue estando desincronizada.")
                .isTrue();
    }
}
