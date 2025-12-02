package com.isam.dto.rol;

/**
 * DTO para la respuesta de modificación de rol.
 * Contiene los datos del rol modificado.
 * Coincide con ModificarRolRequest.Response del proto.
 */
public record ModificarRolResponseDto(
    
    RolDto rol
) {}