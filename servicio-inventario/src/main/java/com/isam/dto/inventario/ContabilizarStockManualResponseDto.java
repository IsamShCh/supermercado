package com.isam.dto.inventario;

import com.isam.dto.movimiento.MovimientoInventarioDto;
import java.util.List;

public record ContabilizarStockManualResponseDto(
    InventarioDto inventario,
    List<MovimientoInventarioDto> movimientos,
    ReporteDiscrepanciasDto reporteDiscrepancias
) {}