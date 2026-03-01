
package com.isam.simulador.controller;

import com.isam.simulador.service.*;
import com.isam.simulador.model.CategoriaSimulada;
import com.isam.simulador.model.EmpleadoSimulado;
import com.isam.simulador.model.Estanteria;
import com.isam.simulador.model.ProductoSimulado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para exponer estadísticas del simulador.
 * Permite monitorear el estado del simulador en tiempo real.
 */
@RestController
@RequestMapping("/api/simulador")
@Slf4j
public class SimuladorController {

    @Autowired
    private EstadoSimulacion estadoSimulacion;

    @Autowired
    private ClienteSimuladoService clienteSimuladoService;

    @Autowired
    private EmpleadoSimuladoService empleadoSimuladoService;

    @Autowired
    private EventosAleatoriosService eventosAleatoriosService;

    /**
     * Obtiene estadísticas generales del simulador.
     */
    @GetMapping("/estadisticas/generales")
    public EstadoSimulacion.EstadisticasGenerales obtenerEstadisticasGenerales() {
        return estadoSimulacion.getEstadisticasGenerales();
    }

    /**
     * Obtiene estadísticas de ventas.
     */
    @GetMapping("/estadisticas/ventas")
    public ClienteSimuladoService.EstadisticasVentas obtenerEstadisticasVentas() {
        return clienteSimuladoService.getEstadisticas();
    }

    /**
     * Obtiene estadísticas de replenishment.
     */
    @GetMapping("/estadisticas/replenishment")
    public EmpleadoSimuladoService.EstadisticasReplenishment obtenerEstadisticasReplenishment() {
        return empleadoSimuladoService.getEstadisticas();
    }

    /**
     * Obtiene estadísticas de eventos aleatorios.
     */
    @GetMapping("/estadisticas/eventos")
    public EventosAleatoriosService.EstadisticasEventosAleatorios obtenerEstadisticasEventos() {
        return eventosAleatoriosService.getEstadisticas();
    }

    /**
     * Obtiene estadísticas de un producto específico.
     */
    @GetMapping("/estadisticas/producto/{sku}")
    public ProductoSimulado.EstadisticasProducto obtenerEstadisticasProducto(String sku) {
        return estadoSimulacion.getEstadisticasProducto(sku);
    }

    /**
     * Obtiene estadísticas de una estantería específica.
     */
    @GetMapping("/estadisticas/estanteria/{sku}")
    public Estanteria.EstadisticasEstanteria obtenerEstadisticasEstanteria(String sku) {
        return estadoSimulacion.getEstadisticasEstanteria(sku);
    }

    /**
     * Obtiene resumen completo del estado del simulador.
     */
    @GetMapping("/resumen")
    public ResumenSimulador obtenerResumenCompleto() {
        return new ResumenSimulador(
            estadoSimulacion.getEstadisticasGenerales(),
            clienteSimuladoService.getEstadisticas(),
            empleadoSimuladoService.getEstadisticas(),
            eventosAleatoriosService.getEstadisticas(),
            estadoSimulacion.getProductos().size(),
            estadoSimulacion.getAllEstanterias().size()
        );
    }

    /**
     * Obtiene todas las categorías del simulador.
     */
    @GetMapping("/categorias")
    public java.util.Map<Long, CategoriaSimulada> obtenerCategorias() {
        return estadoSimulacion.getCategorias();
    }

    /**
     * Obtiene todos los productos del simulador.
     */
    @GetMapping("/productos")
    public java.util.Map<String, ProductoSimulado> obtenerProductos() {
        return estadoSimulacion.getProductos();
    }

    /**
     * Obtiene todas las estanterías del simulador.
     */
    @GetMapping("/estanterias")
    public java.util.List<Estanteria> obtenerEstanterias() {
        return estadoSimulacion.getAllEstanterias();
    }

    /**
     * Obtiene todos los empleados del simulador.
     */
    @GetMapping("/empleados")
    public java.util.List<com.isam.simulador.model.EmpleadoSimulado> obtenerEmpleados() {
        return estadoSimulacion.getEmpleados();
    }

    /**
     * DTO para resumen completo del simulador.
     */
    public record ResumenSimulador(
        EstadoSimulacion.EstadisticasGenerales generales,
        ClienteSimuladoService.EstadisticasVentas ventas,
        EmpleadoSimuladoService.EstadisticasReplenishment replenishment,
        EventosAleatoriosService.EstadisticasEventosAleatorios eventos,
        int totalProductos,
        int totalEstanterias
    ) {}
}