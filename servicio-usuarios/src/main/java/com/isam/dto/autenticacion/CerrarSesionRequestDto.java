package com.isam.dto.autenticacion;

/**
 * DTO para la solicitud de cerrar sesión.
 * El token se envía en los metadatos según el proto.
 * Coincide con CerrarSesionRequest del proto.
 */
public record CerrarSesionRequestDto(
) {}