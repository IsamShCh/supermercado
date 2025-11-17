package com.isam.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para descatalogar un producto.
 * Solo requiere el SKU del producto a descatalogar.
 */
public record DescatalogarProductoDto(
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku
) {}