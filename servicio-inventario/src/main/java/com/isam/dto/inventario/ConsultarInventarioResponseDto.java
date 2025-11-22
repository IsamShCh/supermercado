package com.isam.dto.inventario;

import com.isam.dto.lote.DetalleLoteDto;
import com.isam.model.UnidadMedida;

import java.util.List;
import com.isam.dto.inventario.DetallesInventarioCompletoDto;

public record ConsultarInventarioResponseDto(
    DetallesInventarioCompletoDto detallesInventario
) {

}