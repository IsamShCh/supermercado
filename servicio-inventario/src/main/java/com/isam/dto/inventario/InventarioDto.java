package com.isam.dto.inventario;

import java.math.BigDecimal;

public record InventarioDto(
    String idInventario,
    String sku,
    String ean,
    String plu,
    BigDecimal cantidadAlmacen,
    BigDecimal cantidadEstanteria,
    String unidadMedida
) {}