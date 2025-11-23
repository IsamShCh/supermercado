package com.isam.dto.movimiento;

public record MovimientoInventarioDto(
    String idMovimiento,
    String sku,
    String idLote,
    String tipoMovimiento,
    Double cantidad,
    String unidadMedida,
    String fechaHora,
    String idUsuario,
    String motivo,
    String observaciones
) {}