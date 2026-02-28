package com.isam.e2e;

import com.isam.grpc.usuarios.IniciarSesionRequest;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.grpc.ventas.CrearNuevoTicketRequest;
import com.isam.grpc.ventas.VentasServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class LoadTestingIT {

    private static ManagedChannel usuariosChannel;
    private static ManagedChannel ventasChannel;
    private static VentasServiceGrpc.VentasServiceBlockingStub ventasStub;
    private static String jwtToken;

    @BeforeAll
    public static void setup() {
        //Conectar a Usuarios para autenticación
        usuariosChannel = ManagedChannelBuilder.forAddress("localhost", 9093)
                .usePlaintext()
                .build();
        UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub = UsuarioServiceGrpc.newBlockingStub(usuariosChannel);

        try {
            IniciarSesionRequest loginRequest = IniciarSesionRequest.newBuilder()
                    .setNombreUsuario("admin")
                    .setPassword("admin123")
                    .build();
            var loginResponse = usuariosStub.iniciarSesion(loginRequest);
            jwtToken = loginResponse.getTokenJwt();
            System.out.println("Token obtenido exitosamente para Load Testing.");
        } catch (StatusRuntimeException e) {
            System.err.println("Error al obtener token JWT. " + e.getStatus());
            throw e;
        }

        //Preparar el stub de Ventas con el token JWT inyectado
        ventasChannel = ManagedChannelBuilder.forAddress("localhost", 9092)
                .usePlaintext()
                .build();

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);

        ventasStub = VentasServiceGrpc.newBlockingStub(ventasChannel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @AfterAll
    public static void teardown() throws InterruptedException {
        if (usuariosChannel != null) {
            usuariosChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (ventasChannel != null) {
            ventasChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testConcurrentTicketCreation() throws InterruptedException {
        int numberOfThreads = 500;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        System.out.println("Iniciando prueba de carga con " + numberOfThreads + " hilos concurrentes hacia servicio-ventas...");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                readyLatch.countDown(); // El hilo indica que está listo
                try {
                    startLatch.await(); // Espera la señal de salida para atacar todos a la vez
                    
                    CrearNuevoTicketRequest request = CrearNuevoTicketRequest.newBuilder().build();
                    var response = ventasStub.crearNuevoTicket(request);
                    
                    if (response.getIdTicketTemporal() != null && !response.getIdTicketTemporal().isEmpty()) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        //Esperamos a los hilos
        readyLatch.await();
        
        //Disparamos todos los hilos al mismo tiempo
        startLatch.countDown(); 
        
        //Esperar a que terminen las llamadas
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Prueba finalizada.");
        System.out.println("Tiempo total: " + duration + " ms");
        System.out.println("Tickets creados exitosamente: " + successCount.get());
        System.out.println("Errores/Fallos: " + errorCount.get());
        
        double throughput = (successCount.get() * 1000.0) / duration;
        System.out.printf("Throughput: %.2f peticiones completadas/segundo%n", throughput);

        assertThat(completed).as("El test de carga no finalizó antes del timeout de 30 segundos").isTrue();
        assertThat(errorCount.get()).as("Se registraron errores durante el stress test de creación de tickets").isZero();
        assertThat(successCount.get()).as("No todos los tickets se crearon exitosamente").isEqualTo(numberOfThreads);
    }
}
