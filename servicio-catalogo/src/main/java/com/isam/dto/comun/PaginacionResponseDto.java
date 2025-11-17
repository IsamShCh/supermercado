package com.isam.dto.comun;

import jakarta.validation.constraints.NotNull;

public record PaginacionResponseDto(
        @NotNull(message = "El número de página es obligatorio")
        Integer page,
        
        @NotNull(message = "El tamaño de página es obligatorio")
        Integer pageSize,
        
        @NotNull(message = "El total de páginas es obligatorio")
        Integer totalPages,
        
        @NotNull(message = "El total de elementos es obligatorio")
        Long totalElements
    ) {}
