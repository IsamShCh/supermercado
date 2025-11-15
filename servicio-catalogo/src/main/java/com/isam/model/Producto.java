package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import org.hibernate.annotations.Check;

@Entity
@Getter
@Setter
@Table(name = "PRODUCTOS")
public class Producto {

    @Id
    @NotBlank
    @Size(max=50, message ="El SKU no puede exceder 50 caracteres")
    @Column(name = "SKU", length = 50, unique = true)
    private String sku;

    @Size(max=13, message ="El EAN no puede exceder 13 caracteres")
    @Column(name = "EAN", length = 13, unique = true)
    private String ean;

    @Size(max=5, message ="El PLU no puede exceder 5 caracteres")
    @Column(name = "PLU", length = 5, unique = true)
    private String plu;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 200, message ="El Nombre no puede exceder 200 caracteres")
    @Check(constraints = "trim(nombre) <> ''") // Esta restriccion generará una precondicion dentro del la base de datos. Por ende, el error que genere será a nivel de BBDD.
    @Column(name = "Nombre", length = 200, nullable = false)
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "Descripcion", length = 1000)
    private String descripcion;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value="0.0", inclusive = true, message = "El precio de venta no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El precio de venta debe tener máximo 10 dígitos enteros y 2 decimales")
    @Column(name = "PrecioVenta", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @NotNull(message = "El campo caduca es obligatorio")
    @Column(name = "Caduca", nullable = false)
    private Boolean caduca = false;

    @NotNull(message = "El campo es granel es obligatorio")
    @Column(name = "EsGranel", nullable = false)
    private Boolean esGranel = false;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCategoria")
    private Categoria categoria;

    @NotNull(message = "La política de rotación es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "PoliticaRotacion", nullable = false)
    private PoliticaRotacion politicaRotacion;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", nullable = false)
    private UnidadMedida unidadMedida;

    @Size(max = 500, message = "Las etiquetas no pueden exceder 500 caracteres")
    @Column(name = "Etiquetas", length = 500)
    private String etiquetas;

    @NotNull(message = "El estado es obligatorio")
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