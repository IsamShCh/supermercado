package com.isam.dto.rol;

/**
 * DTO para la representación de roles en respuestas.
 * Contiene los datos del rol.
 * Coincide con RolProto del proto.
 */
public record RolDto(
    
    String idRol,
    
    String nombreRol,
    
    String descripcion
) {}