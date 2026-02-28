package com.isam.e2e;

import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.catalogo.CrearCategoriaRequest;
import com.isam.grpc.catalogo.CrearProductoRequest;
import com.isam.grpc.common.UnidadMedida;
import com.isam.grpc.usuarios.IniciarSesionRequest;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.grpc.ventas.AnadirProductoTicketRequest;
import com.isam.grpc.ventas.CerrarTicketRequest;
import com.isam.grpc.ventas.CrearNuevoTicketRequest;
import com.isam.grpc.ventas.MetodoPago;
import com.isam.grpc.ventas.ProcesarPagoRequest;
import com.isam.grpc.ventas.VentasServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class MaxThroughputIT {

    private static ManagedChannel usuariosChannel;
    private static ManagedChannel catalogoChannel;
    private static ManagedChannel ventasChannel;
    private static VentasServiceGrpc.VentasServiceBlockingStub ventasStub;
    private static CatalogoServiceGrpc.CatalogoServiceBlockingStub catalogoStub;
    private static String jwtToken;
    private static String skuGlobal;
    private static String eanGlobal;

    @BeforeAll
    public static void setup() throws InterruptedException {
        usuariosChannel = ManagedChannelBuilder.forAddress("localhost", 9093).usePlaintext().build();
        UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub = UsuarioServiceGrpc.newBlockingStub(usuariosChannel);

        try {
            var loginResponse = usuariosStub.iniciarSesion(IniciarSesionRequest.newBuilder()
                    .setNombreUsuario("admin")
                    .setPassword("admin123")
                    .build());
            jwtToken = loginResponse.getTokenJwt();
            System.out.println("Token obtenido exitosamente para Max Throughput Testing.");
        } catch (StatusRuntimeException e) {
            System.err.println("Error al obtener token JWT para MaxThroughputIT.");
            throw e;
        }

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);
        
        ventasChannel = ManagedChannelBuilder.forAddress("localhost", 9092).usePlaintext().build();
        ventasStub = VentasServiceGrpc.newBlockingStub(ventasChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
                
        catalogoChannel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext().build();
        catalogoStub = CatalogoServiceGrpc.newBlockingStub(catalogoChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

        //Crear un producto para la prueba
        long idCategoria = 1L;
        try {
            var categoriaResponse = catalogoStub.crearCategoria(CrearCategoriaRequest.newBuilder()
                    .setNombreCategoria("Categoría Masiva " + UUID.randomUUID().toString().substring(0, 5))
                    .setDescripcion("Generada por test de rendimiento")
                    .build());
            idCategoria = categoriaResponse.getCategoria().getIdCategoria();
        } catch (StatusRuntimeException e) {
            System.err.println("Aviso: No se pudo crear la categoría: " + e.getMessage());
        }

        skuGlobal = "SKU-MASS-" + UUID.randomUUID().toString().substring(0, 5);
        eanGlobal = String.valueOf(System.currentTimeMillis()).substring(0, 13);
        
        catalogoStub.crearProducto(CrearProductoRequest.newBuilder()
                .setSku(skuGlobal)
                .setEan(eanGlobal)
                .setNombre("Producto Masivo")
                .setPrecioVenta("10.00")
                .setIdCategoria(idCategoria)
                .setUnidadMedida(UnidadMedida.UNIDAD)
                .build());

        // Esperar a que el producto llegue al Caché Ventas desde Kafka
        Thread.sleep(3000); 
    }

    @AfterAll
    public static void teardown() throws InterruptedException {
        if (usuariosChannel != null) usuariosChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        if (catalogoChannel != null) catalogoChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        if (ventasChannel != null) ventasChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    public void testMaximumThroughputFor10Seconds() throws InterruptedException {
        int numberOfThreads = 400; 
        long testDurationInSeconds = 10;
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        System.out.println("\n[STRESS TEST - Ciclo de vida de venta] Iniciando prueba de RENDIMIENTO MÁXIMO...");
        System.out.println("[STRESS TEST] " + numberOfThreads + " hilos ejecutando el ciclo COMPLETO (Crear -> Añadir -> Pagar -> Cerrar) durante " + testDurationInSeconds + " segundos.");

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    long endTime = System.currentTimeMillis() + (testDurationInSeconds * 1000);
                    
                    while (System.currentTimeMillis() < endTime) {
                        try {
                            //Crear Ticket
                            var createResponse = ventasStub.crearNuevoTicket(CrearNuevoTicketRequest.newBuilder().build());
                            String idTicket = createResponse.getIdTicketTemporal();
                            
                            //Añadir Producto
                            ventasStub.anadirProductoTicket(AnadirProductoTicketRequest.newBuilder()
                                    .setIdTicketTemporal(idTicket)
                                    .setCodigoBarras(eanGlobal) // Buscamos por EAN, forzando consulta a Caché
                                    .build());
                                    
                            //Procesar Pago
                            ventasStub.procesarPago(ProcesarPagoRequest.newBuilder()
                                    .setIdTicketTemporal(idTicket)
                                    .setMetodoPago(MetodoPago.EFECTIVO)
                                    .setMontoRecibido("10.00")
                                    .build());
                                    
                            //Cerrar Ticket
                            var closeResponse = ventasStub.cerrarTicket(CerrarTicketRequest.newBuilder()
                                    .setIdTicketTemporal(idTicket)
                                    .build());
                                    
                            if (closeResponse.getNumeroTicket() != null && !closeResponse.getNumeroTicket().isEmpty()) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        
        System.out.println("[STRESS TEST] Disparando peticiones ---");
        long globalStartTime = System.currentTimeMillis();
        startLatch.countDown(); 
        
        doneLatch.await(testDurationInSeconds + 30, TimeUnit.SECONDS); // Damos margen para que las transacciones en vuelo acaben
        long globalEndTime = System.currentTimeMillis();
        long actualDurationMs = globalEndTime - globalStartTime;

        System.out.println("--- [STRESS TEST FINALIZADO] ---");
        System.out.println("Duración real de las peticiones: " + actualDurationMs + " ms");
        System.out.println("Ciclos completos de Venta exitosos: " + successCount.get());
        System.out.println("Ciclos fallidos/rechazados: " + errorCount.get());
        
        // El throughput real de transacciones gRPC es 4 veces los ciclos exitosos
        long totalGrpcCalls = (successCount.get() * 4L) + errorCount.get();
        double throughput = (totalGrpcCalls * 1000.0) / actualDurationMs;
        System.out.printf("Rendimiento Máximo Sostenido (Throughput): %.2f peticiones gRPC individuales por segundo%n", throughput);

        assertThat(successCount.get())
                .as("El servicio no pudo procesar ningún ciclo de venta completo")
                .isGreaterThan(10);
    }
}
