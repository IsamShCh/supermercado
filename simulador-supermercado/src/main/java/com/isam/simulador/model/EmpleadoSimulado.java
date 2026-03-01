package com.isam.simulador.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Representa un empleado simulado del supermercado.
 * Cada empleado tiene un token JWT (que será el del admin) para autenticación.
 */
@Getter
@Setter
@Slf4j
public class EmpleadoSimulado {
    private String idEmpleado;
    private String nombre;
    private String rol; // "CAJERO", "REPOSITOR", "SUPERVISOR"
    private boolean estaActivo;
    private LocalDateTime ultimaActividad;
    private String token; // Token JWT (será el de admin inyectado)

    public EmpleadoSimulado(String idEmpleado, String nombre, String rol, boolean estaActivo) {
        this.idEmpleado = idEmpleado;
        this.nombre = nombre;
        this.rol = rol;
        this.estaActivo = estaActivo;
        this.ultimaActividad = LocalDateTime.now();
        // El token se asignará después de la inicialización
    }

    public void registrarActividad() {
        this.ultimaActividad = LocalDateTime.now();
        log.debug("Empleado {} ({}) registró actividad a las {}", 
            nombre, rol, ultimaActividad);
    }

    @Override
    public String toString() {
        return "EmpleadoSimulado{" +
                "idEmpleado='" + idEmpleado + '\'' +
                ", nombre='" + nombre + '\'' +
                ", rol='" + rol + '\'' +
                ", estaActivo=" + estaActivo +
                '}';
    }
}