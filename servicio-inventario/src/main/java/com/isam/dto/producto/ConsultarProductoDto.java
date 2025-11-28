package com.isam.dto.producto;

import com.isam.model.UnidadMedida;

public record ConsultarProductoDto(
    String sku,
    String ean,
    String plu,
    UnidadMedida unidadMedida
) {}
