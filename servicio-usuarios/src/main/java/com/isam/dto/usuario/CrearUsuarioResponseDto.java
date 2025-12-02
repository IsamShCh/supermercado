package com.isam.dto.usuario;

/**
 * DTO para la respuesta de creación de usuario.
 * Contiene los datos del usuario creado.
 * Coincide con CrearUsuarioRequest.Response del proto.
 */
public record CrearUsuarioResponseDto(
    
    UsuarioDto usuario
) {}