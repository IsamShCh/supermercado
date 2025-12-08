package com.isam.dto.venta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import com.isam.model.UnidadMedida;

public record ItemVentaDto(
    @NotBlank(message = "El SKU es obligatorio")
    String sku,
    
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", inclusive = true, message = "La cantidad debe ser mayor que 0")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    BigDecimal cantidad,

    @NotNull(message = "La unidad de medida es obligatoria")
    UnidadMedida unidadMedida
) {}