package com.isam.dto.inventario;

import java.math.BigDecimal;
import java.util.List;

public record ReporteDiscrepanciasDto(
    String sku,
    BigDecimal stockLogicoEstanteria,
    BigDecimal stockFisicoEstanteria,
    BigDecimal discrepanciaEstanteria,  // físico - lógico
    BigDecimal stockLogicoAlmacen,
    BigDecimal stockFisicoAlmacen,
    BigDecimal discrepanciaAlmacen,  // físico - lógico
    List<AjusteLoteDto> ajustesRealizados
) {}