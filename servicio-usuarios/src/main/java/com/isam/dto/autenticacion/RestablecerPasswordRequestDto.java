package com.isam.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para el restablecimiento de contraseña.
 * Contiene el ID del usuario cuyo password se restablecerá.
 * Coincide con RestablecerPasswordRequest del proto.
 */
public record RestablecerPasswordRequestDto(
    
    @NotBlank(message = "El ID del usuario es obligatorio")
    String idUsuario
) {}