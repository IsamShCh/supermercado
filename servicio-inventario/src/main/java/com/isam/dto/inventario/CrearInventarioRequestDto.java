package com.isam.dto.inventario;

import com.isam.model.UnidadMedida;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearInventarioRequestDto(
    @Size(max = 50)
    @NotBlank(message = "El SKU es obligatorio")
    String sku,
    @NotNull(message = "La unidad de medida es obligatoria")
    UnidadMedida unidadMedida
) {
}
