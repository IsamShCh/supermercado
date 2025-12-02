package com.isam.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;

/**
 * DTO para la modificación de roles.
 * Contiene los datos opcionales para modificar un rol existente.
 * Coincide con ModificarRolRequest del proto.
 */
public record ModificarRolRequestDto(
    
    @NotBlank(message = "El ID del rol es obligatorio")
    String idRol,
    
    @Size(min = 3, max = 100, message = "El nombre del rol debe tener entre 3 y 100 caracteres")
    Optional<String> nombreRol,
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    Optional<String> descripcion
) {}