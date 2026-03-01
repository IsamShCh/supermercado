package com.isam.simulador.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un lote de productos en almacén.
 * Una vez que el producto pasa a estantería, pierde la identidad del lote.
 */
@Getter
@Setter
@Slf4j
public class LoteSimulado {
    private String idLote;
    private String sku;
    private String numeroLote;
    private BigDecimal cantidadAlmacen;  // Solo en almacén
    private String fechaCaducidad;      // YYYY-MM-DD, opcional
    private String fechaIngreso;        // YYYY-MM-DD
    private String idProveedor;
    private LocalDateTime fechaCreacion;
    private boolean estaDisponible;

    public LoteSimulado() {
        this.fechaCreacion = LocalDateTime.now();
        this.estaDisponible = true;
    }

    public LoteSimulado(String idLote, String sku, String numeroLote, BigDecimal cantidadAlmacen, 
                       String fechaCaducidad, String idProveedor) {
        this();
        this.idLote = idLote;
        this.sku = sku;
        this.numeroLote = numeroLote;
        this.cantidadAlmacen = cantidadAlmacen;
        this.fechaCaducidad = fechaCaducidad;
        this.fechaIngreso = LocalDate.now().toString();
        this.idProveedor = idProveedor;
    }

    /**
     * Reduce la cantidad en almacén y retorna si quedó disponible.
     */
    public boolean reducirCantidad(BigDecimal cantidad) {
        if (cantidadAlmacen.compareTo(cantidad) >= 0) {
            this.cantidadAlmacen = this.cantidadAlmacen.subtract(cantidad);
            
            // Si no queda stock, marcar como no disponible
            if (this.cantidadAlmacen.compareTo(BigDecimal.ZERO) <= 0) {
                this.estaDisponible = false;
                log.debug("Lote {} agotado para SKU {}", idLote, sku);
            }
            
            return true;
        }
        return false;
    }

    /**
     * Verifica si el lote está cerca de caducar (para priorizar en mermas).
     */
    public boolean estaCercanoCaducidad() {
        if (fechaCaducidad == null || fechaCaducidad.isEmpty()) {
            return false;
        }
        
        try {
            LocalDate fechaCad = LocalDate.parse(fechaCaducidad);
            LocalDate hoy = LocalDate.now();
            LocalDate limite = hoy.plusDays(7); // 7 días para caducar
            
            return fechaCad.isBefore(limite) || fechaCad.isEqual(limite);
        } catch (Exception e) {
            log.warn("Error parseando fecha de caducidad: {}", fechaCaducidad, e);
            return false;
        }
    }

    @Override
    public String toString() {
        return "LoteSimulado{" +
                "idLote='" + idLote + '\'' +
                ", sku='" + sku + '\'' +
                ", numeroLote='" + numeroLote + '\'' +
                ", cantidadAlmacen=" + cantidadAlmacen +
                ", fechaCaducidad='" + fechaCaducidad + '\'' +
                ", estaDisponible=" + estaDisponible +
                '}';
    }
}