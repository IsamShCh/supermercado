package com.isam.dto;

public record CrearNuevoTicketResponseDto(
    String idTicketTemporal,
    String fechaHoraCreacion,
    String nombreCajero
) {
}