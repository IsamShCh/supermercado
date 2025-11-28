package com.isam.dto;

import com.isam.model.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProcesarPagoRequestDto(
    @NotBlank(message = "El ID del ticket temporal es obligatorio")
    String idTicketTemporal,
    
    @NotNull(message = "El método de pago es obligatorio")
    MetodoPago metodoPago,
    
    @NotNull(message = "El monto recibido es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto recibido debe ser mayor que 0")
    @Digits(integer = 8, fraction = 2, message = "El monto recibido debe tener máximo 8 dígitos enteros y 2 decimales")
    BigDecimal montoRecibido
) {}