package com.isam.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para recatalogar un producto.
 * Solo requiere el SKU del producto a recatalogar.
 */
public record RecatalogarProductoDto(
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku
) {}