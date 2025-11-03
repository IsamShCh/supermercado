package com.isam.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "CATEGORIAS")
public class Categoria {

    @Id
    @Column(name = "IDCategoria", length = 50)
    private String idCategoria;

    @Column(name = "NombreCategoria", length = 100, nullable = false)
    private String nombreCategoria;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos;

    // Constructors
    public Categoria() {}

    public Categoria(String idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    // Getters and Setters
    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}