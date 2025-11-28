package com.isam.dto.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RegistrarVentaRequestDto(
    @NotBlank(message = "El número de ticket es obligatorio")
    String numeroTicket,
    
    @NotEmpty(message = "La lista de items no puede estar vacía")
    @Valid
    List<ItemVentaDto> items
) {}