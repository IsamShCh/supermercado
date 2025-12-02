package com.isam.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación de roles.
 * Contiene los datos necesarios para crear un nuevo rol en el sistema.
 * Coincide con CrearRolRequest del proto.
 */
public record CrearRolRequestDto(
    
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del rol debe tener entre 3 y 100 caracteres")
    String nombreRol,
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @NotBlank(message = "La descripción del rol es obligatoria")
    String descripcion
) {}