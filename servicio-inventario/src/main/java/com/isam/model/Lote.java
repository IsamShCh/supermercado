package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "LOTES")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDLote", length = 36)
    private String idLote;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(name = "SKU", length = 50, nullable = false)
    private String sku;

    @NotBlank(message = "El ID de inventario es obligatorio")
    @Column(name = "IDInventario", length = 36, nullable = false)
    private String idInventario;

    @Column(name = "EAN", length = 13)
    private String ean;

    @Column(name = "PLU", length = 5)
    private String plu;

    @NotBlank(message = "El número de lote es obligatorio")
    @Column(name = "NumeroLote", length = 50, nullable = false)
    private String numeroLote;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La cantidad no puede ser negativa")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    @Column(name = "Cantidad", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "FechaCaducidad")
    private LocalDate fechaCaducidad;

    @NotBlank(message = "El ID del proveedor es obligatorio")
    @Column(name = "IDProveedor", length = 36, nullable = false)
    private String idProveedor;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Column(name = "FechaIngreso", nullable = false)
    private LocalDate fechaIngreso;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", nullable = false)
    private UnidadMedida unidadMedida;

    @NotNull(message = "El estado del lote es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    // Constructors
    public Lote() {}

    public Lote(String sku, String idInventario, String numeroLote, BigDecimal cantidad,
                LocalDate fechaCaducidad, String idProveedor, LocalDate fechaIngreso,
                UnidadMedida unidadMedida) {
        this.sku = sku;
        this.idInventario = idInventario;
        this.numeroLote = numeroLote;
        this.cantidad = cantidad;
        this.fechaCaducidad = fechaCaducidad;
        this.idProveedor = idProveedor;
        this.fechaIngreso = fechaIngreso;
        this.unidadMedida = unidadMedida;
    }

    @Override
    public String toString() {
        return "Lote{" +
                "idLote='" + idLote + '\'' +
                ", sku='" + sku + '\'' +
                ", idInventario='" + idInventario + '\'' +
                ", numeroLote='" + numeroLote + '\'' +
                ", cantidad=" + cantidad +
                ", fechaCaducidad=" + fechaCaducidad +
                ", idProveedor='" + idProveedor + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", unidadMedida=" + unidadMedida +
                ", estado=" + estado +
                '}';
    }
}