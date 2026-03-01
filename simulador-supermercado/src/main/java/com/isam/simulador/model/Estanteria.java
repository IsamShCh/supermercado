package com.isam.simulador.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa una estantería del supermercado.
 * Contiene producto genérico con contadores por estado (disponible, robado, mermado).
 * Una vez que el producto pasa a estantería, pierde la identidad del lote.
 */
@Getter
@Setter
@Slf4j
public class Estanteria {
    private String idEstanteria;
    private String sku;
    private BigDecimal capacidadMaxima;
    
    // Contadores por estado del producto en estantería
    private BigDecimal stockDisponible = BigDecimal.ZERO;  // Productos normales para venta
    private BigDecimal stockRobado = BigDecimal.ZERO;       // Productos robados
    private BigDecimal stockMermado = BigDecimal.ZERO;      // Productos mermados (dañados/caducados)
    
    public Estanteria() {}
    
    public Estanteria(String idEstanteria, String sku, BigDecimal capacidadMaxima) {
        this.idEstanteria = idEstanteria;
        this.sku = sku;
        this.capacidadMaxima = capacidadMaxima;
    }

    /**
     * Calcula el stock total en estantería (suma de todos los estados).
     */
    public BigDecimal getStockTotal() {
        return stockDisponible.add(stockRobado).add(stockMermado);
    }

    /**
     * Calcula el porcentaje de ocupación de la estantería.
     */
    public BigDecimal getPorcentajeOcupado() {
        return getStockTotal().divide(capacidadMaxima, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el porcentaje de stock disponible para venta.
     */
    public BigDecimal getPorcentajeDisponible() {
        return stockDisponible.divide(capacidadMaxima, 2, RoundingMode.HALF_UP);
    }

    /**
     * Verifica si la estantería necesita replenishment.
     * Necesita replenishment si el stock disponible es menor al 20-35% de la capacidad.
     */
    public boolean necesitaReplenishment() {
        BigDecimal porcentajeDisponible = getPorcentajeDisponible();
        return porcentajeDisponible.compareTo(new BigDecimal("0.20")) < 0;
    }

    /**
     * Añade stock disponible (cuando se mueve de almacén a estantería).
     */
    public synchronized void añadirStockDisponible(BigDecimal cantidad) {
        BigDecimal nuevoStock = this.stockDisponible.add(cantidad);
        
        // Verificar que no exceda la capacidad máxima
        if (nuevoStock.compareTo(capacidadMaxima) > 0) {
            BigDecimal excedente = nuevoStock.subtract(capacidadMaxima);
            this.stockDisponible = capacidadMaxima;
            log.warn("⚠️ Estantería {} excedida. Stock ajustado a capacidad máxima. Excedente: {}", 
                idEstanteria, excedente);
        } else {
            this.stockDisponible = nuevoStock;
            log.debug("📦 Stock añadido a estantería {}: +{} (Total disponible: {})", 
                idEstanteria, cantidad, this.stockDisponible);
        }
    }

    /**
     * Reduce stock disponible por venta.
     */
    public synchronized boolean reducirStockDisponible(BigDecimal cantidad) {
        if (stockDisponible.compareTo(cantidad) >= 0) {
            this.stockDisponible = this.stockDisponible.subtract(cantidad);
            log.debug("🛒 Stock reducido en estantería {}: -{} (Total disponible: {})", 
                idEstanteria, cantidad, this.stockDisponible);
            return true;
        }
        log.warn("⚠️ Stock insuficiente en estantería {}: Disponible={}, Requerido={}", 
            idEstanteria, stockDisponible, cantidad);
        return false;
    }

    /**
     * Mueve stock de disponible a robado.
     */
    public synchronized void moverDisponibleARobado(BigDecimal cantidad) {
        if (stockDisponible.compareTo(cantidad) >= 0) {
            this.stockDisponible = this.stockDisponible.subtract(cantidad);
            this.stockRobado = this.stockRobado.add(cantidad);
            log.info("🚨 Robo en estantería {}: {} unidades movidas de disponible a robado", 
                idEstanteria, cantidad);
        }
    }

    /**
     * Mueve stock de disponible a mermado.
     */
    public synchronized void moverDisponibleAMermado(BigDecimal cantidad) {
        if (stockDisponible.compareTo(cantidad) >= 0) {
            this.stockDisponible = this.stockDisponible.subtract(cantidad);
            this.stockMermado = this.stockMermado.add(cantidad);
            log.info("⚠️ Merma en estantería {}: {} unidades movidas de disponible a mermado", 
                idEstanteria, cantidad);
        }
    }

    /**
     * Obtiene estadísticas de la estantería para reportes.
     */
    public EstadisticasEstanteria getEstadisticas() {
        return new EstadisticasEstanteria(
            idEstanteria,
            sku,
            stockDisponible,
            stockRobado,
            stockMermado,
            getStockTotal(),
            getPorcentajeOcupado(),
            getPorcentajeDisponible()
        );
    }

    @Override
    public String toString() {
        return "Estanteria{" +
                "idEstanteria='" + idEstanteria + '\'' +
                ", sku='" + sku + '\'' +
                ", capacidadMaxima=" + capacidadMaxima +
                ", stockDisponible=" + stockDisponible +
                ", stockRobado=" + stockRobado +
                ", stockMermado=" + stockMermado +
                ", porcentajeDisponible=" + getPorcentajeDisponible() +
                '}';
    }

    /**
     * DTO para estadísticas de estantería.
     */
    public record EstadisticasEstanteria(
        String idEstanteria,
        String sku,
        BigDecimal stockDisponible,
        BigDecimal stockRobado,
        BigDecimal stockMermado,
        BigDecimal stockTotal,
        BigDecimal porcentajeOcupado,
        BigDecimal porcentajeDisponible
    ) {}
}