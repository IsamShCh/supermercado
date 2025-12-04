package com.isam.dto.usuario;

import jakarta.validation.constraints.Size;
import java.util.Optional;

/**
 * DTO para la consulta de usuarios.
 * Contiene los filtros opcionales para buscar usuarios.
 * Coincide con ConsultarUsuariosRequest del proto.
 */
public record ConsultarUsuariosRequestDto(
    
    Optional<@Size(min = 36, max = 36, message = "El ID de usuario debe tener exactamente 36 caracteres") String> idUsuario,
    
    Optional<@Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres") String> nombreUsuario,
    
    Optional<@Size(min = 36,max = 36, message = "El ID de rol debe tener exactamente 36 caracteres") String> idRol
) {}