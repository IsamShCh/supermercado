package com.isam.dto;

import java.math.BigDecimal;

public record AnadirProductoTicketResponseDto(
    String idTicketTemporal,
    String sku,
    String idItemTicket,
    Integer numeroLinea,
    String nombreProducto,
    BigDecimal cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal,
    BigDecimal subtotalTicketActual
) {
}