package com.isam.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para la asignación de permisos a roles.
 * Contiene los datos necesarios para asignar permisos a un rol.
 * Coincide con AsignarPermisosRequest del proto.
 */
public record AsignarPermisosRequestDto(
    
    @NotBlank(message = "El ID del rol es obligatorio")
    @Size(min = 36, max = 36, message = "El ID del rol debe tener exactamente 36 caracteres")
    String idRol,
    
    @NotNull(message = "La lista de permisos es obligatoria")
    List<@NotBlank(message = "El ID del permiso es obligatorio") @Size(min = 36, max = 36, message = "El ID del permiso debe tener como exactamente 36 caracteres") String> idPermisos
) {}