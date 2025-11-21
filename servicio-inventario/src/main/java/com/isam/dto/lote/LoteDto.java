package com.isam.dto.lote;

import com.isam.model.EstadoLote;
import com.isam.model.UnidadMedida;


public record LoteDto(
    String idLote,
    String sku,
    String idInventario,
    String ean,
    String plu,
    String numeroLote,
    Double cantidad,
    String fechaCaducidad,  // YYYY-MM-DD
    String idProveedor,
    String fechaIngreso,    // YYYY-MM-DD
    String unidadMedida,
    String estado
) {}