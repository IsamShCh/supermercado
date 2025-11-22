package com.isam.dto.lote;

public record DetalleLoteDto(
    String idLote,
    String numeroLote,
    Double cantidadAlmacen,
    Double cantidadEstanteria,
    String fechaCaducidad,  // YYYY-MM-DD
    String fechaIngreso     // YYYY-MM-DD
) {}