package com.isam.model;

public enum TipoMovimiento {
    // Movimientos que INCREMENTAN el stock (cantidad positiva = más inventario)
    ENTRADA,        // Ingreso de nueva mercancía
    DEVOLUCION,     // Devoluciones de clientes
    AJUSTE_POSITIVO, // Ajustes manuales que incrementan
    
    // Movimientos que DECREMENTAN el stock (cantidad positiva = menos inventario)  
    SALIDA,         // Salida general de productos
    VENTA,          // Ventas a clientes
    MERMA,          // Pérdidas por deterioro
    AJUSTE_NEGATIVO, // Ajustes manuales que decrementan
    
    // Movimientos NEUTROS (no afectan stock total, solo ubicación)
    TRASLADO_ESTANTERIA; // Movimiento entre almacén y estantería
    
    /**
     * Determina si este tipo de movimiento incrementa el stock total del inventario.
     * @return true si incrementa, false si decrementa o es neutro
     */
    public boolean incrementaStock() {
        return switch (this) {
            case ENTRADA, DEVOLUCION, AJUSTE_POSITIVO -> true;
            default -> false;
        };
    }
    
    /**
     * Determina si este tipo de movimiento decrementa el stock total del inventario.
     * @return true si decrementa, false si incrementa o es neutro
     */
    public boolean decrementaStock() {
        return switch (this) {
            case SALIDA, VENTA, MERMA, AJUSTE_NEGATIVO -> true;
            default -> false;
        };
    }
    
    /**
     * Determina si este tipo de movimiento es neutro (no afecta stock total).
     * @return true si es neutro, false si incrementa o decrementa
     */
    public boolean esNeutro() {
        return switch (this) {
            case TRASLADO_ESTANTERIA -> true;
            default -> false;
        };
    }
    
    /**
     * Obtiene el tipo de movimiento de ajuste apropiado según el signo de la cantidad.
     * @param cantidad La cantidad del ajuste (puede ser positiva o negativa)
     * @return AJUSTE_POSITIVO si la cantidad >= 0, AJUSTE_NEGATIVO si < 0
     */
    public static TipoMovimiento ajustePorCantidad(java.math.BigDecimal cantidad) {
        return cantidad.compareTo(java.math.BigDecimal.ZERO) >= 0 
            ? AJUSTE_POSITIVO 
            : AJUSTE_NEGATIVO;
    }
}