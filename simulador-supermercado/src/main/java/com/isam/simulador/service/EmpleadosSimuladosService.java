package com.isam.simulador.service;

import com.isam.simulador.client.GrpcClientWithAuth;
import com.isam.simulador.model.EmpleadoSimulado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar los empleados simulados.
 * Inicializa los empleados y proporciona métodos para obtenerlos aleatoriamente.
 */
@Service
@Slf4j
public class EmpleadosSimuladosService {

    private final GrpcClientWithAuth grpcClientWithAuth; // Inyectar GrpcClientWithAuth

    private final List<EmpleadoSimulado> empleados = new ArrayList<>();
    private final Random random = new Random();

    // Constructor para inyección de dependencia
    public EmpleadosSimuladosService(GrpcClientWithAuth grpcClientWithAuth) {
        this.grpcClientWithAuth = grpcClientWithAuth;
    }

    /**
     * Inicializa los empleados simulados con el token de admin.
     */
    @PostConstruct
    public void inicializarEmpleados() {
        // Obtener token de admin
        String adminToken = grpcClientWithAuth.getAdminToken();
        
        // Crear empleados simulados con diferentes roles
        crearEmpleados(adminToken);
        
        log.info("✅ Empleados simulados inicializados: {} empleados activos", empleados.size());
    }
    
    /**
     * Crea los empleados simulados base del sistema.
     */
    private void crearEmpleados(String token) {
        // Cajeros
        EmpleadoSimulado emp1 = new EmpleadoSimulado("EMP-001", "María García", "CAJERO", true);
        emp1.setToken(token);
        empleados.add(emp1);
        
        EmpleadoSimulado emp2 = new EmpleadoSimulado("EMP-002", "Juan Pérez", "CAJERO", true);
        emp2.setToken(token);
        empleados.add(emp2);
        
        EmpleadoSimulado emp3 = new EmpleadoSimulado("EMP-003", "Ana Martínez", "CAJERO", true);
        emp3.setToken(token);
        empleados.add(emp3);
        
        EmpleadoSimulado emp4 = new EmpleadoSimulado("EMP-004", "Carlos López", "CAJERO", true);
        emp4.setToken(token);
        empleados.add(emp4);
        
        // Repositores
        EmpleadoSimulado emp5 = new EmpleadoSimulado("EMP-005", "Laura Sánchez", "REPOSITOR", true);
        emp5.setToken(token);
        empleados.add(emp5);
        
        EmpleadoSimulado emp6 = new EmpleadoSimulado("EMP-006", "Miguel Torres", "REPOSITOR", true);
        emp6.setToken(token);
        empleados.add(emp6);
        
        EmpleadoSimulado emp7 = new EmpleadoSimulado("EMP-007", "Isabel Ruiz", "REPOSITOR", true);
        emp7.setToken(token);
        empleados.add(emp7);
        
        // Supervisores
        EmpleadoSimulado emp8 = new EmpleadoSimulado("EMP-008", "Roberto Fernández", "SUPERVISOR", true);
        emp8.setToken(token);
        empleados.add(emp8);
        
        EmpleadoSimulado emp9 = new EmpleadoSimulado("EMP-009", "Carmen Díaz", "SUPERVISOR", true);
        emp9.setToken(token);
        empleados.add(emp9);
    }


    /**
     * Obtiene un empleado aleatorio de un rol específico.
     */
    public EmpleadoSimulado obtenerEmpleadoAleatorio(String rol) {
        List<EmpleadoSimulado> empleadosPorRol = empleados.stream()
            .filter(e -> e.getRol().equals(rol) && e.isEstaActivo())
            .collect(Collectors.toList());

        if (empleadosPorRol.isEmpty()) {
            log.warn("⚠️ No hay empleados activos con rol: {}", rol);
            return null;
        }

        EmpleadoSimulado empleado = empleadosPorRol.get(random.nextInt(empleadosPorRol.size()));
        empleado.registrarActividad();
        
        log.debug("🎲 Empleado aleatorio seleccionado: {} ({})", empleado.getNombre(), empleado.getRol());
        return empleado;
    }

