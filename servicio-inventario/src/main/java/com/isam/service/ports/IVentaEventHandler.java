package com.isam.service.ports;

import com.isam.dto.venta.RegistrarVentaRequestDto;

/**
 * Puerto de entrada para eventos de ventas.
 * Define la capacidad de recibir notificaciones sobre ventas realizadas
 * sin depender de una tecnología de transporte específica (Kafka, RabbitMQ, etc.).
 * 
 * Este puerto es implementado por la capa de aplicación y utilizado por
 * los adaptadores de infraestructura (consumers).
 */
public interface IVentaEventHandler {
    
    /**
     * Maneja el evento de venta realizada.
     * Actualiza el inventario descontando los productos vendidos.
     * 
     * @param request DTO con los datos de la venta a registrar
     */
    void onVentaRealizada(RegistrarVentaRequestDto request);
}
