package com.isam.dto.venta;

import com.isam.dto.movimiento.MovimientoInventarioDto;

import java.util.List;

public record RegistrarVentaResponseDto(
    List<MovimientoInventarioDto> movimientos
) {}