    /**
     * Obtiene un cajero aleatorio.
     */
    public EmpleadoSimulado obtenerCajeroAleatorio() {
        return obtenerEmpleadoAleatorio("CAJERO");
    }

    /**
     * Obtiene un repositor aleatorio.
     */
    public EmpleadoSimulado obtenerRepositorAleatorio() {
        return obtenerEmpleadoAleatorio("REPOSITOR");
    }

    /**
     * Obtiene un supervisor aleatorio.
     */
    public EmpleadoSimulado obtenerSupervisorAleatorio() {
        return obtenerEmpleadoAleatorio("SUPERVISOR");
    }

    /**
     * Obtiene un empleado aleatorio de cualquier rol.
     */
    public EmpleadoSimulado obtenerEmpleadoAleatorio() {
        List<EmpleadoSimulado> empleadosActivos = empleados.stream()
            .filter(EmpleadoSimulado::isEstaActivo)
            .collect(Collectors.toList());

        if (empleadosActivos.isEmpty()) {
            log.warn("⚠️ No hay empleados activos");
            return null;
        }

        EmpleadoSimulado empleado = empleadosActivos.get(random.nextInt(empleadosActivos.size()));
        empleado.registrarActividad();
        
        log.debug("🎲 Empleado aleatorio seleccionado: {} ({})", empleado.getNombre(), empleado.getRol());
        return empleado;
    }

    /**
     * Obtiene todos los cajeros.
     */
    public List<EmpleadoSimulado> obtenerCajeros() {
        return empleados.stream()
            .filter(e -> e.getRol().equals("CAJERO") && e.isEstaActivo())
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los repositores.
     */
    public List<EmpleadoSimulado> obtenerRepositores() {
        return empleados.stream()
            .filter(e -> e.getRol().equals("REPOSITOR") && e.isEstaActivo())
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los supervisores.
     */
    public List<EmpleadoSimulado> obtenerSupervisores() {
        return empleados.stream()
            .filter(e -> e.getRol().equals("SUPERVISOR") && e.isEstaActivo())
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los empleados activos.
     */
    public List<EmpleadoSimulado> obtenerTodosLosEmpleados() {
        return empleados.stream()
            .filter(EmpleadoSimulado::isEstaActivo)
            .collect(Collectors.toList());
    }

    /**
     * Simula la desactivación aleatoria de empleados (baja por enfermedad, vacaciones, etc.).
     */
    public void simularCambiosDisponibilidad() {
        // 5% de probabilidad de que un empleado cambie su estado
        if (ThreadLocalRandom.current().nextDouble() < 0.05) {
            EmpleadoSimulado empleado = obtenerEmpleadoAleatorio();
            if (empleado != null) {
                boolean nuevoEstado = !empleado.isEstaActivo();
                empleado.setEstaActivo(nuevoEstado);
                
                if (nuevoEstado) {
                    log.info("👤 {} vuelve a estar activo", empleado.getNombre());
                } else {
                    log.info("🏥 {} se ha desactivado temporalmente", empleado.getNombre());
                }
            }
        }
    }

    /**
     * Obtiene estadísticas de los empleados.
     */
    public EstadisticasEmpleados getEstadisticas() {
        long totalActivos = empleados.stream().mapToLong(e -> e.isEstaActivo() ? 1 : 0).sum();
        long totalCajerosActivos = obtenerCajeros().size();
        long totalRepositoresActivos = obtenerRepositores().size();
        long totalSupervisoresActivos = obtenerSupervisores().size();

        return new EstadisticasEmpleados(
            empleados.size(),
            totalActivos,
            totalCajerosActivos,
            totalRepositoresActivos,
            totalSupervisoresActivos
        );
    }

    /**
     * DTO para estadísticas de empleados.
     */
    public record EstadisticasEmpleados(
        int totalEmpleados,
        long totalActivos,
        long totalCajerosActivos,
        long totalRepositoresActivos,
        long totalSupervisoresActivos
    ) {}
}