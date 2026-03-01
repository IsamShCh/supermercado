package com.isam.simulador.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Representa una categoría simulada del catálogo.
 */
@Getter
@Setter
@Slf4j
public class CategoriaSimulada {
    private Long idCategoria;
    private String nombreCategoria;
    private String descripcion;
    
    public CategoriaSimulada() {}
    
    public CategoriaSimulada(Long idCategoria, String nombreCategoria, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "CategoriaSimulada{" +
                "idCategoria=" + idCategoria +
                ", nombreCategoria='" + nombreCategoria + '\'' +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}