package com.isam.service;

import com.isam.grpc.eventos.EventoMovimientoInventario;
import com.isam.model.MovimientoInventario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioEventService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC_MOVIMIENTOS = "movimientos-inventario";

    public void publicarMovimiento(MovimientoInventario movimiento) {
        try {
            log.debug("Preparando evento de movimiento para SKU: {}", movimiento.getSku());

            // Mapeo de ID Lote (puede ser null)
            String loteId = movimiento.getIdLote() != null ? movimiento.getIdLote() : "";

            // Construir el evento Protobuf
            EventoMovimientoInventario evento = EventoMovimientoInventario.newBuilder()
                    .setIdEvento(java.util.UUID.randomUUID().toString())
                    .setTimestamp(System.currentTimeMillis())
                    .setIdMovimiento(movimiento.getIdMovimiento())
                    .setSku(movimiento.getSku())
                    .setIdLote(loteId)
                    // En eventos.proto definimos tipo_movimiento como string para desacoplar
                    .setTipoMovimiento(movimiento.getTipoMovimiento().name())
                    .setCantidad(movimiento.getCantidad().toString()) // Decimal como String
                    .setUnidadMedida(com.isam.grpc.common.UnidadMedida.valueOf(movimiento.getUnidadMedida().name()))
                    .setFechaHora(movimiento.getFechaHora().atOffset(ZoneOffset.UTC).toString())
                    .setIdUsuario(movimiento.getIdUsuario() != null ? movimiento.getIdUsuario() : "SYSTEM")
                    .setMotivo(movimiento.getMotivo() != null ? movimiento.getMotivo() : "")
                    .build();

            // Enviar a Kafka
            // Usamos el SKU como Key para garantizar orden secuencial por producto en la misma partición
            kafkaTemplate.send(TOPIC_MOVIMIENTOS, movimiento.getSku(), evento.toByteArray())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Evento de movimiento enviado correctamente. Offset: {}", result.getRecordMetadata().offset());
                        } else {
                            log.error("Error al enviar evento de movimiento a Kafka", ex);
                        }
                    });

        } catch (Exception e) {
            // Importante: No queremos que un fallo en Kafka tire abajo la transacción de inventario principal
            log.error("Error inesperado al publicar evento de movimiento", e);
        }
    }
}