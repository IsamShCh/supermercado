package com.isam.dto.inventario;

// Records para las ubicaciones
public record AjustarAlmacenDto(
    String idLote  // Opcional
) implements UbicacionAjusteDto {}