package com.isam.dto.producto;

import com.isam.model.PoliticaRotacion;
import com.isam.model.UnidadMedida;

import java.math.BigDecimal;
import java.util.List;

public record CrearProductoDto(
    String sku,
    String ean,
    String plu,
    String nombre,
    String descripcion,
    BigDecimal precioVenta,
    Boolean caduca,
    Boolean esGranel,
    Long idCategoria,
    PoliticaRotacion politicaRotacion,
    UnidadMedida unidadMedida,
    List<String> etiquetas
) {}