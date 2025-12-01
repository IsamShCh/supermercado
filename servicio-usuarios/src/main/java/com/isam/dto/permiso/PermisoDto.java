package com.isam.dto.permiso;

import com.isam.model.enums.AccionPermiso;

/**
 * DTO para la representación de permisos en respuestas.
 * Contiene los datos del permiso.
 */
public record PermisoDto(
    
    String idPermiso,
    
    String nombrePermiso,
    
    String descripcion,
    
    String recurso,
    
    AccionPermiso accion
) {}