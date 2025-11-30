package com.isam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de cancelación de ticket temporal
 * Corresponde al AC25: Cancelar ticket 
 */
public record CancelarTicketRequestDto(
    
    @NotBlank(message = "El ID del ticket es obligatorio")
    @Size(max = 36, message = "El ID del ticket debe tener máximo 36 caracteres")
    String idTicket
    
) {
}