package com.isam.dto.oferta;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OfertaDto(
    @NotBlank(message = "El ID de la oferta es obligatorio")
    String idOferta,
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    @NotNull(message = "El precio promocional es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio promocional debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    Double precioPromocional,
    
    @NotBlank(message = "El tipo de promoción es obligatorio")
    @Size(max = 50, message = "El tipo de promoción no puede exceder 50 caracteres")
    String tipoPromocion,
    
    @NotBlank(message = "La fecha de inicio es obligatoria")
    String fechaInicio,
    
    @NotBlank(message = "La fecha de fin es obligatoria")
    String fechaFin,
    
    @NotBlank(message = "El estado es obligatorio")
    String estado
) {}