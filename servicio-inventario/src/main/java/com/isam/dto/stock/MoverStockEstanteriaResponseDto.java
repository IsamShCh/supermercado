package com.isam.dto.stock;

import com.isam.dto.inventario.InventarioDto;
import com.isam.dto.movimiento.MovimientoInventarioDto;

public record MoverStockEstanteriaResponseDto(
    InventarioDto inventario,
    MovimientoInventarioDto movimiento
) {}