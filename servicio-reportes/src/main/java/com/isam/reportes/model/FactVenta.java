package com.isam.reportes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla de hechos para ventas.
 * Granularidad: Una fila por cada ítem vendido (línea de ticket).
 */
@Entity
@Getter
@Setter
@Table(name = "FACT_VENTAS")
public class FactVenta {

    @Id
    @Column(name = "IDVenta", length = 36)
    private String idVenta; // UUID generado aquí

    @Column(name = "IDTicket", length = 36, nullable = false)
    private String idTicket; // ID original del ticket

    @Column(name = "NumeroTicket", length = 50, nullable = false)
    private String numeroTicket;

    @Column(name = "FechaHora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "IDUsuarioCajero", length = 36)
    private String idUsuarioCajero;

    @Column(name = "MetodoPago", length = 50)
    private String metodoPago;

    // Detalles del Item
    @Column(name = "SKU", length = 50, nullable = false)
    private String sku;

    @Column(name = "Cantidad", precision = 10, scale = 3, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "PrecioUnitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    @Column(name = "TotalLinea", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalLinea; // (Precio * Cantidad) - Descuento + Impuesto

    // Datos snapshot para histórico por si cambia el catálogo
    @Column(name = "NombreProductoSnapshot", length = 200)
    private String nombreProductoSnapshot;

    @Column(name = "CategoriaSnapshot", length = 100)
    private String categoriaSnapshot;

    // Auditoría del evento
    @Column(name = "IDEvento", length = 36)
    private String idEvento;

    @Column(name = "FechaProcesado")
    private LocalDateTime fechaProcesado;

    public FactVenta() {}
}
