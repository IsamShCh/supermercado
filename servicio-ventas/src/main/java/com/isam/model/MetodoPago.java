package com.isam.model;

/**
 * Enumeración que representa el método de pago
 * Según el diagrama E-R: "Efectivo, TarjetaDebito, TarjetaCredito, Transferencia"
 */
public enum MetodoPago {
    EFECTIVO,
    TARJETA_DEBITO,
    TARJETA_CREDITO,
    TRANSFERENCIA
}