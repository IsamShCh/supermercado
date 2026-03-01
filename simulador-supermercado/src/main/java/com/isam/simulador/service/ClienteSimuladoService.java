package com.isam.simulador.service;

import com.isam.simulador.client.VentasClient;
import com.isam.simulador.model.EmpleadoSimulado;
import com.isam.simulador.model.ProductoSimulado;
import com.isam.simulador.service.EstadoSimulacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio que simula el comportamiento de clientes comprando.
 * Genera ventas realistas con diferentes patrones de compra.
 */
@Service
@Slf4j
public class ClienteSimuladoService {

    private final VentasClient ventasClient;
    private final EstadoSimulacion estadoSimulacion;
    private final EmpleadosSimuladosService empleadosService;

    @Value("${simulador.probabilidad.venta.multiple}")
    private double probabilidadVentaMultiple;

    private final Random random = new Random();

    public ClienteSimuladoService(VentasClient ventasClient,
                                EstadoSimulacion estadoSimulacion,
                                EmpleadosSimuladosService empleadosService) {
        this.ventasClient = ventasClient;
        this.estadoSimulacion = estadoSimulacion;
        this.empleadosService = empleadosService;
    }

    /**
     * Simula una venta de cliente cada cierto tiempo.
     * El cliente compra 1-5 productos aleatorios.
     * Ejecuta cada 5 segundos (intervalo fijo) con un delay inicial
     * suficiente para asegurar que la inicialización de datos haya terminado.
     */
    @Scheduled(fixedRate = 5000, initialDelay = 15000)
    public void simularCompra() {
        try {
            // 1. Obtener cajero para procesar la venta
            EmpleadoSimulado cajero = empleadosService.obtenerCajeroAleatorio();
            if (cajero == null) {
                log.warn("⚠️ No hay cajeros disponibles para procesar venta");
                return;
            }

            // 2. Obtener productos disponibles en estantería (por SKU)
            List<String> skusDisponibles = estadoSimulacion.obtenerProductosConStockEstanteria();
            if (skusDisponibles.isEmpty()) {
                log.debug("⚠️ No hay productos con stock disponible en estantería para venta");
                return;
            }

            // 3. Seleccionar SKUs aleatorios para la compra
            List<String> skusAComprar = seleccionarProductosAleatorios(skusDisponibles);
            if (skusAComprar.isEmpty()) {
                return;
            }

            // 4. CORRECCIÓN CRÍTICA: Convertir SKUs a Códigos de Barras (EAN/PLU)
            // El servicio de ventas espera un código de barras, no un SKU.
            List<String> codigosBarrasParaVenta = new ArrayList<>();
            Map<String, ProductoSimulado> mapaProductos = estadoSimulacion.getProductos();

            // Lista auxiliar para saber qué SKUs se vendieron realmente (para actualizar stock interno)
            List<String> skusVendidosEfectivos = new ArrayList<>();

            for (String sku : skusAComprar) {
                ProductoSimulado p = mapaProductos.get(sku);
                if (p != null) {
                    String codigoBarras = null;
                    
                    if (p.isEsGranel() && p.getPlu() != null && !p.getPlu().isEmpty()) {
                        codigoBarras = p.getPlu();
                    } else if (p.getEan() != null && !p.getEan().isEmpty()) {
                        codigoBarras = p.getEan();
                    } else {
                        log.warn("⚠️ Producto {} no tiene EAN ni PLU válido para venta. Saltando...", sku);
                        continue;
                    }
                    
                    codigosBarrasParaVenta.add(codigoBarras);
                    skusVendidosEfectivos.add(sku);
                }
            }

            if (codigosBarrasParaVenta.isEmpty()) {
                log.warn("⚠️ No se pudieron obtener códigos de barras válidos para los productos seleccionados");
                return;
            }

            // 5. Simular la venta completa usando los códigos de barras
            String numeroTicket = ventasClient.simularVentaCompleta(
                codigosBarrasParaVenta, 
                VentasClient.obtenerMetodoPagoAleatorio(), 
                cajero
            );

            // 6. Actualizar estado interno del simulador (restar 1 unidad por item vendido)
            // Usamos la lista de SKUs efectivos que sí tenían código de barras
            for (String sku : skusVendidosEfectivos) {
                estadoSimulacion.venderProducto(sku, BigDecimal.ONE);
            }

            log.info("🛒 Venta simulada completada - Ticket: {}, Productos: {}",
                numeroTicket, skusVendidosEfectivos.size());

        } catch (Exception e) {
            log.error("❌ Error en simulación de compra: {}", e.getMessage());
        }
    }

    /**
     * Selecciona productos aleatorios para la compra.
     * Probabilidad configurable de compra múltiple.
     */
    private List<String> seleccionarProductosAleatorios(List<String> productosDisponibles) {
        List<String> seleccionados = new ArrayList<>();

        // Determinar cuántos productos comprar
        int cantidadProductos;
        if (ThreadLocalRandom.current().nextDouble() < probabilidadVentaMultiple) {
            // Compra múltiple (2-5 productos)
            cantidadProductos = ThreadLocalRandom.current().nextInt(4) + 2; // 2-5
            // No podemos comprar más productos de los que existen disponibles
            cantidadProductos = Math.min(cantidadProductos, productosDisponibles.size());
        } else {
            // Compra simple (1 producto)
            cantidadProductos = 1;
        }

        // Seleccionar productos aleatorios sin repetición del pool disponible
        List<String> poolSeleccion = new ArrayList<>(productosDisponibles);
        for (int i = 0; i < cantidadProductos && !poolSeleccion.isEmpty(); i++) {
            int indice = random.nextInt(poolSeleccion.size());
            String productoSeleccionado = poolSeleccion.remove(indice);
            seleccionados.add(productoSeleccionado);
        }

        return seleccionados;
    }

    /**
     * Simula diferentes patrones de compra según hora del día.
     * (Método placeholder para futura lógica más compleja)
     */
    private String obtenerPatronCompra() {
        int hora = java.time.LocalTime.now().getHour();
        
        if (hora >= 7 && hora < 10) {
            return "MAÑANA_RAPIDA";
        } else if (hora >= 12 && hora < 14) {
            return "ALMUERZO_PICO";
        } else if (hora >= 18 && hora < 21) {
            return "NOCHE_COMPRAS";
        } else {
            return "NORMAL";
        }
    }

    /**
     * Obtiene estadísticas de las ventas simuladas.
     */
    public EstadisticasVentas getEstadisticas() {
        EstadoSimulacion.EstadisticasGenerales stats = estadoSimulacion.getEstadisticasGenerales();
        
        return new EstadisticasVentas(
            stats.totalVentasRealizadas(),
            stats.fechaInicio(),
            stats.fechaActual(),
            obtenerPatronCompra(),
            "Datos simulados" // Placeholder
        );
    }

    /**
     * DTO para estadísticas de ventas.
     */
    public record EstadisticasVentas(
        long totalVentas,
        java.time.LocalDateTime fechaInicio,
        java.time.LocalDateTime fechaActual,
        String patronVentasPorHora,
        String productosMasVendidos
    ) {}
}