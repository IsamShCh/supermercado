package com.isam.dto;

import jakarta.validation.constraints.NotBlank;

public record CerrarTicketRequestDto(
    @NotBlank(message = "El ID del ticket temporal es obligatorio")
    String idTicketTemporal
) {}