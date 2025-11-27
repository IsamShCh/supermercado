package com.isam.dto.producto;

import java.math.BigDecimal;
import java.util.List;

import com.isam.dto.categoria.CategoriaDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProductoDto(
        @NotNull(message = "El SKU es obligatorio")
        String sku,

        String ean,

        String plu,

        @NotNull(message = "El nombre es obligatorio")
        String nombre,

        String descripcion,

        @NotNull(message = "El precio de venta es obligatorio")
        BigDecimal precioVenta,

        @NotNull(message = "El campo 'caduca' es obligatorio")
        Boolean caduca,
        
        @NotNull(message = "El campo 'esGranel' es obligatorio")
        Boolean esGranel,
        
        @Valid
        CategoriaDto categoria,
        
        String politicaRotacion,
        
        String unidadMedida,
        
        List<String> etiquetas,
        
        String estado
    ) {}