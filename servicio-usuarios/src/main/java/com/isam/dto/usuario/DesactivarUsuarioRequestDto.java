package com.isam.dto.usuario;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la desactivación de usuarios.
 * Contiene el ID del usuario a desactivar.
 */
public record DesactivarUsuarioRequestDto(
    
    @NotBlank(message = "El ID de usuario es obligatorio")
    String idUsuario
) {}