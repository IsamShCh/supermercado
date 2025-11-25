package com.isam.service;

import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.model.EstadoTicket;
import com.isam.model.Ticket;
import com.isam.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.Status;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Transactional
public class VentasService {

    private final TicketRepository ticketRepository;

    @Transactional
    public CrearNuevoTicketResponseDto crearNuevoTicket(String idUsuario, String nombreCajero) {
        
        // Validar que el ID de usuario no sea nulo o vacío
        if (!isNotNullOrEmpty(idUsuario)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Problema con credenciales login. El ID de usuario es obligatorio.")
                .asRuntimeException();
        }
        
        // Validar que el nombre del cajero no sea nulo o vacío
        if (!isNotNullOrEmpty(nombreCajero)) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Problema con credenciales login. El nombre del cajero es obligatorio")
                .asRuntimeException();
        }
        
        // Crear nueva entidad Ticket
        Ticket ticket = new Ticket();
        ticket.setIdUsuario(idUsuario.trim());
        ticket.setFechaHora(LocalDateTime.now());
        ticket.setEstadoTicket(EstadoTicket.TEMPORAL);
        
        // Guardar en base de datos
        Ticket ticketGuardado = ticketRepository.save(ticket);
        
        // Formatear fecha y hora en formato ISO 8601
        String fechaHoraFormateada = ticketGuardado.getFechaHora()
            .toString();
        
        // Construir y retornar respuesta
        return new CrearNuevoTicketResponseDto(
            ticketGuardado.getIdTicket(),
            fechaHoraFormateada,
            nombreCajero.trim()
        );
    }
    
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}