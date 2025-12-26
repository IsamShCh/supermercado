package com.isam.infrastructure.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.isam.dto.venta.ItemVentaDto;
import com.isam.dto.venta.RegistrarVentaRequestDto;
import com.isam.grpc.eventos.EventoVentaRealizada;
import com.isam.grpc.eventos.ItemVentaEvento;
import com.isam.model.UnidadMedida;
import com.isam.service.ports.IVentaEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de infraestructura para consumir eventos de ventas desde Kafka.
 * Traduce los mensajes (Protocol Buffers) a tipos de dominio y delega en el
 * puerto IVentaEventHandler, manteniendo el desacoplamiento mediante DIP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VentaEventConsumer {

    private final IVentaEventHandler ventaEventHandler;

    @KafkaListener(topics = "ventas.venta.realizada", groupId = "inventario-consumer")
    public void consumirEventoVenta(byte[] payload) {
        try {
            EventoVentaRealizada evento = EventoVentaRealizada.parseFrom(payload);
            log.info("Recibido evento de venta: Ticket={}", evento.getNumeroTicket());

            List<ItemVentaDto> itemsDto = new ArrayList<>();
            
            for (ItemVentaEvento itemEvento : evento.getItemsList()) {
                UnidadMedida udm = UnidadMedida.UNIDAD;

                try {
                    if (itemEvento.getUnidadMedidaSnapshot() != null && !itemEvento.getUnidadMedidaSnapshot().isEmpty()) {
                        udm = UnidadMedida.valueOf(itemEvento.getUnidadMedidaSnapshot());
                    }
                } catch (Exception e) {
                    log.warn("Unidad de medida desconocida en evento: {}", itemEvento.getUnidadMedidaSnapshot());
                }

                itemsDto.add(new ItemVentaDto(
                    itemEvento.getSku(),
                    new BigDecimal(itemEvento.getCantidad()),
                    udm
                ));
            }

            RegistrarVentaRequestDto requestDto = new RegistrarVentaRequestDto(
                evento.getNumeroTicket(),
                itemsDto
            );

            ventaEventHandler.onVentaRealizada(requestDto);
            
            log.info("Stock actualizado satisfactoriamente para Ticket: {}", evento.getNumeroTicket());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializando evento VentaRealizada", e);
        } catch (Exception e) {
            log.error("Error procesando actualización de inventario para venta", e);
        }
    }
}
