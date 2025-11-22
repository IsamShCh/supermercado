package com.isam.dto.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultarInventarioRequestDto(
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU debe ser de máximo 50 carácteres")
    String sku
) {}