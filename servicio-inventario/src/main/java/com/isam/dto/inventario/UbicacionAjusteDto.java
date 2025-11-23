package com.isam.dto.inventario;

// Base para las ubicaciones
public sealed interface UbicacionAjusteDto permits AjustarAlmacenDto, AjustarEstanteriaDto {}