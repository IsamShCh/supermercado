package com.isam.service;

import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.model.EstadoTicket;
import com.isam.model.Ticket;
import com.isam.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentasServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private VentasService ventasService;

    private String idUsuario;
    private String nombreCajero;

    @BeforeEach
    void setUp() {
        idUsuario = "user123";
        nombreCajero = "Juan Pérez";
    }

    @Test
    void crearNuevoTicket_DebeCrearTicketTemporalExitosamente() {
        // Given
        Ticket ticketGuardado = new Ticket();
        ticketGuardado.setIdTicket(UUID.randomUUID().toString());
        ticketGuardado.setIdUsuario(idUsuario);
        ticketGuardado.setEstadoTicket(EstadoTicket.TEMPORAL);
        ticketGuardado.setFechaHora(java.time.LocalDateTime.now());

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketGuardado);

        // When
        CrearNuevoTicketResponseDto resultado = ventasService.crearNuevoTicket(idUsuario, nombreCajero);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketGuardado.getIdTicket(), resultado.idTicketTemporal());
        assertEquals(nombreCajero, resultado.nombreCajero());
        assertNotNull(resultado.fechaHoraCreacion());

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void crearNuevoTicket_DebeAsignarEstadoTemporal() {
        // Given
        Ticket ticketGuardado = new Ticket();
        ticketGuardado.setIdTicket(UUID.randomUUID().toString());
        ticketGuardado.setIdUsuario(idUsuario);
        ticketGuardado.setEstadoTicket(EstadoTicket.TEMPORAL);
        ticketGuardado.setFechaHora(java.time.LocalDateTime.now());

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketGuardado);

        // When
        ventasService.crearNuevoTicket(idUsuario, nombreCajero);

        // Then
        verify(ticketRepository, times(1)).save(argThat(ticket -> 
            EstadoTicket.TEMPORAL.equals(ticket.getEstadoTicket())
        ));
    }

    @Test
    void crearNuevoTicket_DebeAsignarIdUsuarioCorrecto() {
        // Given
        Ticket ticketGuardado = new Ticket();
        ticketGuardado.setIdTicket(UUID.randomUUID().toString());
        ticketGuardado.setIdUsuario(idUsuario);
        ticketGuardado.setEstadoTicket(EstadoTicket.TEMPORAL);
        ticketGuardado.setFechaHora(java.time.LocalDateTime.now());

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketGuardado);

        // When
        ventasService.crearNuevoTicket(idUsuario, nombreCajero);

        // Then
        verify(ticketRepository, times(1)).save(argThat(ticket ->
            idUsuario.equals(ticket.getIdUsuario())
        ));
    }
}