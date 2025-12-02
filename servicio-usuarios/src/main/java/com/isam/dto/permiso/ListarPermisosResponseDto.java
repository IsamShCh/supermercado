package com.isam.dto.permiso;

import java.util.List;

/**
 * DTO para la respuesta de listar permisos.
 * Contiene la lista de permisos del sistema.
 * Coincide con ListarPermisosRequest.Response del proto.
 */
public record ListarPermisosResponseDto(
    
    List<PermisoDto> permisos
) {}