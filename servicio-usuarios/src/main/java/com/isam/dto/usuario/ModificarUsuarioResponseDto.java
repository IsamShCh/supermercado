package com.isam.dto.usuario;

/**
 * DTO para la respuesta de modificación de usuario.
 * Contiene los datos del usuario modificado.
 * Coincide con ModificarUsuarioRequest.Response del proto.
 */
public record ModificarUsuarioResponseDto(
    
    UsuarioDto usuario
) {}