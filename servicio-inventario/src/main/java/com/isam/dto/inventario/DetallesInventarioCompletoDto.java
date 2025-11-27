package com.isam.dto.inventario;

import java.math.BigDecimal;
import java.util.List;

import com.isam.dto.lote.DetalleLoteDto;
import com.isam.model.UnidadMedida;

public record DetallesInventarioCompletoDto(
    String sku,
    String nombreProducto,
    BigDecimal stockTotalAlmacen,
    BigDecimal stockTotalEstanteria,
    UnidadMedida unidadMedida,
    List<DetalleLoteDto> lotes
){}