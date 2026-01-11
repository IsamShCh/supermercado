package com.isam.service;

import com.isam.dto.CancelarTicketRequestDto;
import com.isam.dto.CancelarTicketResponseDto;
import com.isam.integration.client.InventarioGrpcClient;
import com.isam.service.ports.IProveedorCatalogo;
import com.isam.model.EstadoTicket;
import com.isam.model.MetodoPago;
import com.isam.model.Pago;
import com.isam.model.Ticket;
import com.isam.repository.PagoRepository;
import com.isam.repository.TicketRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para VentasService - Funcionalidad de Cancelar Ticket (AC25).
 * Prueba la funcionalidad de cancelación de tickets temporales y pagados.
 */
@ExtendWith(MockitoExtension.class)
class VentasServiceCancelarTicketTest {

    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private PagoRepository pagoRepository;
    
    @Mock
    private IProveedorCatalogo proveedorCatalogo;
    
    @Mock
    private InventarioGrpcClient inventarioGrpcClient;

    @InjectMocks
    private VentasService ventasService;

    private String idTicket;
    private Ticket ticketTemporal;
    private Ticket ticketPagado;
    private Ticket ticketCerrado;
    private Ticket ticketCancelado;

    @BeforeEach
    void setUp() {
        // Limpiar y preparar datos de prueba
        idTicket = UUID.randomUUID().toString();
        
        // Crear ticket TEMPORAL
        ticketTemporal = new Ticket();
        ticketTemporal.setIdTicket(idTicket);
        ticketTemporal.setIdUsuario("user123");
        ticketTemporal.setFechaHora(LocalDateTime.now());
        ticketTemporal.setEstadoTicket(EstadoTicket.TEMPORAL);
        ticketTemporal.setSubtotal(BigDecimal.ZERO);
        
        // Crear ticket PAGADO (con pago asociado)
        ticketPagado = new Ticket();
        ticketPagado.setIdTicket(UUID.randomUUID().toString());
        ticketPagado.setIdUsuario("user123");
        ticketPagado.setFechaHora(LocalDateTime.now());
        ticketPagado.setEstadoTicket(EstadoTicket.PAGADO);
        ticketPagado.setSubtotal(new BigDecimal("40.00"));
        
        Pago pago = new Pago();
        pago.setIdPago(UUID.randomUUID().toString());
        pago.setTicket(ticketPagado);
        pago.setMetodoPago(MetodoPago.EFECTIVO);
        pago.setMontoRecibido(new BigDecimal("50.00"));
        pago.setMontoCambio(new BigDecimal("10.00"));
        pago.setFechaHora(LocalDateTime.now());
        
        ticketPagado.setPago(pago);
        ticketPagado.setIdPago(pago.getIdPago());
        
        // Crear ticket CERRADO
        ticketCerrado = new Ticket();
        ticketCerrado.setIdTicket(UUID.randomUUID().toString());
        ticketCerrado.setIdUsuario("user123");
        ticketCerrado.setFechaHora(LocalDateTime.now());
        ticketCerrado.setEstadoTicket(EstadoTicket.CERRADO);
        ticketCerrado.setNumeroTicket("T-2025-0000001");
        
        // Crear ticket CANCELADO
        ticketCancelado = new Ticket();
        ticketCancelado.setIdTicket(UUID.randomUUID().toString());
        ticketCancelado.setIdUsuario("user123");
        ticketCancelado.setFechaHora(LocalDateTime.now());
        ticketCancelado.setEstadoTicket(EstadoTicket.CANCELADO);
    }

