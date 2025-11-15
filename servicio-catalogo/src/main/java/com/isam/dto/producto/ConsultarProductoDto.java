package com.isam.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultarProductoDto(

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku
) 
{}
