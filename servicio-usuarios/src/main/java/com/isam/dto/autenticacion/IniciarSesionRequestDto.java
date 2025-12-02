package com.isam.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el inicio de sesión.
 * Contiene las credenciales del usuario.
 * Coincide con IniciarSesionRequest del proto.
 */
public record IniciarSesionRequestDto(
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    String nombreUsuario,
    
    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}