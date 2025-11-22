package com.isam.dto.inventario;


public record InventarioDto(
    String idInventario,
    String sku,
    String ean,
    String plu,
    Double cantidadAlmacen,
    Double cantidadEstanteria,
    String unidadMedida
) {}