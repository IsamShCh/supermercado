package com.isam.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTOS")
public class Producto {

    @Id
    @Column(name = "SKU", length = 50)
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

    // Getters and Setters
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Boolean getCaduca() {
        return caduca;
    }

    public void setCaduca(Boolean caduca) {
        this.caduca = caduca;
    }

    public Boolean getEsGranel() {
        return esGranel;
    }

    public void setEsGranel(Boolean esGranel) {
        this.esGranel = esGranel;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public PoliticaRotacion getPoliticaRotacion() {
        return politicaRotacion;
    }

    public void setPoliticaRotacion(PoliticaRotacion politicaRotacion) {
        this.politicaRotacion = politicaRotacion;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedida unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(String etiquetas) {
        this.etiquetas = etiquetas;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }
}