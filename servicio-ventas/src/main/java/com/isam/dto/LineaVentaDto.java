package com.isam.dto;

import java.math.BigDecimal;

public record LineaVentaDto(
    Integer numeroLinea,
    String sku,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    String promocionAplicada,
    BigDecimal subtotal,
    BigDecimal impuesto
) {}