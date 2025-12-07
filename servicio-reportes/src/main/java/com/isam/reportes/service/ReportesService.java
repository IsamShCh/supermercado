package com.isam.reportes.service;

import com.isam.grpc.eventos.EventoMovimientoInventario;
import com.isam.grpc.eventos.EventoVentaRealizada;
import com.isam.grpc.eventos.ItemVentaEvento;
import com.isam.reportes.model.FactMovimientoInventario;
import com.isam.reportes.model.FactVenta;
import com.isam.reportes.repository.FactMovimientoInventarioRepository;
import com.isam.reportes.repository.FactVentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Servicio para la gestión de datos de reportes y BI.
 * Procesa eventos y actualiza las tablas de hechos y dimensiones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportesService {

    private final FactMovimientoInventarioRepository movimientoRepository;
    private final FactVentaRepository ventaRepository;

    /**
     * Procesa un evento de movimiento de inventario y lo persiste en la tabla de hechos.
     * 
     * @param evento El evento Protobuf recibido de Kafka.
     */
    @Transactional
    public void procesarMovimientoInventario(EventoMovimientoInventario evento) {
        log.debug("Procesando movimiento de inventario para BI - ID: {}", evento.getIdMovimiento());

        try {
            FactMovimientoInventario fact = new FactMovimientoInventario();
            
            // Mapeo directo de campos
            fact.setIdMovimiento(evento.getIdMovimiento());
            fact.setSku(evento.getSku());
            fact.setIdLote(evento.getIdLote().isEmpty() ? null : evento.getIdLote());
            fact.setTipoMovimiento(evento.getTipoMovimiento()); // Viene como String
            fact.setUnidadMedida(evento.getUnidadMedida().name()); // Enum -> String
            fact.setMotivo(evento.getMotivo());
            fact.setIdUsuario(evento.getIdUsuario());
            fact.setUbicacion(evento.getUbicacion());
            
            // Conversión de tipos
            fact.setCantidad(new BigDecimal(evento.getCantidad()));
            
            // Parseo de fechas ISO 8601
            if (evento.getFechaHora() != null && !evento.getFechaHora().isEmpty()) {
                // El productor envía LocalDateTime.toString() -> '2023-12-06T19:00:00'
                fact.setFechaHora(LocalDateTime.parse(evento.getFechaHora(), DateTimeFormatter.ISO_DATE_TIME));
            } else {
                fact.setFechaHora(LocalDateTime.now());
            }

            // Datos de auditoría
            fact.setIdEvento(evento.getIdEvento());
            fact.setFechaProcesado(LocalDateTime.now());

            movimientoRepository.save(fact);
            log.info("Movimiento de inventario guardado en BI - SKU: {}, Tipo: {}", fact.getSku(), fact.getTipoMovimiento());

        } catch (Exception e) {
            log.error("Error al guardar movimiento en BI: {}", e.getMessage(), e);
            throw e; // Relanzar para que Kafka reintente si es un error temporal
        }
    }

    /**
     * Procesa un evento de venta realizada y persiste cada línea en la tabla de hechos.
     * 
     * @param evento El evento Protobuf recibido de Kafka.
     */
    @Transactional
    public void procesarVentaRealizada(EventoVentaRealizada evento) {
        log.debug("Procesando venta para BI - Ticket: {}", evento.getNumeroTicket());

        try {
            // Parsear fecha común para todos los items
            LocalDateTime fechaVenta = LocalDateTime.now();
            if (evento.getTimestamp() > 0) {
                 fechaVenta = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(evento.getTimestamp()), java.time.ZoneId.systemDefault());
            }

            for (ItemVentaEvento item : evento.getItemsList()) {
                FactVenta fact = new FactVenta();
                
                fact.setIdVenta(UUID.randomUUID().toString());
                fact.setIdTicket(evento.getIdTicket());
                fact.setNumeroTicket(evento.getNumeroTicket());
                fact.setFechaHora(fechaVenta);
                fact.setIdUsuarioCajero(evento.getIdUsuarioCajero());
                fact.setMetodoPago(evento.getMetodoPago());
                
                fact.setSku(item.getSku());
                fact.setCantidad(new BigDecimal(item.getCantidad()));
                fact.setPrecioUnitario(new BigDecimal(item.getPrecioUnitario()));
                fact.setTotalLinea(new BigDecimal(item.getTotalLinea()));
                
                fact.setNombreProductoSnapshot(item.getNombreProductoSnapshot());
                fact.setCategoriaSnapshot(item.getCategoriaSnapshot());
                
                fact.setIdEvento(evento.getIdEvento());
                fact.setFechaProcesado(LocalDateTime.now());
                
                ventaRepository.save(fact);
            }
            
            log.info("Venta guardada en BI - Ticket: {}, Items procesados: {}", evento.getNumeroTicket(), evento.getItemsCount());

        } catch (Exception e) {
            log.error("Error al guardar venta en BI: {}", e.getMessage(), e);
            throw e;
        }
    }
}
