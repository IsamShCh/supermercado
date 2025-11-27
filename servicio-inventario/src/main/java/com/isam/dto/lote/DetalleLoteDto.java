package com.isam.dto.lote;

import java.math.BigDecimal;

public record DetalleLoteDto(
    String idLote,
    String numeroLote,
    BigDecimal cantidadAlmacen,
    BigDecimal cantidadEstanteria,
    String fechaCaducidad,  // YYYY-MM-DD
    String fechaIngreso     // YYYY-MM-DD
) {}