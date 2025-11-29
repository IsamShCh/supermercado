package com.isam.dto;

import io.grpc.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para consultar un ticket.
 * Permite buscar por ID de ticket o número de ticket.
 */
public record ConsultarTicketRequestDto(
    
    @Size(max = 36, message = "El ID del ticket no puede exceder 36 caracteres")
    String idTicket,
    
    @Size(max = 50, message = "El número de ticket no puede exceder 50 caracteres")
    String numeroTicket
) {
    /**
     * Valida que al menos uno de los identificadores esté presente
     */
    public void validate() {
        // Validación de que al menos uno de los campos esté presente
        if ((idTicket == null || idTicket.trim().isEmpty()) && 
            (numeroTicket == null || numeroTicket.trim().isEmpty())) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Debe proporcionar el ID del ticket o el número de ticket")
                .asRuntimeException();
        }
        // Validación de que no se proporcionen ambos identificadores
        if (idTicket != null && !idTicket.trim().isEmpty() && 
            numeroTicket != null && !numeroTicket.trim().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("No se puede proporcionar tanto el ID del ticket como el número de ticket. Elige solo uno.")
                .asRuntimeException();
        }
    }
}