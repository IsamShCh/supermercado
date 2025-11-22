package com.isam.dto.existencias;

import com.isam.model.UnidadMedida;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RegistrarNuevasExistenciasRequestDto(
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor que 0")
    @Digits(integer = 10, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
    BigDecimal cantidad,
    
    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
    String numeroLote,
    
    // FechaCaducidad es opcional (solo para productos que caducan)
    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "El formato de fecha debe ser YYYY-MM-DD")
    String fechaCaducidad,  // Formato YYYY-MM-DD
    
    @NotBlank(message = "El ID del proveedor es obligatorio")
    String idProveedor,
    
    @NotNull(message = "La unidad de medida es obligatoria")
    UnidadMedida unidadMedida
) {}