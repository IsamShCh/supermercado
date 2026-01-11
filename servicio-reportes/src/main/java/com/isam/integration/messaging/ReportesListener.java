package com.isam.integration.messaging;

import com.isam.grpc.eventos.EventoMovimientoInventario;
import com.isam.grpc.eventos.EventoVentaRealizada;
import com.isam.service.ReportesService;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener para consumir eventos de Kafka relacionados con reportes y BI.
 * Procesa mensajes de movimientos de inventario y ventas realizadas.
 */
@Component
@Slf4j
@lombok.RequiredArgsConstructor
public class ReportesListener {

    private final ReportesService reportesService;

    /**
     * Consume eventos del topic 'movimientos-inventario'.
     * Deserializa el mensaje Protobuf y loguea la información básica.
     * 
     * @param mensaje Array de bytes conteniendo el mensaje Protobuf serializado.
     */
    @KafkaListener(topics = "inventario.movimiento.eventos", groupId = "reportes-consumer")
    public void consumirMovimientoInventario(byte[] mensaje) {
        try {
            EventoMovimientoInventario evento = EventoMovimientoInventario.parseFrom(mensaje);
            log.info("Evento de Movimiento Inventario recibido - SKU: {}, Tipo: {}, Cantidad: {}",
                    evento.getSku(), evento.getTipoMovimiento(), evento.getCantidad());
            
            reportesService.procesarMovimientoInventario(evento);
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Error al deserializar EventoMovimientoInventario", e);
        }
    }

    /**
     * Consume eventos del topic 'ventas-realizadas'.
     * Deserializa el mensaje Protobuf y loguea la información básica.
     * 
     * @param mensaje Array de bytes conteniendo el mensaje Protobuf serializado.
     */
    @KafkaListener(topics = "ventas.venta.realizada", groupId = "reportes-consumer")
    public void consumirVentaRealizada(byte[] mensaje) {
        try {
            EventoVentaRealizada evento = EventoVentaRealizada.parseFrom(mensaje);
            log.info("Evento de Venta Realizada recibido - Ticket: {}, Total: {}",
                    evento.getNumeroTicket(), evento.getTotalVenta());
            
            reportesService.procesarVentaRealizada(evento);
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Error al deserializar EventoVentaRealizada", e);
        }
    }
}
