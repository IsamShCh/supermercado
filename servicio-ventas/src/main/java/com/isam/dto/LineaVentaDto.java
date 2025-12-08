package com.isam.dto;

import java.math.BigDecimal;

import com.isam.model.UnidadMedida;

public record LineaVentaDto(
    Integer numeroLinea,
    String sku,
    String descripcion,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal descuento,
    String promocionAplicada,
    BigDecimal subtotal,
    BigDecimal impuesto,
    String categoria,
    UnidadMedida unidadMedida
) {}