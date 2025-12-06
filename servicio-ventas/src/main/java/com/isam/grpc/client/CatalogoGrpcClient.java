package com.isam.grpc.client;

import com.isam.dto.CatalogoClient.CategoriaDto;
import com.isam.dto.CatalogoClient.ProductoDto;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

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
        
        // Inyectar token de seguridad si existe (Propagación de Token)
        CatalogoServiceGrpc.CatalogoServiceBlockingStub stubUsar = catalogoStub;
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getCredentials() != null) {
            String token = auth.getCredentials().toString();
            stubUsar = catalogoStub.withCallCredentials(new BearerTokenCallCredentials(token));
        }
        
        TraducirIdentificadorRequest.Response response = stubUsar.traducirIdentificador(request);
        
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
    public ProductoDto consultarProducto(String sku) throws StatusRuntimeException {
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
        log.debug("Producto consultado: SKU='{}', Nombre='{}', Precio='{}'", producto.getSku(), producto.getNombre(), producto.getPrecioVenta());
        
        // Convertir CategoriaProto a CategoriaDto si existe
        CategoriaDto categoriaDto = null;
        if (producto.hasCategoria()) {
            categoriaDto = new CategoriaDto(
                producto.getCategoria().getIdCategoria(),
                producto.getCategoria().getNombreCategoria(),
                producto.getCategoria().getDescripcion()
            );
        }
        
        // Extraer EAN o PLU
        String ean = producto.hasEan() ? producto.getEan() : null;
        String plu = producto.hasPlu() ? producto.getPlu() : null;
        
        // Convertir enums a strings (acceso directo para campos regulares)
        String politicaRotacion = producto.getPoliticaRotacion() != null ?
            producto.getPoliticaRotacion().name() : null;
        String unidadMedida = producto.getUnidadMedida() != null ?
            producto.getUnidadMedida().name() : null;
        String estado = producto.getEstado() != null ?
            producto.getEstado().name() : null;
        
        // Convertir precio de string a BigDecimal
        BigDecimal precioVenta = new BigDecimal(producto.getPrecioVenta());
        
        return new ProductoDto(
            producto.getSku(),
            ean,
            plu,
            producto.getNombre(),
            producto.getDescripcion(),
            precioVenta,
            producto.getCaduca(),
            producto.getEsGranel(),
            categoriaDto,
            politicaRotacion,
            unidadMedida,
            producto.getEtiquetasList(),
            estado
        );
    }
}