package com.isam.dto.rol;

import java.util.List;
import com.isam.dto.permiso.PermisoDto;

/**
 * DTO para la representación de roles en respuestas.
 * Contiene los datos del rol.
 * Coincide con RolProto del proto.
 */
public record RolDto(
    
    String idRol,
    
    String nombreRol,
    
    String descripcion,

    List<PermisoDto> permisos
) {}