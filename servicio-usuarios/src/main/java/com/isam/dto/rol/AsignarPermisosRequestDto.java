package com.isam.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para la asignación de permisos a roles.
 * Contiene los datos necesarios para asignar permisos a un rol.
 * Coincide con AsignarPermisosRequest del proto.
 */
public record AsignarPermisosRequestDto(
    
    @NotBlank(message = "El ID del rol es obligatorio")
    String idRol,
    
    @NotNull(message = "La lista de permisos es obligatoria")
    List<String> idPermisos
) {}