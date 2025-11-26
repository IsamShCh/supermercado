package com.isam.grpc.client;

import com.isam.grpc.catalogo.CatalogoServiceGrpc;
import com.isam.grpc.catalogo.ConsultarProductoRequest;
import com.isam.grpc.catalogo.ProductoProto;
import com.isam.grpc.catalogo.TraducirIdentificadorRequest;
import com.isam.grpc.catalogo.ResultadoTraduccion;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CatalogoGrpcClient {
    
    @Value("${grpc.client.catalogo.host:localhost}")
    private String catalogoHost;
    
    @Value("${grpc.client.catalogo.port:9090}")
    private int catalogoPort;
    
    private ManagedChannel channel;
    private CatalogoServiceGrpc.CatalogoServiceBlockingStub catalogoStub;
    
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
     * Traduce un código de barras (EAN/PLU) a SKU
     * @param codigoBarras El código de barras a traducir
     * @return El SKU del producto
     * @throws StatusRuntimeException Si hay error en la comunicación
     */
    public String traducirCodigoBarrasASku(String codigoBarras) throws StatusRuntimeException {
        log.debug("Traduciendo código de barras '{}' a SKU", codigoBarras);
        
        TraducirIdentificadorRequest.Builder requestBuilder = TraducirIdentificadorRequest.newBuilder();
            
        // Determinar si es EAN o PLU basado en la longitud
        if(codigoBarras.contains("SKU")){
            return codigoBarras;
        } else if (codigoBarras.length() == 13 ) {
            requestBuilder.setEan(codigoBarras);
        } else if (codigoBarras.length() == 5) {
            requestBuilder.setPlu(codigoBarras);
        } else {
            // Si no encontramos entonces hay un error en el formato
            throw Status.INVALID_ARGUMENT
                .withDescription("Código de barras no válido: " + codigoBarras + "\nUn EAN debe tener 13 dígitos y un PLU 5 dígitos. Un SKU debe contener la palabra 'SKU'")
                .asRuntimeException();
        }
        
        TraducirIdentificadorRequest request = requestBuilder.build();
        TraducirIdentificadorRequest.Response response = catalogoStub.traducirIdentificador(request);
        
        ResultadoTraduccion resultado = response.getResultadoTraduccion();
        
        // Extraer el SKU de salida
        String sku = null;
        switch (resultado.getCodigoSalidaCase()) {
            case SKU_SALIDA:
                sku = resultado.getSkuSalida();
                break;
            case CODIGOSALIDA_NOT_SET:
                log.warn("No se pudo traducir el código de barras '{}'", codigoBarras);
                break;
            default:
                log.warn("Respuesta inesperada al traducir código de barras '{}'", codigoBarras);
                break;
        }
        
        log.debug("Código de barras '{}' traducido a SKU '{}'", codigoBarras, sku);
        return sku;
    }
    
    /**
     * Consulta un producto por su SKU
     * @param sku El SKU del producto a consultar
     * @return Los datos del producto
     * @throws StatusRuntimeException Si hay error en la comunicación
     */
    public ProductoProto consultarProducto(String sku) throws StatusRuntimeException {
        log.debug("Consultando producto con SKU '{}'", sku);
        
        ConsultarProductoRequest request = ConsultarProductoRequest.newBuilder()
            .setSku(sku)
            .build();
        
        ConsultarProductoRequest.Response response = catalogoStub.consultarProducto(request);
        
        ProductoProto producto = response.getProducto();
        log.debug("Producto consultado: SKU='{}', Nombre='{}', Precio='{}'", producto.getSku(), producto.getNombre(), producto.getPrecioVenta());
        
        return producto;
    }
}