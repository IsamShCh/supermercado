package com.isam.dto.autenticacion;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la verificación de token JWT.
 * Contiene el token a verificar.
 * Coincide con VerificarTokenRequest del proto.
 */
public record VerificarTokenRequestDto(
    
    @NotBlank(message = "El token JWT es obligatorio")
    String tokenJwt
) {}