package com.isam.dto.usuario;

import jakarta.validation.constraints.Size;
import java.util.Optional;

/**
 * DTO para la consulta de usuarios.
 * Contiene los filtros opcionales para buscar usuarios.
 * Coincide con ConsultarUsuariosRequest del proto.
 */
public record ConsultarUsuariosRequestDto(
    
    Optional<@Size(max = 36, message = "El ID de usuario no puede exceder 36 caracteres") String> idUsuario,
    
    Optional<@Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres") String> nombreUsuario,
    
    Optional<@Size(max = 36, message = "El ID de rol no puede exceder 36 caracteres") String> idRol
) {}