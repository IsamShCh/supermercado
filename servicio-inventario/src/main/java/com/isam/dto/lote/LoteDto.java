package com.isam.dto.lote;

import com.isam.model.EstadoLote;
import com.isam.model.UnidadMedida;

import java.math.BigDecimal;

public record LoteDto(
    String idLote,
    String sku,
    String idInventario,
    String numeroLote,
    BigDecimal cantidadEntrada,
    BigDecimal cantidadAlmacen,
    BigDecimal cantidadEstanteria,
    String fechaCaducidad,  // YYYY-MM-DD
    String idProveedor,
    String fechaIngreso,    // YYYY-MM-DD
    String unidadMedida,
    String estado
) {}