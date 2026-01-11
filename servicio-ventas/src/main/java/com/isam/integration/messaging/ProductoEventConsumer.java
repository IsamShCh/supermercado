package com.isam.integration.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import com.isam.grpc.eventos.EventoProductoCreado;
import com.isam.model.UnidadMedida;
import com.isam.service.ports.IProductoEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Adaptador de infraestructura para consumir eventos de productos desde Kafka.
 * Traduce los mensajes (Protocol Buffers) a tipos de dominio y delega en el
 * puerto IProductoEventHandler, manteniendo el desacoplamiento mediante DIP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoEventConsumer {

    private final IProductoEventHandler productoEventHandler;

    @KafkaListener(topics = "catalogo.producto.eventos", groupId = "ventas-consumer")
    public void consumirEventosProducto(byte[] payload) {
        try {
            EventoProductoCreado evento;
            try {
                evento = EventoProductoCreado.parseFrom(payload);
            } catch (InvalidProtocolBufferException e) {
                log.error("Error deserializando evento producto", e);
                return;
            }
            
            log.debug("Recibido evento producto: SKU={}", evento.getSku());
            
            BigDecimal precioVenta = BigDecimal.ZERO;
            try {
                if (evento.getPrecioVentaSnapshot() != null && !evento.getPrecioVentaSnapshot().isEmpty()) {
                    precioVenta = new BigDecimal(evento.getPrecioVentaSnapshot());
                }
            } catch (Exception e) {
                log.warn("Error parseando precio: {}", evento.getPrecioVentaSnapshot());
            }
            
            UnidadMedida unidadMedida = UnidadMedida.UNIDAD;
            try {
                if (evento.getUnidadMedidaSnapshot() != null && !evento.getUnidadMedidaSnapshot().isEmpty()) {
                    unidadMedida = UnidadMedida.valueOf(evento.getUnidadMedidaSnapshot());
                }
            } catch (IllegalArgumentException e) {
                log.warn("Unidad de medida desconocida: {}", evento.getUnidadMedidaSnapshot());
            }
            
            productoEventHandler.onProductoActualizado(
                    evento.getSku(),
                    evento.getNombreSnapshot(),
                    precioVenta,
                    unidadMedida,
                    evento.getCategoriaSnapshot(),
                    evento.getEanSnapshot(),
                    evento.getPluSnapshot());

        } catch (Exception e) {
            log.error("Error procesando mensaje de producto en Ventas", e);
        }
    }
}
