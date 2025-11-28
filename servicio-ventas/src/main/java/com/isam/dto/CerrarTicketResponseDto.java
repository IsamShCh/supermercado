package com.isam.dto;

import com.isam.model.MetodoPago;

import java.math.BigDecimal;
import java.util.List;

public record CerrarTicketResponseDto(
    String numeroTicket,
    String fechaHora,
    String nombreCajero,
    List<LineaVentaDto> lineasVenta,
    BigDecimal subtotal,
    BigDecimal totalImpuestos,
    BigDecimal total,
    MetodoPago metodoPago,
    BigDecimal montoRecibido,
    BigDecimal montoCambio
) {}