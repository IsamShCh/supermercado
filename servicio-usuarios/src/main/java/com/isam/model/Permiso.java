package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Representa un permiso en el sistema de autenticación.
 * Define una acción específica que se puede realizar sobre un recurso.
 */
@Entity
@Getter
@Setter
@Table(name = "PERMISOS")
public class Permiso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDPermiso", length = 36)
    private String idPermiso;
    
    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(max = 100, message = "El nombre del permiso no puede exceder 100 caracteres")
    @Column(name = "NombrePermiso", length = 100, nullable = false, unique = true)
    private String nombrePermiso;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "Descripcion", length = 500)
    private String descripcion;
    
    @NotBlank(message = "El recurso es obligatorio")
    @Size(max = 100, message = "El recurso no puede exceder 100 caracteres")
    @Column(name = "Recurso", length = 100, nullable = false)
    private String recurso;
    
    @NotNull(message = "La acción es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "Accion", nullable = false)
    private AccionPermiso accion;
    
    // Relaciones
    @ManyToMany(mappedBy = "permisos")
    private List<Rol> roles;
    
    // Constructors
    public Permiso() {}
    
    public Permiso(String nombrePermiso, String descripcion, String recurso, AccionPermiso accion) {
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
        this.recurso = recurso;
        this.accion = accion;
    }
    
    @Override
    public String toString() {
        return "Permiso{" +
                "idPermiso='" + idPermiso + '\'' +
                ", nombrePermiso='" + nombrePermiso + '\'' +
                ", recurso='" + recurso + '\'' +
                ", accion=" + accion +
                '}';
    }
}