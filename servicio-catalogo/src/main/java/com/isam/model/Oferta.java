package com.isam.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "OFERTAS")
public class Oferta {

    @Id
    @Column(name = "IDOferta", length = 50)
    private String idOferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SKU", nullable = false)
    private Producto producto;

    @Column(name = "PrecioPromocional", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioPromocional;

    @Column(name = "TipoPromocion", length = 100)
    private String tipoPromocion;

    @Column(name = "FechaInicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FechaFin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false)
    private EstadoOferta estado = EstadoOferta.ACTIVA;

    // Constructors
    public Oferta() {}

    public Oferta(String idOferta, Producto productoEntity, BigDecimal precioPromocional,
                  LocalDate fechaInicio, LocalDate fechaFin) {
        this.idOferta = idOferta;
        this.producto = productoEntity;
        this.precioPromocional = precioPromocional;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    // Getters and Setters
    public String getIdOferta() {
        return idOferta;
    }

    public void setIdOferta(String idOferta) {
        this.idOferta = idOferta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto productoEntity) {
        this.producto = productoEntity;
    }

    public BigDecimal getPrecioPromocional() {
        return precioPromocional;
    }

    public void setPrecioPromocional(BigDecimal precioPromocional) {
        this.precioPromocional = precioPromocional;
    }

    public String getTipoPromocion() {
        return tipoPromocion;
    }

    public void setTipoPromocion(String tipoPromocion) {
        this.tipoPromocion = tipoPromocion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoOferta getEstado() {
        return estado;
    }

    public void setEstado(EstadoOferta estado) {
        this.estado = estado;
    }
}