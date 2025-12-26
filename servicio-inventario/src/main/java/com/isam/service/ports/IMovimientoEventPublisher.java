package com.isam.service.ports;

import com.isam.model.MovimientoInventario;

public interface IMovimientoEventPublisher {
    void publicarMovimiento(MovimientoInventario movimiento);
}
