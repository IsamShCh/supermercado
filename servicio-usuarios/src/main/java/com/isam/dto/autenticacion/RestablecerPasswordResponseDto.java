package com.isam.dto.autenticacion;

/**
 * DTO para la respuesta del restablecimiento de contraseña.
 * Contiene el ID del usuario y la contraseña temporal generada.
 * Coincide con RestablecerPasswordRequest.Response del proto.
 */
public record RestablecerPasswordResponseDto(
    
    String idUsuario,
    
    String passwordTemporal
) {}