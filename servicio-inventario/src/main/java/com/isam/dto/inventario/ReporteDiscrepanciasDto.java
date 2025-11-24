package com.isam.dto.inventario;

import java.util.List;

public record ReporteDiscrepanciasDto(
    String sku,
    double stockLogicoEstanteria,
    double stockFisicoEstanteria,
    double discrepanciaEstanteria,  // físico - lógico
    double stockLogicoAlmacen,
    double stockFisicoAlmacen,
    double discrepanciaAlmacen,  // físico - lógico
    List<AjusteLoteDto> ajustesRealizados
) {}