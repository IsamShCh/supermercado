package com.isam.simulador.service;

import com.isam.simulador.client.InventarioClient;
import com.isam.simulador.model.EmpleadoSimulado;
import com.isam.simulador.service.EstadoSimulacion;
import com.isam.simulador.model.LoteSimulado;
import com.isam.simulador.model.Estanteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio que simula el comportamiento de empleados reponiendo estanterías.
 * Revisa estanterías y mueve stock de almacén siguiendo FIFO (con 5% de error).
 * También detecta y reporta robos y mermas acumulados al backend.
 */
@Service
@Slf4j
public class EmpleadoSimuladoService {

    private final InventarioClient inventarioClient;
    private final EstadoSimulacion estadoSimulacion;
    private final EmpleadosSimuladosService empleadosService;

    @Value("${simulador.probabilidad.error.fifo}")
    private double probabilidadErrorFifo;

    public EmpleadoSimuladoService(InventarioClient inventarioClient,
                                 EstadoSimulacion estadoSimulacion,
                                 EmpleadosSimuladosService empleadosService) {
        this.inventarioClient = inventarioClient;
        this.estadoSimulacion = estadoSimulacion;
        this.empleadosService = empleadosService;
    }

    /**
     * Simula la revisión de estanterías por parte de los empleados.
     * Cada empleado revisa sus estanterías asignadas y hace replenishment si es necesario.
     */
    @Scheduled(fixedRate = 15000, initialDelay = 10000)
    public void revisarEstanterias() {
        try {
            // 1. Obtener empleado repositor
            EmpleadoSimulado empleado = empleadosService.obtenerRepositorAleatorio();
            if (empleado == null) {
                log.warn("⚠️ No hay repositores disponibles para revisión");
                return;
            }

            log.info("👷 {} ({}) iniciando revisión de estanterías", 
                empleado.getNombre(), empleado.getRol());

            // 2. Revisar todas las estanterías que necesitan replenishment
            List<Estanteria> estanteriasNecesitadas = estadoSimulacion.obtenerEstanteriasQueNecesitanReplenishment();
            
            if (estanteriasNecesitadas.isEmpty()) {
                log.debug("📋 No hay estanterías que necesiten replenishment");
                return;
            }

            int estanteriasRevisadas = 0;
            int replenishmentsRealizados = 0;

            // 3. Procesar cada estantería que necesita replenishment
            for (Estanteria estanteria : estanteriasNecesitadas) {
                if (procesarReplenishmentEstanteria(estanteria, empleado)) {
                    replenishmentsRealizados++;
                }
                estanteriasRevisadas++;
            }

            // 4. Actualizar actividad del empleado
            empleado.registrarActividad();

            log.info("📦 {} completó revisión - Estanterías revisadas: {}, Replenishments: {}", 
                empleado.getNombre(), estanteriasRevisadas, replenishmentsRealizados);

        } catch (Exception e) {
            log.error("❌ Error en revisión de estanterías: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa el replenishment para una estantería específica.
     * FASE 1: Detecta y reporta robos/mermas acumulados al backend.
     * FASE 2: Mueve stock de almacén a estantería (backend PRIMERO, luego estado local).
     * @return true si se realizó el replenishment, false en caso contrario
     */
    private boolean procesarReplenishmentEstanteria(Estanteria estanteria, EmpleadoSimulado empleado) {
        try {
            // --- FASE 1: AUDITORÍA Y LIMPIEZA ---
            
            // 1. Gestionar Robos acumulados (detectados por el empleado)
            if (estanteria.getStockRobado().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cantidad = estanteria.getStockRobado();
                log.info("🚨 {} detectó {} robos en {}. Reportando...", empleado.getNombre(), cantidad, estanteria.getSku());
                
                try {
                    // Reportar al backend
                    inventarioClient.registrarRobo(estanteria.getSku(), cantidad.negate().toString(), null, empleado);
                    // Solo limpiar si el reporte al backend fue exitoso
                    estanteria.setStockRobado(BigDecimal.ZERO);
                } catch (Exception e) {
                    log.warn("⚠️ Error reportando robo para SKU: {}. Se reintentará en el próximo ciclo: {}", 
                        estanteria.getSku(), e.getMessage());
                }
            }

            // 2. Gestionar Mermas acumuladas (detectadas por el empleado)
            if (estanteria.getStockMermado().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cantidad = estanteria.getStockMermado();
                log.info("☣️ {} detectó {} mermas en {}. Reportando...", empleado.getNombre(), cantidad, estanteria.getSku());
                
                try {
                    // Reportar al backend
                    inventarioClient.registrarMerma(estanteria.getSku(), cantidad.negate().toString(), null, empleado);
                    // Solo limpiar si el reporte al backend fue exitoso
                    estanteria.setStockMermado(BigDecimal.ZERO);
                } catch (Exception e) {
                    log.warn("⚠️ Error reportando merma para SKU: {}. Se reintentará en el próximo ciclo: {}", 
                        estanteria.getSku(), e.getMessage());
                }
            }

            // --- FASE 2: REPOSICIÓN ---

            // 1. Verificar que la estantería realmente necesite replenishment
            if (!estanteria.necesitaReplenishment()) {
                log.debug("📋 Estantería {} no necesita replenishment ({}% disponible)", 
                    estanteria.getIdEstanteria(), estanteria.getPorcentajeDisponible().multiply(BigDecimal.valueOf(100)));
                return false;
            }

            // 2. Calcular cantidad a mover (para llenar al 80% de capacidad)
            BigDecimal cantidadMover = calcularCantidadMover(estanteria);
            if (cantidadMover.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("📋 No hay cantidad que mover para estantería {}", estanteria.getIdEstanteria());
                return false;
            }

            // 3. Seleccionar lote según estrategia (FIFO con posible error)
            LoteSimulado lote = seleccionarLoteParaReplenishment(estanteria.getSku());
            if (lote == null) {
                log.warn("⚠️ No hay lotes disponibles para SKU: {}", estanteria.getSku());
                return false;
            }

            // Limitar la cantidad a mover a lo que realmente tiene el lote
            cantidadMover = cantidadMover.min(lote.getCantidadAlmacen());

            // 4. Llamar al servicio real PRIMERO para mover el stock
            // Si falla, NO tocamos el estado local (evita desincronización)
            try {
                inventarioClient.moverStockEstanteria(
                    estanteria.getSku(),
                    lote.getIdLote(),
                    cantidadMover.toString(),
                    com.isam.grpc.common.UnidadMedida.UNIDAD,
                    empleado
                );
            } catch (Exception e) {
                log.warn("⚠️ Backend rechazó movimiento de stock para SKU: {}. Estado local no modificado: {}", 
                    estanteria.getSku(), e.getMessage());
                return false;
            }

            // 5. Backend exitoso → actualizar estado interno del simulador
            if (!estadoSimulacion.moverAlmacenAEstanteria(estanteria.getSku(), lote.getIdLote(), cantidadMover)) {
                log.warn("⚠️ Error actualizando estado interno para SKU: {} (backend ya procesó el movimiento)", 
                    estanteria.getSku());
            }

            log.info("📦 {} movió {} unidades de SKU: {} a estantería {} (Lote: {})", 
                empleado.getNombre(), cantidadMover, estanteria.getSku(), estanteria.getIdEstanteria(), lote.getIdLote());

            return true;

        } catch (Exception e) {
            log.error("❌ Error procesando replenishment para estantería {}: {}", 
                estanteria.getIdEstanteria(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Calcula la cantidad óptima a mover a estantería.
     * Objetivo: Llenar estantería al 80% de su capacidad.
     */
    private BigDecimal calcularCantidadMover(Estanteria estanteria) {
        BigDecimal capacidadMaxima = estanteria.getCapacidadMaxima();
        BigDecimal stockActual = estanteria.getStockTotal();
        BigDecimal objetivo80 = capacidadMaxima.multiply(new BigDecimal("0.80"));
        
        BigDecimal cantidadNecesaria = objetivo80.subtract(stockActual);
        
        // Limitar a un máximo razonable (no más del 50% de la capacidad)
        BigDecimal maximo50 = capacidadMaxima.multiply(new BigDecimal("0.50"));
        cantidadNecesaria = cantidadNecesaria.min(maximo50);
        
        // Redondear a múltiplos de 1 unidad
        return cantidadNecesaria.setScale(0, RoundingMode.UP);
    }

    /**
     * Selecciona el lote para replenishment.
     * 95% FIFO (el más antiguo), 5% error humano (lote aleatorio).
     */
    private LoteSimulado seleccionarLoteParaReplenishment(String sku) {
        boolean usarErrorHumano = ThreadLocalRandom.current().nextDouble() < probabilidadErrorFifo;
        
        if (usarErrorHumano) {
            log.debug("🎲 Simulando error humano - seleccionando lote aleatorio para SKU: {}", sku);
            return estadoSimulacion.seleccionarLoteAleatorio(sku);
        } else {
            log.debug("✅ Usando FIFO - seleccionando lote más antiguo para SKU: {}", sku);
            return estadoSimulacion.seleccionarLoteFIFO(sku);
        }
    }

    /**
     * Obtiene estadísticas de los replenishments realizados.
     */
    public EstadisticasReplenishment getEstadisticas() {
        EstadoSimulacion.EstadisticasGenerales stats = estadoSimulacion.getEstadisticasGenerales();
        
        return new EstadisticasReplenishment(
            stats.totalEmpleados(),
            stats.fechaInicio(),
            stats.fechaActual(),
            calcularEstanteriasConReplenishment(),
            calcularEficienciaReplenishment()
        );
    }

    /**
     * Calcula cuántas estanterías tienen menos del 20% de stock.
     */
    private int calcularEstanteriasConReplenishment() {
        return (int) estadoSimulacion.getAllEstanterias().stream()
            .filter(Estanteria::necesitaReplenishment)
            .count();
    }

    /**
     * Calcula la eficiencia del replenishment (simulado).
     */
    private String calcularEficienciaReplenishment() {
        int estanteriasCriticas = calcularEstanteriasConReplenishment();
        int totalEstanterias = estadoSimulacion.getAllEstanterias().size();
        
        if (totalEstanterias == 0) {
            return "N/A";
        }
        
        double porcentajeCriticas = (double) estanteriasCriticas / totalEstanterias * 100;
        
        if (porcentajeCriticas < 10) {
            return "Excelente";
        } else if (porcentajeCriticas < 20) {
            return "Buena";
        } else if (porcentajeCriticas < 30) {
            return "Regular";
        } else {
            return "Crítica";
        }
    }

    /**
     * DTO para estadísticas de replenishment.
     */
    public record EstadisticasReplenishment(
        int totalEmpleados,
        java.time.LocalDateTime fechaInicio,
        java.time.LocalDateTime fechaActual,
        int estanteriasConReplenishment,
        String eficienciaReplenishment
    ) {}
}