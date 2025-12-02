package com.isam.dto.usuario;

import java.util.List;

/**
 * DTO para la respuesta de consulta de usuarios.
 * Contiene la lista de usuarios encontrados.
 * Coincide con ConsultarUsuariosRequest.Response del proto.
 */
public record ConsultarUsuariosResponseDto(
    
    List<UsuarioDto> usuarios
) {}