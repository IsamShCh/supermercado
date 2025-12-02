package com.isam.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Optional;

/**
 * DTO para la modificación de usuarios.
 * Contiene los datos opcionales para modificar un usuario existente.
 * Coincide con ModificarUsuarioRequest del proto.
 */
public record ModificarUsuarioRequestDto(
    
    @NotBlank(message = "El ID de usuario es obligatorio")
    String idUsuario,
    
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    Optional<String> nombreUsuario,
    
    @Size(max = 200, message = "El nombre completo no puede exceder 200 caracteres")
    Optional<String> nombreCompleto,
    
    List<String> idRoles
) {}