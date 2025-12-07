package com.isam.service;

import com.isam.grpc.eventos.EventoVentaRealizada;
import com.isam.grpc.eventos.ItemVentaEvento;
import com.isam.model.ItemTicket;
import com.isam.model.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentasEventService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC_VENTAS = "ventas-realizadas";

    public void publicarVenta(Ticket ticket, List<ItemTicket> items, String metodoPago) {
        try {
            log.debug("Preparando evento de venta para Ticket: {}", ticket.getNumeroTicket());

            // Construir items
            List<ItemVentaEvento> itemsEvento = items.stream()
                .map(item -> ItemVentaEvento.newBuilder()
                    .setSku(item.getSku() != null ? item.getSku() : "")
                    .setCantidad(item.getCantidad() != null ? item.getCantidad().toString() : "0")
                    .setPrecioUnitario(item.getPrecioUnitario() != null ? item.getPrecioUnitario().toString() : "0")
                    .setSubtotal(item.getSubtotal() != null ? item.getSubtotal().toString() : "0")
                    .setImpuesto(item.getImpuesto() != null ? item.getImpuesto().toString() : "0")
                    .setTotalLinea(item.getSubtotal() != null ? item.getSubtotal().toString() : "0") 
                    .setNombreProductoSnapshot(item.getNombreProducto() != null ? item.getNombreProducto() : "")
                    .setCategoriaSnapshot("") 
                    .build())
                .collect(Collectors.toList());

            // Construir evento principal
            EventoVentaRealizada evento = EventoVentaRealizada.newBuilder()
                    .setIdEvento(java.util.UUID.randomUUID().toString())
                    .setTimestamp(System.currentTimeMillis())
                    .setIdTicket(ticket.getIdTicket())
                    .setNumeroTicket(ticket.getNumeroTicket())
                    .setIdUsuarioCajero(ticket.getIdUsuario())
                    .setTotalVenta(ticket.getTotal().toString())
                    .setMetodoPago(metodoPago)
                    .addAllItems(itemsEvento)
                    .build();

            // Enviar a Kafka
            // Key: NumeroTicket
            kafkaTemplate.send(TOPIC_VENTAS, ticket.getNumeroTicket(), evento.toByteArray())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Evento de venta enviado correctamente. Offset: {}", result.getRecordMetadata().offset());
                        } else {
                            log.error("Error al enviar evento de venta a Kafka", ex);
                        }
                    });

        } catch (Exception e) {
            log.error("Error inesperado al publicar evento de venta", e);
        }
    }
}
