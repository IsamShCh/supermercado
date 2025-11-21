package com.isam.dto.existencias;

import com.isam.dto.lote.LoteDto;
import com.isam.dto.inventario.InventarioDto;


public record RegistrarNuevasExistenciasResponseDto(
    LoteDto lote,
    InventarioDto inventario
) {}