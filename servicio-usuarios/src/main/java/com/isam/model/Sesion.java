package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.isam.model.enums.EstadoSesion;

/**
 * Representa una sesión de usuario en el sistema.
 * Almacena información sobre el login y logout de usuarios.
 */
@Entity
@Getter
@Setter
@Table(name = "SESIONES")
public class Sesion {
    
    @Id
    @Column(name = "TokenJWT", length = 500)
    private String tokenJWT;
    
    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUsuario", nullable = false)
    private Usuario usuario;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "FechaHoraInicio", nullable = false)
    private LocalDateTime fechaHoraInicio;
    
    @Column(name = "FechaHoraFin")
    private LocalDateTime fechaHoraFin;
    
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoSesion estado = EstadoSesion.ACTIVA;
    
    // Constructors
    public Sesion() {}
    
    public Sesion(String tokenJWT, Usuario usuario) {
        this.tokenJWT = tokenJWT;
        this.usuario = usuario;
        this.fechaHoraInicio = LocalDateTime.now();
        this.estado = EstadoSesion.ACTIVA;
    }
    
    @Override
    public String toString() {
        return "Sesion{" +
                "tokenJWT='" + tokenJWT + '\'' +
                ", usuario=" + (usuario != null ? usuario.getNombreUsuario() : "null") +
                ", estado=" + estado +
                '}';
    }
}