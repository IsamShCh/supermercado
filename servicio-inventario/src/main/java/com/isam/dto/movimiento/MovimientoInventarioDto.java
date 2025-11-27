package com.isam.dto.movimiento;

import java.math.BigDecimal;

public record MovimientoInventarioDto(
    String idMovimiento,
    String sku,
    String idLote,
    String tipoMovimiento,
    BigDecimal cantidad,
    String unidadMedida,
    String fechaHora,
    String idUsuario,
    String motivo,
    String observaciones
) {}