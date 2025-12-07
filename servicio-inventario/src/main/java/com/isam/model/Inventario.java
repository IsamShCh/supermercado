package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "INVENTARIO")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDInventario", length = 36)
    private String idInventario;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(name = "SKU", length = 50, nullable = false)
    private String sku;

    @Column(name = "EAN", length = 13)
    private String ean;

    @Column(name = "PLU", length = 5)
    private String plu;

    @NotNull(message = "La cantidad en almacén es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La cantidad en almacén no puede ser negativa")
    @Digits(integer = 10, fraction = 3, message = "La cantidad en almacén debe tener máximo 10 dígitos enteros y 3 decimales")
    @Column(name = "CantidadAlmacen", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidadAlmacen = BigDecimal.ZERO;

    @NotNull(message = "La cantidad en estantería es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La cantidad en estantería no puede ser negativa")
    @Digits(integer = 10, fraction = 3, message = "La cantidad en estantería debe tener máximo 10 dígitos enteros y 3 decimales")
    @Column(name = "CantidadEstanteria", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidadEstanteria = BigDecimal.ZERO;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "EsProvisional", nullable = false)
    private Boolean esProvisional = false;

    // Constructors
    public Inventario() {}

    public Inventario(String sku, BigDecimal cantidadAlmacen, BigDecimal cantidadEstanteria, UnidadMedida unidadMedida) {
        this.sku = sku;
        this.cantidadAlmacen = cantidadAlmacen;
        this.cantidadEstanteria = cantidadEstanteria;
        this.unidadMedida = unidadMedida;
    }

    public Inventario(String sku, String ean, String plu, UnidadMedida unidadMedida) {
        this.sku = sku;
        this.ean = ean;
        this.plu = plu;
        this.cantidadAlmacen = BigDecimal.ZERO;
        this.cantidadEstanteria = BigDecimal.ZERO;
        this.unidadMedida = unidadMedida;
    }

    @Override
    public String toString() {
        return "Inventario{" +
                "idInventario='" + idInventario + '\'' +
                ", sku='" + sku + '\'' +
                ", ean='" + ean + '\'' +
                ", plu='" + plu + '\'' +
                ", cantidadAlmacen=" + cantidadAlmacen +
                ", cantidadEstanteria=" + cantidadEstanteria +
                ", unidadMedida=" + unidadMedida +
                '}';
    }
}