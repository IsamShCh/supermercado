package com.isam.infrastructure.kafka;

import com.isam.grpc.eventos.EventoProductoCreado;
import com.isam.grpc.eventos.EventoProductoModificado;
import com.isam.model.Producto;
import com.isam.service.ports.IProductoEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Adaptador de infraestructura para publicar eventos en Kafka.
 * Implementa el puerto IProductoEventPublisher definido en la capa de
 * Aplicación.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProductoEventPublisher implements IProductoEventPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC_PRODUCTOS = "catalogo.productos";

    @Override
    public void publicarProductoCreado(Producto producto) {
        try {
            log.debug("Publicando producto creado para SKU: {}", producto.getSku());

            EventoProductoCreado evento = EventoProductoCreado.newBuilder()
                    .setIdEvento(UUID.randomUUID().toString())
                    .setTimestamp(System.currentTimeMillis())
                    .setSku(producto.getSku())
                    .setNombreSnapshot(producto.getNombre() != null ? producto.getNombre() : "")
                    .setCategoriaSnapshot(
                            producto.getCategoria() != null ? producto.getCategoria().getNombreCategoria() : "")
                    .setUnidadMedidaSnapshot(
                            producto.getUnidadMedida() != null ? producto.getUnidadMedida().name() : "")
                    .setEsGranelSnapshot(producto.getEsGranel() != null ? producto.getEsGranel() : false)
                    .setCaducaSnapshot(producto.getCaduca() != null ? producto.getCaduca() : false)
                    .setEanSnapshot(producto.getEan() != null ? producto.getEan() : "")
                    .setPluSnapshot(producto.getPlu() != null ? producto.getPlu() : "")
                    .build();

            enviarAKafka(producto.getSku(), evento.toByteArray(), "ProductoCreado");
        } catch (Exception e) {
            log.error("Error al publicar evento ProductoCreado para SKU: {}", producto.getSku(), e);
        }
    }

    @Override
    public void publicarProductoModificado(Producto producto) {
        try {
            log.debug("Publicando producto modificado para SKU: {}", producto.getSku());

            EventoProductoModificado evento = EventoProductoModificado.newBuilder()
                    .setIdEvento(UUID.randomUUID().toString())
                    .setTimestamp(System.currentTimeMillis())
                    .setSku(producto.getSku())
                    .setNombreSnapshot(producto.getNombre() != null ? producto.getNombre() : "")
                    .setCategoriaSnapshot(
                            producto.getCategoria() != null ? producto.getCategoria().getNombreCategoria() : "")
                    .setUnidadMedidaSnapshot(
                            producto.getUnidadMedida() != null ? producto.getUnidadMedida().name() : "")
                    .setEanSnapshot(producto.getEan() != null ? producto.getEan() : "")
                    .setPluSnapshot(producto.getPlu() != null ? producto.getPlu() : "")
                    .build();

            enviarAKafka(producto.getSku(), evento.toByteArray(), "ProductoModificado");
        } catch (Exception e) {
            log.error("Error al publicar evento ProductoModificado para SKU: {}", producto.getSku(), e);
        }
    }

    private void enviarAKafka(String key, byte[] payload, String tipo) {
        kafkaTemplate.send(TOPIC_PRODUCTOS, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Evento {} enviado a Kafka (offset: {})", tipo, result.getRecordMetadata().offset());
                    } else {
                        log.error("Error al enviar evento {} a Kafka", tipo, ex);
                    }
                });
    }
}
