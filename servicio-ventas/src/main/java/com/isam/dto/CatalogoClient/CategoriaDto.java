package com.isam.dto.CatalogoClient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaDto(
    @NotNull(message = "El ID de categoría es obligatorio")
    Long idCategoria,
    
    @NotBlank(message = "El nombre de categoría es obligatorio")
    @Size(max = 100, message = "El nombre de la categoria debe ser de máximo 100 carácteres")
    String nombreCategoria,
    
    @Size(max = 500, message = "La descripción debe ser de máximo 500 carácteres")
    String descripcion
) {}
