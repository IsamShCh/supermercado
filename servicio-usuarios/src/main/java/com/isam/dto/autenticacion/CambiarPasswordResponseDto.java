package com.isam.dto.autenticacion;

/**
 * DTO para la respuesta del cambio de contraseña.
 * Respuesta vacía en caso de éxito según el proto.
 * Coincide con CambiarPasswordRequest.Response del proto.
 */
public record CambiarPasswordResponseDto(
) {}