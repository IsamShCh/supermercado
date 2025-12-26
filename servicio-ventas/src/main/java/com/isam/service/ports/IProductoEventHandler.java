package com.isam.service.ports;

import com.isam.model.UnidadMedida;
import java.math.BigDecimal;

/**
 * Puerto de entrada para eventos de productos.
 * Define la capacidad de recibir notificaciones sobre cambios en productos
 * sin depender de una tecnología de transporte específica (Kafka, RabbitMQ, etc.).
 * 
 * Este puerto es implementado por la capa de aplicación y utilizado por
 * los adaptadores de infraestructura (consumers).
 */
public interface IProductoEventHandler {
    
    /**
     * Maneja el evento de creación o modificación de un producto.
     * Actualiza la caché local con los datos del producto.
     * 
     * @param sku Identificador único del producto
     * @param nombre Nombre del producto
     * @param precioVenta Precio de venta del producto
     * @param unidadMedida Unidad de medida del producto
     * @param categoria Categoría del producto
     * @param ean Código EAN (puede ser null)
     * @param plu Código PLU (puede ser null)
     */
    void onProductoActualizado(String sku, String nombre, BigDecimal precioVenta, 
                                UnidadMedida unidadMedida, String categoria, 
                                String ean, String plu);
}
