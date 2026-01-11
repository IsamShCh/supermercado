package com.isam.integration.client;

import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.catalogo.ConsultarProductoRequest;
import com.isam.grpc.catalogo.ProductoProto;
import com.isam.mapper.InventarioMapper;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CatalogoGrpcClient {
    
    @Value("${grpc.client.catalogo.host:localhost}")
    private String catalogoHost;
    
    @Value("${grpc.client.catalogo.port:9090}")
    private int catalogoPort;
    
    private ManagedChannel channel;
    private CatalogoServiceGrpc.CatalogoServiceBlockingStub catalogoStub;
    private final InventarioMapper inventarioMapper;
    
    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder
            .forAddress(catalogoHost, catalogoPort)
            .usePlaintext()
            .build();
        
        catalogoStub = CatalogoServiceGrpc.newBlockingStub(channel);
        log.info("Conexión gRPC con servicio de catálogo establecida en {}:{}", catalogoHost, catalogoPort);
    }
    
    @PreDestroy
    public void cleanup() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("Canal gRPC con servicio de catálogo cerrado");
        }
    }
    
    /**
     * Consulta un producto por su SKU
     * @param sku El SKU del producto a consultar
     * @return Los datos del producto
     * @throws StatusRuntimeException Si hay error en la comunicación
     */
    public ConsultarProductoDto consultarProducto(String sku) throws StatusRuntimeException {
        log.debug("Consultando producto con SKU '{}'", sku);
        
        ConsultarProductoRequest request = ConsultarProductoRequest.newBuilder()
            .setSku(sku)
            .build();
        
        // Inyectar token de seguridad si existe (Propagación de Token)
        CatalogoServiceGrpc.CatalogoServiceBlockingStub stubUsar = catalogoStub;
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getCredentials() != null) {
            String token = auth.getCredentials().toString();
            stubUsar = catalogoStub.withCallCredentials(new BearerTokenCallCredentials(token));
        }
        
        ConsultarProductoRequest.Response response = stubUsar.consultarProducto(request);
        
        ProductoProto producto = response.getProducto();
        log.debug("Producto consultado: SKU='{}', Nombre='{}'", producto.getSku(), producto.getNombre());
        
        
        return inventarioMapper.toDto(producto);
    }
    
    /**
     * Verifica si un producto existe en el catálogo
     * @param sku El SKU del producto a verificar
     * @return true si el producto existe, false si no existe
     * @throws StatusRuntimeException Si hay error en la comunicación
     */
    public boolean existeProducto(String sku) throws StatusRuntimeException {
        log.debug("Verificando existencia del producto con SKU '{}'", sku);
        
        try {
            consultarProducto(sku);
            log.debug("Producto con SKU '{}' existe en el catálogo", sku);
            return true;
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                log.debug("Producto con SKU '{}' no existe en el catálogo", sku);
                return false;
            }
            // Si es otro error, lo propagamos
            throw e;
        }
    }
}