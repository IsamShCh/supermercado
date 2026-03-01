package com.isam.simulador.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un producto simulado del catálogo.
 * Contiene referencia a sus lotes (almacén) y estantería (venta).
 */
@Getter
@Setter
@Slf4j
public class ProductoSimulado {
    private String sku;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private boolean caduca;
    private boolean esGranel;
    private String ean;  // Para identificación en ventas
    private String plu;   // Para identificación en ventas
    
    // Referencias separadas
    private List<LoteSimulado> lotes = new ArrayList<>();
    private Estanteria estanteria;
    
    public ProductoSimulado() {}
    
    public ProductoSimulado(String sku, String nombre, String categoria, BigDecimal precio, 
                         boolean caduca, boolean esGranel) {
        this.sku = sku;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.caduca = caduca;
        this.esGranel = esGranel;
    }

    /**
     * Añade un lote al producto.
     */
    public void añadirLote(LoteSimulado lote) {
        if (lote.getSku().equals(this.sku)) {
            this.lotes.add(lote);
            log.debug("📦 Lote {} añadido al producto {}", lote.getIdLote(), sku);
        } else {
            log.warn("⚠️ Intento de añadir lote con SKU {} a producto {}", lote.getSku(), sku);
        }
    }

    /**
     * Obtiene el stock total en almacén (suma de todos los lotes disponibles).
     */
    public BigDecimal getStockTotalAlmacen() {
        return lotes.stream()
            .filter(LoteSimulado::isEstaDisponible)
            .map(LoteSimulado::getCantidadAlmacen)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtiene el stock disponible para venta (en estantería).
     */
    public BigDecimal getStockDisponibleVenta() {
        return estanteria != null ? estanteria.getStockDisponible() : BigDecimal.ZERO;
    }

    /**
     * Verifica si el producto tiene stock disponible en almacén.
     */
    public boolean hayStockAlmacen(BigDecimal cantidad) {
        return getStockTotalAlmacen().compareTo(cantidad) >= 0;
    }

    /**
     * Verifica si el producto tiene stock disponible en estantería.
     */
    public boolean hayStockEstanteria(BigDecimal cantidad) {
        return getStockDisponibleVenta().compareTo(cantidad) >= 0;
    }

    /**
     * Limpia lotes agotados (estaDisponible=false, cantidadAlmacen<=0) de la lista del producto.
     * Previene acumulación de memoria con lotes que ya no aportan nada.
     * @return Número de lotes eliminados
     */
    public int limpiarLotesAgotados() {
        int antes = lotes.size();
        lotes.removeIf(l -> !l.isEstaDisponible() && l.getCantidadAlmacen().compareTo(BigDecimal.ZERO) <= 0);
        int eliminados = antes - lotes.size();
        if (eliminados > 0) {
            log.debug("🧹 Limpiados {} lotes agotados del producto {}", eliminados, sku);
        }
        return eliminados;
    }

    /**
     * Obtiene estadísticas completas del producto.
     */
    public EstadisticasProducto getEstadisticas() {
        BigDecimal stockTotalAlmacen = getStockTotalAlmacen();
        BigDecimal stockDisponibleVenta = getStockDisponibleVenta();
        BigDecimal stockRobado = estanteria != null ? estanteria.getStockRobado() : BigDecimal.ZERO;
        BigDecimal stockMermado = estanteria != null ? estanteria.getStockMermado() : BigDecimal.ZERO;
        
        return new EstadisticasProducto(
            sku,
            nombre,
            categoria,
            precio,
            caduca,
            esGranel,
            stockTotalAlmacen,
            stockDisponibleVenta,
            stockRobado,
            stockMermado,
            lotes.size()
        );
    }

    @Override
    public String toString() {
        return "ProductoSimulado{" +
                "sku='" + sku + '\'' +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                ", precio=" + precio +
                ", caduca=" + caduca +
                ", esGranel=" + esGranel +
                ", stockAlmacen=" + getStockTotalAlmacen() +
                ", stockEstanteria=" + getStockDisponibleVenta() +
                '}';
    }

    /**
     * DTO para estadísticas de producto.
     */
    public record EstadisticasProducto(
        String sku,
        String nombre,
        String categoria,
        BigDecimal precio,
        boolean caduca,
        boolean esGranel,
        BigDecimal stockTotalAlmacen,
        BigDecimal stockDisponibleVenta,
        BigDecimal stockRobado,
        BigDecimal stockMermado,
        int cantidadLotes
    ) {}
}