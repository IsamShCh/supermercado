package com.isam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla de hechos para movimientos de inventario.
 * Optimizada para consultas analíticas (BI).
 */
@Entity
@Getter
@Setter
@Table(name = "FACT_MOVIMIENTOS_INVENTARIO")
public class FactMovimientoInventario {

    @Id
    @Column(name = "IDMovimiento", length = 36)
    private String idMovimiento; // Usamos el mismo ID que en el servicio origen para trazabilidad

    @Column(name = "FechaHora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "SKU", length = 50, nullable = false)
    private String sku;

    @Column(name = "IDLote", length = 36)
    private String idLote;

    @Column(name = "TipoMovimiento", length = 50, nullable = false)
    private String tipoMovimiento;

    @Column(name = "Cantidad", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "UnidadMedidaSnapshot", length = 20)
    private String unidadMedidaSnapshot;

    @Column(name = "Motivo", length = 200)
    private String motivo;

    @Column(name = "IDUsuario", length = 36)
    private String idUsuario;

    @Column(name = "Ubicacion", length = 50)
    private String ubicacion; // ALMACEN o ESTANTERIA

    // Campos de auditoría del evento
    @Column(name = "IDEvento", length = 36)
    private String idEvento;

    @Column(name = "FechaProcesado")
    private LocalDateTime fechaProcesado;

    // Constructors
    public FactMovimientoInventario() {}
}
