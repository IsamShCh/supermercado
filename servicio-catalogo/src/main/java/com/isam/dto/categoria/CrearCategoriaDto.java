package com.isam.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearCategoriaDto(
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombreCategoria,
    @NotBlank(message = "La descripción de la categoría es obligatoria")
    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    String descripcion
) {}