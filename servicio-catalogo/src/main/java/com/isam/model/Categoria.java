package com.isam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "CATEGORIAS")
@Getter
@Setter
public class Categoria {

    @Id
    @Column(name = "IDCategoria")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idCategoria;

    @Column(name = "NombreCategoria", length = 100, nullable = false, unique = true)
    private String nombreCategoria;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productoEntities;

    // Constructors
    public Categoria() {}

    public Categoria(long idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

}