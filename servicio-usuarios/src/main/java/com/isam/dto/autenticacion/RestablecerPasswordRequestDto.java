package com.isam.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el restablecimiento de contraseña.
 * Contiene el ID del usuario cuyo password se restablecerá.
 * Coincide con RestablecerPasswordRequest del proto.
 */
public record RestablecerPasswordRequestDto(
    
    @NotBlank(message = "El ID del usuario es obligatorio")
    @Size(min = 36, max = 36, message = "El ID del usuario debe tener 36 caracteres")
    String idUsuario
) {}