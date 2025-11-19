package com.isam.dto.producto;

import com.isam.model.PoliticaRotacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ModificarProductoDto(
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    String nombre,

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    String descripcion,

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    BigDecimal precioVenta,

    @Positive(message = "El ID de categoría debe ser positivo")
    Long idCategoria,

    PoliticaRotacion politicaRotacion,

    @Size(max = 500, message = "Las etiquetas no pueden exceder 500 caracteres en total")
    List<@Size(max = 50) @NotBlank String> etiquetas
) {}