package com.isam.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entidad que actúa como caché local de productos.
 * Almacena información básica replicada desde el servicio de catálogo
 * para permitir que el servicio de inventario sea autónomo.
 */
@Entity
@Table(name = "PRODUCTO_CACHE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCache {

    @Id
    @Column(name = "SKU", length = 50)
    private String sku;

    @Column(name = "Nombre", length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", length = 20)
    private UnidadMedida unidadMedida;

    @Column(name = "EAN", length = 13)
    private String ean;

    @Column(name = "PLU", length = 10)
    private String plu;

    @Column(name = "FechaActualizacion")
    private LocalDateTime fechaActualizacion;

    public ProductoCache(String sku, String nombre, UnidadMedida unidadMedida) {
        this.sku = sku;
        this.nombre = nombre;
        this.unidadMedida = unidadMedida;
        this.fechaActualizacion = LocalDateTime.now();
    }
}
