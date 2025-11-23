package com.isam.dto.stock;

import com.isam.model.UnidadMedida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MoverStockEstanteriaRequestDto(
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    @NotBlank(message = "El ID del lote es obligatorio")
    @Size(max = 36, message = "El ID del lote no puede exceder 36 caracteres")
    String idLote,
    
    @NotNull(message = "La cantidad a trasladar es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad a trasladar debe ser mayor que 0")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    BigDecimal cantidadTransladar,
    
    @NotNull(message = "La unidad de medida es obligatoria")
    UnidadMedida unidadMedida
) {}