package com.isam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "MOVIMIENTOS_INVENTARIO")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "IDMovimiento", length = 36)
    private String idMovimiento;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(name = "SKU", length = 50, nullable = false)
    private String sku;

    @Column(name = "IDLote", length = 36)
    private String idLote;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "TipoMovimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    @Column(name = "Cantidad", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidad;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "UnidadMedida", nullable = false)
    private UnidadMedida unidadMedida;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "FechaHora", nullable = false)
    private LocalDateTime fechaHora;

    @NotBlank(message = "El ID del usuario es obligatorio")
    @Column(name = "IDUsuario", length = 36, nullable = false)
    private String idUsuario;

    @Column(name = "Motivo", length = 200)
    private String motivo;

    @Column(name = "Observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Constructors
    public MovimientoInventario() {}

    public MovimientoInventario(String sku, String idLote, TipoMovimiento tipoMovimiento,
                               BigDecimal cantidad, UnidadMedida unidadMedida,
                               LocalDateTime fechaHora, String idUsuario, String motivo,
                               String observaciones) {
        this.sku = sku;
        this.idLote = idLote;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.fechaHora = fechaHora;
        this.idUsuario = idUsuario;
        this.motivo = motivo;
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "MovimientoInventario{" +
                "idMovimiento='" + idMovimiento + '\'' +
                ", sku='" + sku + '\'' +
                ", idLote='" + idLote + '\'' +
                ", tipoMovimiento=" + tipoMovimiento +
                ", cantidad=" + cantidad +
                ", unidadMedida=" + unidadMedida +
                ", fechaHora=" + fechaHora +
                ", idUsuario='" + idUsuario + '\'' +
                ", motivo='" + motivo + '\'' +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}