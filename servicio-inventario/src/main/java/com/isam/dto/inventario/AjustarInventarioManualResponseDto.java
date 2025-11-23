package com.isam.dto.inventario;

import com.isam.dto.movimiento.MovimientoInventarioDto;

public record AjustarInventarioManualResponseDto(
    InventarioDto inventario,
    MovimientoInventarioDto movimiento
) {}