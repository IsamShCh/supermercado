package com.isam.dto.oferta;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearOfertaDto(
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    @NotNull(message = "El precio promocional es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio promocional debe ser mayor que 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    BigDecimal precioPromocional,
    
    @NotBlank(message = "El tipo de promoción es obligatorio")
    @Size(max = 100, message = "El tipo de promoción no puede exceder 100 caracteres")
    String tipoPromocion,
    
    @NotBlank(message = "La fecha de inicio es obligatoria")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha de inicio debe tener el formato YYYY-MM-DD")
    String fechaInicio,
    
    @NotBlank(message = "La fecha de fin es obligatoria")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha de fin debe tener el formato YYYY-MM-DD")
    String fechaFin
) {}