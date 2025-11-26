package com.isam.dto;

public record AnadirProductoTicketResponseDto(
    String idTicketTemporal,
    String sku,
    String idItemTicket,
    Integer numeroLinea,
    String nombreProducto,
    String cantidad,
    String precioUnitario,
    String subtotal,
    String subtotalTicketActual
) {
}