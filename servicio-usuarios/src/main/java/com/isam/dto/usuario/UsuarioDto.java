package com.isam.dto.usuario;

import com.isam.dto.rol.RolDto;
import com.isam.model.enums.EstadoUsuario;

import java.util.List;
import java.util.Optional;

/**
 * DTO para la representación de usuarios en respuestas.
 * Contiene los datos del usuario sin información sensible.
 * Coincide con UsuarioProto del proto.
 */
public record UsuarioDto(
    
    String idUsuario,
    
    String nombreUsuario,
    
    String nombreCompleto,
    
    EstadoUsuario estado,
    
    String fechaCreacion,
    
    Optional<String> fechaUltimoAcceso,
    
    Boolean requiereCambioPassword,
    
    List<RolDto> roles
) {}