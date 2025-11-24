package com.isam.dto.inventario;

public record AjusteLoteDto(
    String idLote,
    String numeroLote,
    String ubicacion,  // "ALMACEN" o "ESTANTERIA"
    double cantidadAjustada,  // positivo = añadido, negativo = deducido
    double stockAnterior,
    double stockNuevo
) {}