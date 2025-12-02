package com.isam.dto.autenticacion;

import com.isam.dto.rol.RolDto;
import java.util.Optional;

/**
 * DTO para la respuesta de verificación de token JWT.
 * Contiene la validez del token y datos del usuario si es válido.
 * Coincide con VerificarTokenRequest.Response del proto.
 */
public record VerificarTokenResponseDto(
    
    Boolean esValido,
    
    Optional<String> idUsuario,
    
    Optional<String> nombreUsuario,
    
    java.util.List<RolDto> roles
) {}