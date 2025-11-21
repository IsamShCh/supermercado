package com.isam.dto.inventario;


public record InventarioDto(
    String idInventario,
    String sku,
    Double cantidadAlmacen,
    Double cantidadEstanteria,
    String unidadMedida
) {}