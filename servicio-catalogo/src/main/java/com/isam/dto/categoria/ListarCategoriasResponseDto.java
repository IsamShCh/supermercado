package com.isam.dto.categoria;

import java.util.List;

public record ListarCategoriasResponseDto(
    List<CategoriaDto> categorias
) {}