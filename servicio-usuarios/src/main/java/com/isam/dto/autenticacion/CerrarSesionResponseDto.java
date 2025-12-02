package com.isam.dto.autenticacion;

/**
 * DTO para la respuesta del cerrar sesión.
 * Respuesta vacía en caso de éxito según el proto.
 * Coincide con CerrarSesionRequest.Response del proto.
 */
public record CerrarSesionResponseDto(
) {}