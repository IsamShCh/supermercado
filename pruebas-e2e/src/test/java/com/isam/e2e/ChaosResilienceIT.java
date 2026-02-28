package com.isam.e2e;

import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.catalogo.CrearCategoriaRequest;
import com.isam.grpc.catalogo.CrearProductoRequest;
import com.isam.grpc.common.UnidadMedida;
import com.isam.grpc.usuarios.IniciarSesionRequest;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.grpc.ventas.AnadirProductoTicketRequest;
import com.isam.grpc.ventas.CrearNuevoTicketRequest;
import com.isam.grpc.ventas.VentasServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ChaosResilienceIT {

    private ManagedChannel usuariosChannel;
    private ManagedChannel ventasChannel;
    private ManagedChannel catalogoChannel;
    private VentasServiceGrpc.VentasServiceBlockingStub ventasStub;
    private CatalogoServiceGrpc.CatalogoServiceBlockingStub catalogoStub;
    private String jwtToken;

    @BeforeEach
    public void setup() {
        usuariosChannel = ManagedChannelBuilder.forAddress("localhost", 9093).usePlaintext().build();
        UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub = UsuarioServiceGrpc.newBlockingStub(usuariosChannel);

        try {
            var loginResponse = usuariosStub.iniciarSesion(IniciarSesionRequest.newBuilder()
                    .setNombreUsuario("admin")
                    .setPassword("admin123")
                    .build());
            jwtToken = loginResponse.getTokenJwt();
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Error al loguearse para el test de resiliencia", e);
        }

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);

        ventasChannel = ManagedChannelBuilder.forAddress("localhost", 9092).usePlaintext().build();
        ventasStub = VentasServiceGrpc.newBlockingStub(ventasChannel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

        catalogoChannel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        catalogoStub = CatalogoServiceGrpc.newBlockingStub(catalogoChannel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @AfterEach
    public void teardown() {
        unpauseCatalogoDB();

        if (usuariosChannel != null) usuariosChannel.shutdown();
        if (ventasChannel != null) ventasChannel.shutdown();
        if (catalogoChannel != null) catalogoChannel.shutdown();
    }

    private void pauseCatalogoDB() {
        try {
            System.out.println("--- Pausando catalogo-db vía Docker CMD ---");
            Process process = new ProcessBuilder("docker", "pause", "catalogo-db").start();
            process.waitFor();
            Thread.sleep(2000); 
        } catch (Exception e) {
            System.err.println("No se pudo pausar catalogo-db via Docker. " + e.getMessage());
        }
    }

    private void unpauseCatalogoDB() {
        try {
            System.out.println("--- Reanudando catalogo-db ---");
            Process process = new ProcessBuilder("docker", "unpause", "catalogo-db").start();
            process.waitFor();
            Thread.sleep(2000);
        } catch (Exception e) {
            System.err.println("No se pudo reanudar catalogo-db via Docker. " + e.getMessage());
        }
    }

    @Test
    public void testVentasSurvivesCatalogoDbOutageUsingCQRS() throws InterruptedException {
        System.out.println("Iniciando prueba de Caos con Setup dinámico...");

        //Preparar un producto existente en Catálogo
        long idCategoria = 1L;
        try {
            var categoriaResponse = catalogoStub.crearCategoria(CrearCategoriaRequest.newBuilder()
                    .setNombreCategoria("Categoría Búnker " + UUID.randomUUID().toString().substring(0, 5))
                    .setDescripcion("Generada por test de caos")
                    .build());
            idCategoria = categoriaResponse.getCategoria().getIdCategoria();
        } catch (StatusRuntimeException e) {
            System.err.println("Aviso: No se pudo crear la categoría: " + e.getMessage());
        }

        String eanCaos = String.valueOf(System.currentTimeMillis()).substring(0, 13);
        String skuCaos = "SKU-BUNKER-" + UUID.randomUUID().toString().substring(0, 5);

        catalogoStub.crearProducto(CrearProductoRequest.newBuilder()
                .setSku(skuCaos)
                .setEan(eanCaos)
                .setNombre("Lata de Supervivencia")
                .setPrecioVenta("15.50")
                .setIdCategoria(idCategoria)
                .setUnidadMedida(UnidadMedida.UNIDAD)
                .build());

        System.out.println("Producto " + skuCaos + " creado en el Maestro. Esperando 3 segundos para propagación Kafka...");
        Thread.sleep(3000); // Tiempo para que Ventas capture el evento "catalogo.producto.eventos"

        //Creamos un ticket temporal
        var ticketResponse = ventasStub.crearNuevoTicket(CrearNuevoTicketRequest.newBuilder().build());
        String ticketId = ticketResponse.getIdTicketTemporal();
        assertThat(ticketId).isNotNull().isNotEmpty();

        //Matamos a la DB de Catálogo
        pauseCatalogoDB();

        //Intentamos añadir em mismo producto a Ventas
        try {
            var addItemResponse = ventasStub.anadirProductoTicket(AnadirProductoTicketRequest.newBuilder()
                    .setIdTicketTemporal(ticketId)
                    .setCodigoBarras(eanCaos)
                    .build());
            
            System.out.println("El producto fue añadido. Ventas usó su cache local y esquivó a Catálogo.");
            assertThat(addItemResponse.getSku()).isEqualTo(skuCaos);
            
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode().name().equals("UNAVAILABLE") || e.getStatus().getCode().name().equals("DEADLINE_EXCEEDED")) {
                fail("FALSO FALLBACK: A pesar de que el producto " + skuCaos + " debía estar en la Caché de Ventas, " +
                     "el servicio intentó llamar a Catálogo de manera síncrona y falló debido al caos inyectado.");
            } else {
                fail("ERROR DE DOMINIO INESPERADO: Ventas devolvió un error distinto en pleno Caos: " + e.getStatus());
            }
        }
    }
}
