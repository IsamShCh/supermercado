package com.isam.dto.usuario;

import java.util.Optional;

/**
 * DTO para la consulta de usuarios.
 * Contiene los filtros opcionales para buscar usuarios.
 * Coincide con ConsultarUsuariosRequest del proto.
 */
public record ConsultarUsuariosRequestDto(
    
    Optional<String> idUsuario,
    
    Optional<String> nombreUsuario,
    
    Optional<String> idRol
) {}