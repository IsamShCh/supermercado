package com.isam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnadirProductoTicketRequestDto(
    
    @NotBlank(message = "El ID del ticket temporal es obligatorio")
    @Size(max = 36, message = "El ID del ticket temporal no puede exceder 36 caracteres")
    String idTicketTemporal,
    
    @NotBlank(message = "El código de barras es obligatorio")
    @Size(max = 50, message = "El código de barras no puede exceder 50 caracteres")
    String codigoBarras
) {
}