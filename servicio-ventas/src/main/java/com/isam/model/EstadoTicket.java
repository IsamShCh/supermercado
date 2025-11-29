package com.isam.model;

/**
 * Enumeración que representa el estado de un ticket
 * Según el diagrama E-R: "Temporal, Cerrado, Cancelado, Pagado"
 */
public enum EstadoTicket {
    TEMPORAL,
    CERRADO,
    CANCELADO,
    PAGADO
}