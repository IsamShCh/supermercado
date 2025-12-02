package com.isam.dto.rol;

/**
 * DTO para la respuesta de creación de rol.
 * Contiene los datos del rol creado.
 * Coincide con CrearRolRequest.Response del proto.
 */
public record CrearRolResponseDto(
    
    RolDto rol
) {}