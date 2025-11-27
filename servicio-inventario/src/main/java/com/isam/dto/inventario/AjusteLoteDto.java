package com.isam.dto.inventario;

import java.math.BigDecimal;

public record AjusteLoteDto(
    String idLote,
    String numeroLote,
    String ubicacion,  // "ALMACEN" o "ESTANTERIA"
    BigDecimal cantidadAjustada,  // positivo = añadido, negativo = deducido
    BigDecimal stockAnterior,
    BigDecimal stockNuevo
) {}