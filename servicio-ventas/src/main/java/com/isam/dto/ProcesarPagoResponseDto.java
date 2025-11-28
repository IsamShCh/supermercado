package com.isam.dto;

import com.isam.model.MetodoPago;

import java.math.BigDecimal;

public record ProcesarPagoResponseDto(
    String idPago,
    String idTicketTemporal,
    MetodoPago metodoPago,
    BigDecimal montoRecibido,
    BigDecimal montoCambio
) {}