package com.isam.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Optional;

public record EliminarProductoTicketRequestDto(
    
    @NotBlank(message = "El ID del ticket es obligatorio")
    @Size(max = 36, message = "El ID del ticket no puede exceder 36 caracteres")
    String idTicket,
    
    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    String sku,
    
    Optional<
        @DecimalMin(value = "0.001", inclusive = true, message = "La cantidad a eliminar debe ser mayor que 0")
        @Digits(integer = 7, fraction = 3, message = "La cantidad debe tener máximo 10 dígitos enteros y 3 decimales")
        BigDecimal
    > cantidadAEliminar  // Opcional - vacío significa eliminar todo
) {
}