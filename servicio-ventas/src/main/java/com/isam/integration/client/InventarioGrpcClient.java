package com.isam.integration.client;

import com.isam.grpc.inventario.InventarioServiceGrpc;
import com.isam.grpc.inventario.ItemVenta;
import com.isam.grpc.inventario.RegistrarVentaRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventarioGrpcClient {
    
    @Value("${grpc.client.inventario.host:localhost}")
    private String inventarioHost;
    
    @Value("${grpc.client.inventario.port:9091}")
    private int inventarioPort;
    
    private ManagedChannel channel;
    private InventarioServiceGrpc.InventarioServiceBlockingStub inventarioStub;
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder
            .forAddress(inventarioHost, inventarioPort)
            .usePlaintext()
            .build();
        
        inventarioStub = InventarioServiceGrpc.newBlockingStub(channel);
        log.info("Conexión gRPC con servicio de inventario establecida en {}:{}", inventarioHost, inventarioPort);
    }
    
    @PreDestroy
    public void cleanup() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("Canal gRPC con servicio de inventario cerrado");
        }
    }
    
    /**
     * Registra una venta completa con todos sus items en el servicio de inventario
     * @param numeroTicket Número de ticket de la venta
     * @param items Lista de items vendidos (SKU y cantidad)
     * @throws StatusRuntimeException Si hay error en la comunicación
     */
    public void registrarVenta(String numeroTicket, java.util.List<ItemVenta> items) throws StatusRuntimeException {
        log.info("Registrando venta en inventario: Ticket='{}', Items={}", numeroTicket, items.size());
        
        // Construir el request para registrar la venta
        RegistrarVentaRequest request = RegistrarVentaRequest.newBuilder()
            .setNumeroTicket(numeroTicket)
            .addAllItems(items)
            .build();
        
        try {
            // Inyectar token de seguridad si existe (Propagación de Token)
            InventarioServiceGrpc.InventarioServiceBlockingStub stubUsar = inventarioStub;
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.getCredentials() != null) {
                String token = auth.getCredentials().toString();
                stubUsar = inventarioStub.withCallCredentials(new BearerTokenCallCredentials(token));
            }
            
            RegistrarVentaRequest.Response response = stubUsar.registrarVenta(request);
            log.info("Venta registrada exitosamente en inventario: Ticket='{}', Movimientos={}",
                numeroTicket, response.getMovimientosCount());
        } catch (StatusRuntimeException e) {
            log.error("Error al registrar venta en inventario: Ticket='{}', Error: {}",
                numeroTicket, e.getMessage());
            throw e;
        }
    }
}