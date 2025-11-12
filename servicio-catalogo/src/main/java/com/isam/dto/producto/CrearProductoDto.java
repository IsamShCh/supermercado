package com.isam.dto.producto;

import com.isam.model.PoliticaRotacion;
import com.isam.model.UnidadMedida;

import java.math.BigDecimal;
import java.util.List;

public class CrearProductoDto {
    
    private String sku;
    private String ean;
    private String plu;
    private String nombre;
    private String descripcion;
    private BigDecimal precioVenta;
    private Boolean caduca;
    private Boolean esGranel;
    private Long idCategoria;
    private PoliticaRotacion politicaRotacion;
    private UnidadMedida unidadMedida;
    private List<String> etiquetas;

    // Constructors
    public CrearProductoDto() {}

    public CrearProductoDto(String sku, String nombre, BigDecimal precioVenta, 
                           Long idCategoria, PoliticaRotacion politicaRotacion, 
                           UnidadMedida unidadMedida) {
        this.sku = sku;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.idCategoria = idCategoria;
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

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
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

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas;
    }

    @Override
    public String toString() {
        return "CrearProductoDto{" +
                "sku='" + sku + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precioVenta=" + precioVenta +
                ", idCategoria=" + idCategoria +
                ", etiquetas=" + etiquetas +
                '}';
    }
}