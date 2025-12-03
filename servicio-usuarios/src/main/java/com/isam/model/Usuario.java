package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.isam.model.enums.EstadoUsuario;

/**
 * Representa un usuario en el sistema de autenticación.
 * Contiene información básica del usuario y sus credenciales de acceso.
 */
@Entity
@Getter
@Setter
@Table(name = "USUARIOS")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDUsuario", length = 36)
    private String idUsuario;
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Column(name = "NombreUsuario", length = 50, unique = true, nullable = false)
    private String nombreUsuario;
    
    @NotBlank(message = "El hash de la contraseña es obligatorio")
    @Column(name = "HashContrasena", nullable = false)
    private String hashContrasena;
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 200, message = "El nombre completo no puede exceder 200 caracteres")
    @Column(name = "NombreCompleto", length = 200, nullable = false)
    private String nombreCompleto;
    
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;
    
    @NotNull(message = "La fecha de creación es obligatoria")
    @Column(name = "FechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "FechaUltimoAcceso")
    private LocalDateTime fechaUltimoAcceso;
    
    @NotNull(message = "El flag de cambio de contraseña es obligatorio")
    @Column(name = "RequiereCambioContrasena", nullable = false)
    private Boolean requiereCambioContrasena = false;
    
    // Relaciones
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "USUARIO_ROLES",
        joinColumns = @JoinColumn(name = "IDUsuario"),
        inverseJoinColumns = @JoinColumn(name = "IDRol")
    )
    private List<Rol> roles;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Sesion> sesiones;
    
    // Constructors
    public Usuario() {}
    
    public Usuario(String nombreUsuario, String hashContrasena, String nombreCompleto) {
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.nombreCompleto = nombreCompleto;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario='" + idUsuario + '\'' +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", estado=" + estado +
                '}';
    }
}