package com.isam.infrastructure.kafka;

import com.isam.grpc.eventos.EventoProductoCreado;
import com.isam.grpc.eventos.EventoProductoModificado;
import com.isam.model.UnidadMedida;
import com.isam.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura para consumir eventos de productos desde Kafka.
 * Traduce los mensajes (Protocol Buffers) a tipos de dominio y delega en el
 * servicio.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProductoEventConsumer {

    private final InventarioService inventarioService;

    @KafkaListener(topics = "catalogo.productos", groupId = "inventario-service")
    public void onProductoEvent(byte[] message) {
        try {
            try {
                EventoProductoCreado evento = EventoProductoCreado.parseFrom(message);
                if (evento.getSku() != null && !evento.getSku().isEmpty()) {
                    log.info("Evento ProductoCreado recibido via Kafka para SKU: {}", evento.getSku());
                    inventarioService.actualizarCacheProducto(
                            evento.getSku(),
                            evento.getNombreSnapshot(),
                            UnidadMedida.valueOf(evento.getUnidadMedidaSnapshot()),
                            evento.getEanSnapshot(),
                            evento.getPluSnapshot());
                    return;
                }
            } catch (Exception e) {
                // No era un evento de creación
            }

            try {
                EventoProductoModificado evento = EventoProductoModificado.parseFrom(message);
                if (evento.getSku() != null && !evento.getSku().isEmpty()) {
                    log.info("Evento ProductoModificado recibido via Kafka para SKU: {}", evento.getSku());
                    inventarioService.actualizarCacheProducto(
                            evento.getSku(),
                            evento.getNombreSnapshot(),
                            UnidadMedida.valueOf(evento.getUnidadMedidaSnapshot()),
                            evento.getEanSnapshot(),
                            evento.getPluSnapshot());
                }
            } catch (Exception e) {
                log.warn("Mensaje recibido en Kafka no coincide con formatos conocidos de producto");
            }

        } catch (Exception e) {
            log.error("Error procesando evento de producto desde Kafka", e);
        }
    }
}
