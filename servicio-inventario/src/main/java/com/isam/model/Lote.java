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

    @NotBlank(message = "El número de lote es obligatorio")
    @Column(name = "NumeroLote", length = 50, nullable = false)
    private String numeroLote;

    @NotNull(message = "La cantidad de entrada es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La cantidad de entrada no puede ser negativa")
    @Digits(integer = 10, fraction = 3, message = "La cantidad de entrada debe tener máximo 10 dígitos enteros y 3 decimales")
    @Column(name = "CantidadEntrada", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidadEntrada;

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

    public Lote(String sku, String idInventario, String numeroLote, BigDecimal cantidadEntrada,
                LocalDate fechaCaducidad, String idProveedor, LocalDate fechaIngreso,
                UnidadMedida unidadMedida) {
        this.sku = sku;
        this.idInventario = idInventario;
        this.numeroLote = numeroLote;
        this.cantidadEntrada = cantidadEntrada;
        this.cantidadAlmacen = cantidadEntrada;  // Por defecto todo va al almacén
        this.cantidadEstanteria = BigDecimal.ZERO;
        this.fechaCaducidad = fechaCaducidad;
        this.idProveedor = idProveedor;
        this.fechaIngreso = fechaIngreso;
        this.unidadMedida = unidadMedida;
    }

    public Lote(String sku, String idInventario, String numeroLote, BigDecimal cantidadEntrada,
                BigDecimal cantidadAlmacen, BigDecimal cantidadEstanteria, LocalDate fechaCaducidad,
                String idProveedor, LocalDate fechaIngreso, UnidadMedida unidadMedida) {
        this.sku = sku;
        this.idInventario = idInventario;
        this.numeroLote = numeroLote;
        this.cantidadEntrada = cantidadEntrada;
        this.cantidadAlmacen = cantidadAlmacen;
        this.cantidadEstanteria = cantidadEstanteria;
        this.fechaCaducidad = fechaCaducidad;
        this.idProveedor = idProveedor;
        this.fechaIngreso = fechaIngreso;
        this.unidadMedida = unidadMedida;
    }

    // Métodos helper para gestión de cantidades
    public BigDecimal getCantidadTotal() {
        return cantidadAlmacen.add(cantidadEstanteria);
    }

    public void moverAEstanteria(BigDecimal cantidadMover) {
        if (cantidadAlmacen.compareTo(cantidadMover) < 0) {
            throw new IllegalArgumentException("No hay suficiente stock en almacén para mover a estantería");
        }
        this.cantidadAlmacen = this.cantidadAlmacen.subtract(cantidadMover);
        this.cantidadEstanteria = this.cantidadEstanteria.add(cantidadMover);
    }

    public void moverDesdeEstanteria(BigDecimal cantidadMover) {
        if (cantidadEstanteria.compareTo(cantidadMover) < 0) {
            throw new IllegalArgumentException("No hay suficiente stock en estantería para mover");
        }
        this.cantidadEstanteria = this.cantidadEstanteria.subtract(cantidadMover);
        this.cantidadAlmacen = this.cantidadAlmacen.add(cantidadMover);
    }

    public void descontarVenta(BigDecimal cantidadVender) {
        if (cantidadEstanteria.compareTo(cantidadVender) < 0) {
            throw new IllegalArgumentException("No hay suficiente stock en estantería para la venta");
        }
        this.cantidadEstanteria = this.cantidadEstanteria.subtract(cantidadVender);
    }

    public void devolverVenta(BigDecimal cantidadDevolver) {
        this.cantidadEstanteria = this.cantidadEstanteria.add(cantidadDevolver);
    }

    @Override
    public String toString() {
        return "Lote{" +
                "idLote='" + idLote + '\'' +
                ", sku='" + sku + '\'' +
                ", idInventario='" + idInventario + '\'' +
                ", numeroLote='" + numeroLote + '\'' +
                ", cantidadEntrada=" + cantidadEntrada +
                ", cantidadAlmacen=" + cantidadAlmacen +
                ", cantidadEstanteria=" + cantidadEstanteria +
                ", cantidadTotal=" + getCantidadTotal() +
                ", fechaCaducidad=" + fechaCaducidad +
                ", idProveedor='" + idProveedor + '\'' +
                ", fechaIngreso=" + fechaIngreso +
                ", unidadMedida=" + unidadMedida +
                ", estado=" + estado +
                '}';
    }
}