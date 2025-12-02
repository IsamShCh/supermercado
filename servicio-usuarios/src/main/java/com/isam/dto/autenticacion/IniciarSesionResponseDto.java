package com.isam.dto.autenticacion;

import com.isam.dto.usuario.UsuarioDto;

/**
 * DTO para la respuesta del inicio de sesión.
 * Contiene el token JWT y los datos del usuario.
 * Coincide con IniciarSesionRequest.Response del proto.
 */
public record IniciarSesionResponseDto(
    
    String tokenJwt,
    
    UsuarioDto usuario
) {}