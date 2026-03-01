package com.isam.simulador.service;

import com.isam.simulador.client.InventarioClient;
import com.isam.simulador.model.EmpleadoSimulado;
import com.isam.simulador.service.EstadoSimulacion;
import com.isam.simulador.model.Estanteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio que simula eventos aleatorios en el supermercado.
 * Genera robos y mermas de productos de forma realista.
 * 
 * IMPORTANTE: Tanto robos como mermas son eventos "físicos" que ocurren en la estantería.
 * No se reportan al backend directamente. Los empleados los detectan durante la revisión
 * de estanterías y entonces reportan la discrepancia al backend.
 */
@Service
@Slf4j
public class EventosAleatoriosService {

    private final InventarioClient inventarioClient;
    private final EstadoSimulacion estadoSimulacion;
    private final EmpleadosSimuladosService empleadosService;

    public EventosAleatoriosService(InventarioClient inventarioClient,
                                  EstadoSimulacion estadoSimulacion,
                                  EmpleadosSimuladosService empleadosService) {
        this.inventarioClient = inventarioClient;
        this.estadoSimulacion = estadoSimulacion;
        this.empleadosService = empleadosService;
    }

    /**
     * Simula un robo aleatorio en el supermercado.
     * El robo es un evento "físico" que solo modifica el estado local de la estantería.
     * Un empleado lo detectará durante la revisión y reportará al backend.
     * Ejecuta cada 30 segundos.
     */
    @Scheduled(fixedRate = 30000, initialDelay = 15000)
    public void simularRoboAleatorio() {
        try {
            EmpleadoSimulado empleado = seleccionarEmpleadoParaRobo();
            if (empleado == null) return;

            // 1. Seleccionar un SKU que tenga stock físico en la estantería
            String sku = seleccionarSkuParaEvento();
            if (sku == null) {
                log.debug("📋 No hay productos en estantería para robar");
                return;
            }

            // 2. Calcular cantidad a robar
            BigDecimal cantidad = calcularCantidadEvento(sku);
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) return;

            // 3. Actualizar estado interno del simulador (evento físico, sin detectar aún)
            estadoSimulacion.registrarRobo(sku, cantidad);

            // NOTA: No se llama al backend aquí. El robo es un evento "físico" que queda
            // pendiente hasta que un empleado lo detecte durante la revisión de estanterías.
            // El empleado reportará al backend cuando encuentre la discrepancia.
            // (Mismo patrón que las mermas para evitar doble-reporte)

            log.info("🕵️ Robo físico ocurrido (sin detectar) - SKU: {}, Cantidad: {}", sku, cantidad);

        } catch (Exception e) {
            log.error("❌ Error en simulación de robo: {}", e.getMessage());
        }
    }

    /**
     * Simula una merma aleatoria en el supermercado.
     * La merma es un evento "físico" que solo modifica el estado local de la estantería.
     * Un empleado la detectará durante la revisión y reportará al backend.
     * Ejecuta cada 45 segundos.
     */
    @Scheduled(fixedRate = 45000, initialDelay = 20000)
    public void simularMermaAleatoria() {
        try {
            EmpleadoSimulado empleado = seleccionarEmpleadoParaMerma();
            if (empleado == null) return;

            // 1. Seleccionar un SKU que tenga stock físico en la estantería
            String sku = seleccionarSkuParaEvento();
            if (sku == null) {
                log.debug("📋 No hay productos en estantería para merma");
                return;
            }

            // 2. Calcular cantidad a mermar
            BigDecimal cantidad = calcularCantidadEvento(sku);
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) return;

            // 3. Actualizar estado interno del simulador (evento físico, sin detectar aún)
            estadoSimulacion.registrarMerma(sku, cantidad);

            log.info("🤢 Merma física ocurrida (sin detectar) - SKU: {}, Cantidad: {}", sku, cantidad);

        } catch (Exception e) {
            log.error("❌ Error en simulación de merma: {}", e.getMessage());
        }
    }

    // --- Métodos Auxiliares ---

    private String seleccionarSkuParaEvento() {
        // Solo robamos de productos que tengan stock real en la estantería del simulador
        List<String> skus = estadoSimulacion.obtenerProductosConStockEstanteria();
        if (skus.isEmpty()) return null;
        return skus.get(ThreadLocalRandom.current().nextInt(skus.size()));
    }

    private BigDecimal calcularCantidadEvento(String sku) {
        Estanteria.EstadisticasEstanteria stats = estadoSimulacion.getEstadisticasEstanteria(sku);
        if (stats == null || stats.stockDisponible().compareTo(BigDecimal.ONE) < 0) return BigDecimal.ZERO;

        // Robar/Mermar entre 1 y 3 unidades máximo, o lo que haya disponible
        int max = Math.min(3, stats.stockDisponible().intValue());
        if (max < 1) return BigDecimal.ZERO;
        
        return new BigDecimal(ThreadLocalRandom.current().nextInt(max) + 1);
    }

    private EmpleadoSimulado seleccionarEmpleadoParaRobo() {
        if (ThreadLocalRandom.current().nextDouble() < 0.7) {
            return empleadosService.obtenerSupervisorAleatorio();
        } else {
            return empleadosService.obtenerRepositorAleatorio();
        }
    }

    private EmpleadoSimulado seleccionarEmpleadoParaMerma() {
        if (ThreadLocalRandom.current().nextDouble() < 0.8) {
            return empleadosService.obtenerRepositorAleatorio();
        } else {
            return empleadosService.obtenerSupervisorAleatorio();
        }
    }

    public EstadisticasEventosAleatorios getEstadisticas() {
        EstadoSimulacion.EstadisticasGenerales stats = estadoSimulacion.getEstadisticasGenerales();
        return new EstadisticasEventosAleatorios(
            stats.fechaInicio(),
            stats.fechaActual(),
            stats.totalRobosRegistrados(),
            stats.totalMermasRegistradas(),
            "N/A"
        );
    }

    public record EstadisticasEventosAleatorios(
        java.time.LocalDateTime fechaInicio,
        java.time.LocalDateTime fechaActual,
        long totalRobos,
        long totalMermas,
        String tasaPerdida
    ) {}
}