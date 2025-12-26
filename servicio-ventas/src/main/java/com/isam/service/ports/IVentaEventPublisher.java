package com.isam.service.ports;

import com.isam.model.ItemTicket;
import com.isam.model.Ticket;
import java.util.List;

public interface IVentaEventPublisher {
    void publicarVenta(Ticket ticket, List<ItemTicket> items, String metodoPago);
}
