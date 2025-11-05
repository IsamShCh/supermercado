package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "PRODUCTOS")
public class Producto {

    @Id
    @Column(name = "SKU", length = 50, unique = true)
    private String sku;

    @Column(name = "EAN", length = 13, unique = true)
    private String ean;

    @Column(name = "PLU", length = 5, unique = true)
    private String plu;

    @Column(name = "Nombre", length = 200, nullable = false)
    private String nombre;

    @Column(name = "Descripcion", length = 1000)
    private String descripcion;

    @Column(name = "PrecioVenta", precision = 10, scale = 2, nullable = false)
    @PositiveOrZero
    private BigDecimal precioVenta;

    @Column(name = "Caduca", nullable = false)
    private Boolean caduca = false;

    @Column(name = "EsGranel", nullable = false)
    private Boolean esGranel = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCategoria")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "PoliticaRotacion", nullable = false)
    private PoliticaRotacion politicaRotacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "Etiquetas", length = 500)
    private String etiquetas;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    // Constructors
    public Producto() {}

    public Producto(String sku, String nombre, BigDecimal precioVenta, Categoria categoria,
                    PoliticaRotacion politicaRotacion, UnidadMedida unidadMedida) {
        this.sku = sku;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.categoria = categoria;
        this.politicaRotacion = politicaRotacion;
        this.unidadMedida = unidadMedida;
    }

    @Override
    public String toString() {
        return "ProductoEntity{" +
                "sku='" + sku + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precioVenta=" + precioVenta +
                ", estado=" + estado +
                '}';
    }
}