package com.isam.simulador.service;

import com.isam.simulador.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class EstadoSimulacion {

    // Estado principal
    private final Map<Long, CategoriaSimulada> categorias = new ConcurrentHashMap<>();
    private final Map<String, ProductoSimulado> productos = new ConcurrentHashMap<>();
    private final Map<String, Estanteria> estanteriasPorSku = new ConcurrentHashMap<>();
    private final Map<String, List<LoteSimulado>> lotesPorProducto = new ConcurrentHashMap<>();
    private final List<EmpleadoSimulado> empleados = new ArrayList<>();
    
    // Lista para guardar los UUIDs reales de los proveedores
    private final List<String> idsProveedores = new ArrayList<>();

    // Contadores
    private LocalDateTime fechaInicioSimulacion;
    private long totalVentasRealizadas = 0;
    private long totalRobosRegistrados = 0;
    private long totalMermasRegistradas = 0;

    public void inicializar() {
        this.fechaInicioSimulacion = LocalDateTime.now();
        log.info("🎮 Estado de simulación inicializado a las {}", fechaInicioSimulacion);
    }

    public synchronized void añadirCategoria(CategoriaSimulada categoria) {
        categorias.put(categoria.getIdCategoria(), categoria);
    }

    public synchronized void añadirProducto(ProductoSimulado producto) {
        productos.put(producto.getSku(), producto);
    }

    public synchronized void añadirEstanteria(Estanteria estanteria) {
        estanteriasPorSku.put(estanteria.getSku(), estanteria);
    }

    public synchronized void añadirLote(String sku, LoteSimulado lote) {
        lotesPorProducto.computeIfAbsent(sku, k -> new ArrayList<>()).add(lote);
    }

    public synchronized void añadirEmpleado(EmpleadoSimulado empleado) {
        empleados.add(empleado);
    }

    // --- MÉTODOS PARA PROVEEDORES ---
    public synchronized void añadirIdProveedor(String idProveedor) {
        this.idsProveedores.add(idProveedor);
    }

    public synchronized String obtenerIdProveedorAleatorio() {
        if (idsProveedores.isEmpty()) return null;
        return idsProveedores.get(new Random().nextInt(idsProveedores.size()));
    }

    public synchronized boolean venderProducto(String sku, BigDecimal cantidad) {
        Estanteria estanteria = estanteriasPorSku.get(sku);
        if (estanteria != null && estanteria.reducirStockDisponible(cantidad)) {
            totalVentasRealizadas++;
            return true;
        }
        return false;
    }

    public synchronized boolean moverAlmacenAEstanteria(String sku, String idLote, BigDecimal cantidad) {
        List<LoteSimulado> lotes = lotesPorProducto.get(sku);
        if (lotes == null) return false;

        LoteSimulado lote = lotes.stream()
            .filter(l -> l.getIdLote().equals(idLote) && l.isEstaDisponible())
            .findFirst().orElse(null);

        if (lote == null || !lote.reducirCantidad(cantidad)) return false;

        Estanteria estanteria = estanteriasPorSku.get(sku);
        if (estanteria != null) {
            estanteria.añadirStockDisponible(cantidad);
            return true;
        }
        return false;
    }

    public synchronized void registrarRobo(String sku, BigDecimal cantidad) {
        Estanteria estanteria = estanteriasPorSku.get(sku);
        if (estanteria != null) {
            estanteria.moverDisponibleARobado(cantidad);
            totalRobosRegistrados++;
        }
    }

    public synchronized void registrarMerma(String sku, BigDecimal cantidad) {
        Estanteria estanteria = estanteriasPorSku.get(sku);
        if (estanteria != null) {
            estanteria.moverDisponibleAMermado(cantidad);
            totalMermasRegistradas++;
        }
    }

    // --- Selectores de Lotes ---

    /**
     * Selecciona el lote más antiguo disponible para un SKU.
     */
    public synchronized LoteSimulado seleccionarLoteFIFO(String sku) {
        List<LoteSimulado> lotes = lotesPorProducto.get(sku);
        if (lotes == null || lotes.isEmpty()) return null;

        return lotes.stream()
            .filter(LoteSimulado::isEstaDisponible)
            .min(Comparator.comparing(LoteSimulado::getFechaIngreso))
            .orElse(null);
    }

    /**
     * Selecciona el lote más antiguo disponible que tenga al menos la cantidad mínima indicada.
     * Si no hay ningún lote con suficiente cantidad, cae al primer lote disponible (fallback).
     */
    public synchronized LoteSimulado seleccionarLoteFIFO(String sku, BigDecimal cantidadMinima) {
        List<LoteSimulado> lotes = lotesPorProducto.get(sku);
        if (lotes == null || lotes.isEmpty()) return null;

        // Intentar encontrar un lote con suficiente cantidad
        LoteSimulado loteConSuficiente = lotes.stream()
            .filter(LoteSimulado::isEstaDisponible)
            .filter(l -> l.getCantidadAlmacen().compareTo(cantidadMinima) >= 0)
            .min(Comparator.comparing(LoteSimulado::getFechaIngreso))
            .orElse(null);

        // Fallback: cualquier lote disponible
        if (loteConSuficiente != null) return loteConSuficiente;
        return seleccionarLoteFIFO(sku);
    }

    public synchronized LoteSimulado seleccionarLoteAleatorio(String sku) {
        List<LoteSimulado> lotes = lotesPorProducto.get(sku);
        if (lotes == null || lotes.isEmpty()) return null;

        List<LoteSimulado> disponibles = lotes.stream()
            .filter(LoteSimulado::isEstaDisponible)
            .toList();
        
        if (disponibles.isEmpty()) return null;
        return disponibles.get(new Random().nextInt(disponibles.size()));
    }

    public synchronized LoteSimulado seleccionarLoteCercanoCaducidad(String sku) {
        List<LoteSimulado> lotes = lotesPorProducto.get(sku);
        if (lotes == null || lotes.isEmpty()) return null;

        return lotes.stream()
            .filter(LoteSimulado::isEstaDisponible)
            .filter(LoteSimulado::estaCercanoCaducidad)
            .min(Comparator.comparing(LoteSimulado::getFechaCaducidad))
            .orElse(null);
    }

    // --- Limpieza de Lotes Agotados ---

    /**
     * Limpia lotes agotados (estaDisponible=false, cantidadAlmacen<=0) del estado de simulación.
     * Previene acumulación de memoria con lotes que ya no aportan nada.
     * @return Número total de lotes eliminados
     */
    public synchronized int limpiarLotesAgotados() {
        int totalEliminados = 0;
        for (Map.Entry<String, List<LoteSimulado>> entry : lotesPorProducto.entrySet()) {
            List<LoteSimulado> lotes = entry.getValue();
            int antes = lotes.size();
            lotes.removeIf(l -> !l.isEstaDisponible() && l.getCantidadAlmacen().compareTo(BigDecimal.ZERO) <= 0);
            totalEliminados += (antes - lotes.size());
        }
        return totalEliminados;
    }

    // --- Getters y Consultas ---

    public synchronized List<Estanteria> obtenerEstanteriasQueNecesitanReplenishment() {
        return estanteriasPorSku.values().stream().filter(Estanteria::necesitaReplenishment).toList();
    }

    public synchronized List<String> obtenerProductosConStockEstanteria() {
        return estanteriasPorSku.values().stream()
            .filter(e -> e.getStockDisponible().compareTo(BigDecimal.ZERO) > 0)
            .map(Estanteria::getSku).toList();
    }

    public synchronized ProductoSimulado.EstadisticasProducto getEstadisticasProducto(String sku) {
        ProductoSimulado p = productos.get(sku);
        return p != null ? p.getEstadisticas() : null;
    }

    public synchronized Estanteria.EstadisticasEstanteria getEstadisticasEstanteria(String sku) {
        Estanteria e = estanteriasPorSku.get(sku);
        return e != null ? e.getEstadisticas() : null;
    }

    public synchronized Map<Long, CategoriaSimulada> getCategorias() { return new HashMap<>(categorias); }
    public synchronized Map<String, ProductoSimulado> getProductos() { return new HashMap<>(productos); }
    public synchronized Map<String, Estanteria> getEstanterias() { return new HashMap<>(estanteriasPorSku); }
    public synchronized List<EmpleadoSimulado> getEmpleados() { return new ArrayList<>(empleados); }
    public synchronized List<Estanteria> getAllEstanterias() { return new ArrayList<>(estanteriasPorSku.values()); }

    public synchronized EstadisticasGenerales getEstadisticasGenerales() {
        BigDecimal stockAlmacen = lotesPorProducto.values().stream().flatMap(List::stream)
            .filter(LoteSimulado::isEstaDisponible).map(LoteSimulado::getCantidadAlmacen)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal stockEstanteria = estanteriasPorSku.values().stream()
            .map(Estanteria::getStockTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new EstadisticasGenerales(
            fechaInicioSimulacion, LocalDateTime.now(),
            categorias.size(), productos.size(), estanteriasPorSku.size(), empleados.size(),
            totalVentasRealizadas, totalRobosRegistrados, totalMermasRegistradas,
            stockAlmacen, stockEstanteria
        );
    }

    public record EstadisticasGenerales(
        LocalDateTime fechaInicio, LocalDateTime fechaActual,
        int totalCategorias, int totalProductos, int totalEstanterias, int totalEmpleados,
        long totalVentasRealizadas, long totalRobosRegistrados, long totalMermasRegistradas,
        BigDecimal stockTotalAlmacen, BigDecimal stockTotalEstanteria
    ) {}
}