package com.isam.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el cambio de contraseña.
 * Contiene la contraseña actual y la nueva.
 * Coincide con CambiarPasswordRequest del proto.
 */
public record CambiarPasswordRequestDto(
    
    @NotBlank(message = "La contraseña actual es obligatoria")
    String passwordActual,
    
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
    String passwordNueva
) {}