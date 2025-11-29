package com.isam.repository;

import com.isam.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {


    // Consulta nativa de PostgreSQL para obtener el siguiente número
    @Query(value = "SELECT nextval('ticket_fiscal_seq')", nativeQuery = true)
    Long getNextTicketNumber();
    
    /**
     * Busca un ticket por su número de ticket
     * @param numeroTicket El número de ticket único
     * @return Optional con el ticket si existe
     */
    Optional<Ticket> findByNumeroTicket(String numeroTicket);
}