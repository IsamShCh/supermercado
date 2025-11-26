package com.isam.dto;

public record CrearNuevoTicketResponseDto(
    String idTicketTemporal,
    String numeroTicket,
    String fechaHoraCreacion,
    String nombreCajero
) {
}