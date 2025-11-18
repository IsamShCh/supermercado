package com.isam.dto.producto;

import com.isam.dto.comun.PaginacionDto;
import jakarta.validation.Valid;

public record ListarProductosRequestDto(
    @Valid
    PaginacionDto paginacion
) {}