    @Test
    void cancelarTicket_TicketTemporal_CancelaExitosamente() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketTemporal.getIdTicket());
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicket());
        assertTrue(resultado.montoADevolver().isEmpty());
        assertTrue(resultado.metodoPagoOriginal().isEmpty());
        assertEquals(EstadoTicket.CANCELADO, ticketTemporal.getEstadoTicket());
        
        verify(ticketRepository, times(1)).findById(ticketTemporal.getIdTicket());
        verify(ticketRepository, times(1)).save(ticketTemporal);
    }

    @Test
    void cancelarTicket_TicketPagado_CancelaYRetornaMontoADevolver() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketPagado.getIdTicket());
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketPagado);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketPagado.getIdTicket(), resultado.idTicket());
        assertTrue(resultado.montoADevolver().isPresent());
        assertTrue(resultado.metodoPagoOriginal().isPresent());
        assertEquals(new BigDecimal("50.00"), resultado.montoADevolver().get());
        assertEquals(MetodoPago.EFECTIVO, resultado.metodoPagoOriginal().get());
        assertEquals(EstadoTicket.CANCELADO, ticketPagado.getEstadoTicket());
        
        verify(ticketRepository, times(1)).findById(ticketPagado.getIdTicket());
        verify(ticketRepository, times(1)).save(ticketPagado);
    }

    @Test
    void cancelarTicket_TicketPagadoConTarjeta_RetornaMetodoPagoCorrecto() {
        // Given
        ticketPagado.getPago().setMetodoPago(MetodoPago.TARJETA_CREDITO);
        ticketPagado.getPago().setMontoRecibido(new BigDecimal("100.50"));
        
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketPagado.getIdTicket());
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketPagado);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.montoADevolver().isPresent());
        assertTrue(resultado.metodoPagoOriginal().isPresent());
        assertEquals(new BigDecimal("100.50"), resultado.montoADevolver().get());
        assertEquals(MetodoPago.TARJETA_CREDITO, resultado.metodoPagoOriginal().get());
    }

    @Test
    void cancelarTicket_IdTicketVacio_LanzaExcepcion() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto("");

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.cancelarTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("ID del ticket es obligatorio"));
        
        verify(ticketRepository, never()).findById(anyString());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelarTicket_IdTicketNull_LanzaExcepcion() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(null);

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.cancelarTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("ID del ticket es obligatorio"));
    }

    @Test
    void cancelarTicket_TicketNoExiste_LanzaExcepcion() {
        // Given
        String idTicketInexistente = "ticket-inexistente";
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(idTicketInexistente);
        
        when(ticketRepository.findById(idTicketInexistente))
            .thenReturn(Optional.empty());

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.cancelarTicket(dto)
        );

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Ticket no encontrado con ID"));
        assertTrue(exception.getMessage().contains(idTicketInexistente));
        
        verify(ticketRepository, times(1)).findById(idTicketInexistente);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelarTicket_TicketCerrado_LanzaExcepcion() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketCerrado.getIdTicket());
        
        when(ticketRepository.findById(ticketCerrado.getIdTicket()))
            .thenReturn(Optional.of(ticketCerrado));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.cancelarTicket(dto)
        );

        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("El ticket está CERRADO"));
        assertTrue(exception.getMessage().contains("No se puede cancelar"));
        assertTrue(exception.getMessage().contains("devoluciones"));
        
        verify(ticketRepository, times(1)).findById(ticketCerrado.getIdTicket());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelarTicket_TicketYaCancelado_LanzaExcepcion() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketCancelado.getIdTicket());
        
        when(ticketRepository.findById(ticketCancelado.getIdTicket()))
            .thenReturn(Optional.of(ticketCancelado));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.cancelarTicket(dto)
        );

        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("El ticket ya está CANCELADO"));
        
        verify(ticketRepository, times(1)).findById(ticketCancelado.getIdTicket());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelarTicket_TicketTemporal_NoRetornaMontoADevolver() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketTemporal.getIdTicket());
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.montoADevolver().isPresent());
        assertFalse(resultado.metodoPagoOriginal().isPresent());
    }

    @Test
    void cancelarTicket_TicketPagado_CambiaEstadoACancelado() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketPagado.getIdTicket());
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketPagado);

        // When
        ventasService.cancelarTicket(dto);

        // Then
        assertEquals(EstadoTicket.CANCELADO, ticketPagado.getEstadoTicket());
        verify(ticketRepository, times(1)).save(argThat(ticket ->
            EstadoTicket.CANCELADO.equals(ticket.getEstadoTicket())
        ));
    }

    @Test
    void cancelarTicket_TicketPagadoConTransferencia_RetornaInformacionCompleta() {
        // Given
        ticketPagado.getPago().setMetodoPago(MetodoPago.TRANSFERENCIA);
        ticketPagado.getPago().setMontoRecibido(new BigDecimal("250.75"));
        ticketPagado.getPago().setMontoCambio(BigDecimal.ZERO);
        
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketPagado.getIdTicket());
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketPagado);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketPagado.getIdTicket(), resultado.idTicket());
        assertTrue(resultado.montoADevolver().isPresent());
        assertTrue(resultado.metodoPagoOriginal().isPresent());
        assertEquals(new BigDecimal("250.75"), resultado.montoADevolver().get());
        assertEquals(MetodoPago.TRANSFERENCIA, resultado.metodoPagoOriginal().get());
    }

    @Test
    void cancelarTicket_ValidaEstructuraRespuesta() {
        // Given
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketTemporal.getIdTicket());
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.idTicket());
        assertNotNull(resultado.montoADevolver());
        assertNotNull(resultado.metodoPagoOriginal());
        assertFalse(resultado.idTicket().isEmpty());
    }

    @Test
    void cancelarTicket_TicketPagado_GuardaCambiosCorrectamente() {
        // Given
        String idTicketOriginal = ticketPagado.getIdTicket();
        EstadoTicket estadoOriginal = ticketPagado.getEstadoTicket();
        
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketPagado.getIdTicket());
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketPagado);

        // When
        ventasService.cancelarTicket(dto);

        // Then
        assertEquals(idTicketOriginal, ticketPagado.getIdTicket());
        assertNotEquals(estadoOriginal, ticketPagado.getEstadoTicket());
        assertEquals(EstadoTicket.CANCELADO, ticketPagado.getEstadoTicket());
        
        verify(ticketRepository, times(1)).save(ticketPagado);
    }

    @Test
    void cancelarTicket_TicketPagadoConTarjetaDebito_FuncionaCorrectamente() {
        // Given
        ticketPagado.getPago().setMetodoPago(MetodoPago.TARJETA_DEBITO);
        ticketPagado.getPago().setMontoRecibido(new BigDecimal("75.25"));
        
        CancelarTicketRequestDto dto = new CancelarTicketRequestDto(ticketPagado.getIdTicket());
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketPagado);

        // When
        CancelarTicketResponseDto resultado = ventasService.cancelarTicket(dto);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.montoADevolver().isPresent());
        assertTrue(resultado.metodoPagoOriginal().isPresent());
        assertEquals(new BigDecimal("75.25"), resultado.montoADevolver().get());
        assertEquals(MetodoPago.TARJETA_DEBITO, resultado.metodoPagoOriginal().get());
        assertEquals(EstadoTicket.CANCELADO, ticketPagado.getEstadoTicket());
    }
}