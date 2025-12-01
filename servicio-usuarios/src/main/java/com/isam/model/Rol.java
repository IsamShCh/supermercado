package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Representa un rol en el sistema de autenticación.
 * Define los permisos y capacidades que puede tener un usuario.
 */
@Entity
@Getter
@Setter
@Table(name = "ROLES")
public class Rol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDRol", length = 36)
    private String idRol;
    
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del rol debe tener entre 3 y 100 caracteres")
    @Column(name = "NombreRol", length = 100, nullable = false, unique = true)
    private String nombreRol;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "DescripcionRol", length = 500)
    private String descripcionRol;
    
    // Relaciones
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ROL_PERMISOS",
        joinColumns = @JoinColumn(name = "IDRol"),
        inverseJoinColumns = @JoinColumn(name = "IDPermiso")
    )
    private List<Permiso> permisos;
    
    @ManyToMany(mappedBy = "roles")
    private List<Usuario> usuarios;
    
    // Constructors
    public Rol() {}
    
    public Rol(String nombreRol, String descripcionRol) {
        this.nombreRol = nombreRol;
        this.descripcionRol = descripcionRol;
    }
    
    @Override
    public String toString() {
        return "Rol{" +
                "idRol='" + idRol + '\'' +
                ", nombreRol='" + nombreRol + '\'' +
                ", descripcionRol='" + descripcionRol + '\'' +
                '}';
    }
}