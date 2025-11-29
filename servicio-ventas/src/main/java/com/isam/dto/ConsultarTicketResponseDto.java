package com.isam.dto;

import com.isam.model.EstadoTicket;
import com.isam.model.MetodoPago;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de salida para consultar un ticket.
 * Contiene toda la información del ticket incluyendo líneas de venta y pago.
 */
public record ConsultarTicketResponseDto(
    String idTicket,
    String numeroTicket,
    String fechaHora,
    String nombreCajero,
    List<LineaVentaDto> lineasVenta,
    BigDecimal subtotal,
    BigDecimal totalImpuestos,
    BigDecimal total,
    MetodoPago metodoPago,
    BigDecimal montoRecibido,
    BigDecimal montoCambio,
    EstadoTicket estado
) {
}