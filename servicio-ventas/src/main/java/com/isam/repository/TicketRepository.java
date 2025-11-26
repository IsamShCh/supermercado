package com.isam.repository;

import com.isam.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {


    // Consulta nativa de PostgreSQL para obtener el siguiente número
    @Query(value = "SELECT nextval('ticket_fiscal_seq')", nativeQuery = true)
    Long getNextTicketNumber();
}