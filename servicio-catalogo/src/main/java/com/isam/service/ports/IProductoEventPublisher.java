package com.isam.service.ports;

import com.isam.model.Producto;

/**
 * Puerto de salida para eventos de productos.
 * Define la capacidad de notificar cambios en los productos sin depender
 * de una tecnología de transporte específica (Kafka, RabbitMQ, etc.).
 */
public interface IProductoEventPublisher {
    void publicarProductoCreado(Producto producto);

    void publicarProductoModificado(Producto producto);
}
