package com.isam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTOS_CACHE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCache {

    @Id
    @Column(name = "SKU", length = 50)
    private String sku;

    @Column(name = "Nombre", length = 200, nullable = false)
    private String nombre;
    
    @Column(name = "PrecioVenta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida")
    private UnidadMedida unidadMedida;

    @Column(name = "Categoria")
    private String categoria;

    @Column(name = "EAN", length = 13)
    private String ean;

    @Column(name = "PLU", length = 5)
    private String plu;

    @Column(name = "FechaActualizacion")
    private LocalDateTime fechaActualizacion;
}
