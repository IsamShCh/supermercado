package com.isam.dto;

import java.math.BigDecimal;

public record EliminarProductoTicketResponseDto(
    String idTicket,
    String sku,
    String nombreProducto,
    BigDecimal cantidadEliminada,
    BigDecimal cantidadRestante,
    Boolean itemEliminadoCompletamente,
    BigDecimal subtotalTicketActual
) {
}