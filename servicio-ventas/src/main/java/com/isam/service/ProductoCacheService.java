package com.isam.service;

import com.isam.model.ProductoCache;
import com.isam.model.UnidadMedida;
import com.isam.repository.ProductoCacheRepository;
import com.isam.service.ports.IProductoEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio para gestionar la caché local de productos.
 * Implementa el puerto IProductoEventHandler para recibir eventos de productos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoCacheService implements IProductoEventHandler {

    private final ProductoCacheRepository productoCacheRepository;

    /**
     * Implementación del puerto IProductoEventHandler.
     * Actualiza la caché local con los datos del producto recibido.
     */
    @Override
    @Transactional
    public void onProductoActualizado(String sku, String nombre, BigDecimal precioVenta,
                                       UnidadMedida unidadMedida, String categoria,
                                       String ean, String plu) {
        log.debug("Actualizando caché de producto: SKU={}", sku);
        
        ProductoCache cache = productoCacheRepository.findById(sku)
            .orElse(new ProductoCache());
            
        cache.setSku(sku);
        cache.setNombre(nombre);
        cache.setPrecioVenta(precioVenta != null ? precioVenta : BigDecimal.ZERO);
        cache.setUnidadMedida(unidadMedida != null ? unidadMedida : UnidadMedida.UNIDAD);
        cache.setCategoria(categoria);
        cache.setEan(ean);
        cache.setPlu(plu);
        cache.setFechaActualizacion(LocalDateTime.now());
        
        productoCacheRepository.save(cache);
        log.info("Producto actualizado en caché Ventas: {}", cache.getSku());
    }
}
