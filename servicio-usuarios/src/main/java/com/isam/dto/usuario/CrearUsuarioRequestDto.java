package com.isam.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO para la creación de usuarios.
 * Contiene los datos necesarios para crear un nuevo usuario en el sistema.
 */
public record CrearUsuarioRequestDto(
    
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    String nombreUsuario,
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String password,
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 200, message = "El nombre completo no puede exceder 200 caracteres")
    String nombreCompleto,
    
    @NotEmpty(message = "Debe asignar al menos un rol")
    @NotNull(message = "Los roles son obligatorios")
    List<@NotBlank(message = "El ID del rol es obligatorio") String> idRoles
) {}