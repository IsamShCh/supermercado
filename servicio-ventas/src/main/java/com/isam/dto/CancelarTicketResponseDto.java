package com.isam.dto;

import com.isam.model.MetodoPago;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * DTO para la respuesta de cancelación de ticket temporal
 * Corresponde al AC25R: Confirmación de ticket cancelado
 */
public record CancelarTicketResponseDto(
    
    String idTicket,
    Optional<BigDecimal> montoADevolver,
    Optional<MetodoPago> metodoPagoOriginal
    
) {
}