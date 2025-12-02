package com.isam.dto.rol;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la eliminación de roles.
 * Contiene el ID del rol a eliminar.
 * Coincide con EliminarRolRequest del proto.
 */
public record EliminarRolRequestDto(
    
    @NotBlank(message = "El ID del rol es obligatorio")
    String idRol
) {}