package com.isam.dto.categoria;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ModificarCategoriaDto(
    @NotNull(message = "El ID de categoría es obligatorio")
    @Positive(message = "El ID de categoría debe ser positivo")
    Long idCategoria,
    
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombreCategoria,
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    String descripcion
) {}