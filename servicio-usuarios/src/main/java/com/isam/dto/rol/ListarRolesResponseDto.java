package com.isam.dto.rol;

import java.util.List;

/**
 * DTO para la respuesta de listar roles.
 * Contiene la lista de roles del sistema.
 * Coincide con ListarRolesRequest.Response del proto.
 */
public record ListarRolesResponseDto(
    
    List<RolDto> roles
) {}