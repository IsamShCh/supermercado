package com.isam.service;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.ConsultarTicketRequestDto;
import com.isam.dto.ConsultarTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.grpc.catalogo.ProductoProto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.model.EstadoTicket;
import com.isam.model.Ticket;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VentasService usando mocks.
 * Prueban la lógica de negocio de manera aislada.
 */
@ExtendWith(MockitoExtension.class)
class VentasServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private CatalogoGrpcClient catalogoGrpcClient;

    @InjectMocks
    private VentasService ventasService;

    private String idUsuario;
    private String nombreCajero;
    private Ticket ticketTemporal;

    @BeforeEach
    void setUp() {
        idUsuario = "user123";
        nombreCajero = "Juan Pérez";
        
        // Crear ticket temporal para tests
        ticketTemporal = new Ticket();
        ticketTemporal.setIdTicket(UUID.randomUUID().toString());
        ticketTemporal.setNumeroTicket("T-2025-0000001");
        ticketTemporal.setIdUsuario(idUsuario);
        ticketTemporal.setEstadoTicket(EstadoTicket.TEMPORAL);
        ticketTemporal.setFechaHora(java.time.LocalDateTime.now());
        ticketTemporal.setSubtotal(BigDecimal.ZERO);
    }

    @Test
    void crearNuevoTicket_DatosValidos_CreaTicketExitosamente() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketTemporal);

        // When
        CrearNuevoTicketResponseDto resultado = ventasService.crearNuevoTicket(idUsuario, nombreCajero);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicketTemporal());
        assertEquals(nombreCajero, resultado.nombreCajero());
        assertNotNull(resultado.fechaHoraCreacion());

        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void crearNuevoTicket_DebeAsignarEstadoTemporal() {
        // Given
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketTemporal);

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
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticketTemporal);

        // When
        ventasService.crearNuevoTicket(idUsuario, nombreCajero);

        // Then
        verify(ticketRepository, times(1)).save(argThat(ticket ->
            idUsuario.equals(ticket.getIdUsuario())
        ));
    }
    
    @Test
    void anadirProductoTicket_ProductoNuevo_AñadeProductoExitosamente() {
        // Given
        String codigoBarras = "1234567890123";
        String sku = "SKU-001";
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            codigoBarras
        );
        
        ProductoProto producto = ProductoProto.newBuilder()
            .setSku(sku)
            .setNombre("Producto Test")
            .setPrecioVenta("10.50")
            .build();
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.traducirCodigoBarrasASku(codigoBarras))
            .thenReturn(sku);
        when(catalogoGrpcClient.consultarProducto(sku))
            .thenReturn(producto);
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);
        
        // When
        AnadirProductoTicketResponseDto resultado = ventasService.anadirProductoTicket(dto);
        
        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicketTemporal());
        assertEquals(sku, resultado.sku());
        assertEquals("Producto Test", resultado.nombreProducto());
        assertEquals(BigDecimal.ONE, resultado.cantidad());
        assertEquals(new BigDecimal("10.50"), resultado.precioUnitario());
        
        verify(catalogoGrpcClient, times(1)).traducirCodigoBarrasASku(codigoBarras);
        verify(catalogoGrpcClient, times(1)).consultarProducto(sku);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }
    
    @Test
    void anadirProductoTicket_TicketNoExiste_LanzaExcepcion() {
        // Given
        String idTicketInexistente = "ticket-inexistente";
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            idTicketInexistente,
            "1234567890123"
        );
        
        when(ticketRepository.findById(idTicketInexistente))
            .thenReturn(Optional.empty());
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.anadirProductoTicket(dto)
        );
        
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Ticket temporal no encontrado"));
    }
    
    @Test
    void anadirProductoTicket_TicketCerrado_LanzaExcepcion() {
        // Given
        ticketTemporal.setEstadoTicket(EstadoTicket.CERRADO);
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "1234567890123"
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.anadirProductoTicket(dto)
        );
        
        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("no está en estado TEMPORAL"));
    }
    
    @Test
    void anadirProductoTicket_ProductoNoEncontrado_LanzaExcepcion() {
        // Given
        String codigoBarras = "9999999999999";
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            codigoBarras
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.traducirCodigoBarrasASku(codigoBarras))
            .thenReturn(null);
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.anadirProductoTicket(dto)
        );
        
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("No se encontró producto"));
    }
    
    @Test
    void anadirProductoTicket_IdTicketVacio_LanzaExcepcion() {
        // Given
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            "",
            "1234567890123"
        );
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.anadirProductoTicket(dto)
        );
        
        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("ID del ticket temporal es obligatorio"));
    }
    
    @Test
    void anadirProductoTicket_CodigoBarrasVacio_LanzaExcepcion() {
        // Given
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            ""
        );
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.anadirProductoTicket(dto)
        );
        
        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("código de barras es obligatorio"));
    }
    
    @Test
    void consultarTicket_PorIdTicket_RetornaTicketCorrectamente() {
        // Given
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        
        ConsultarTicketRequestDto dto = new ConsultarTicketRequestDto(
            ticketTemporal.getIdTicket(),
            null
        );
        
        // When
        ConsultarTicketResponseDto resultado = ventasService.consultarTicket(dto);
        
        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicket());
        assertEquals(ticketTemporal.getEstadoTicket(), resultado.estado());
        assertEquals("Cajero " + idUsuario, resultado.nombreCajero());
        
        verify(ticketRepository, times(1)).findById(ticketTemporal.getIdTicket());
    }
    
    @Test
    void consultarTicket_PorNumeroTicket_RetornaTicketCorrectamente() {
        // Given
        ticketTemporal.setNumeroTicket("T-2025-0000001");
        when(ticketRepository.findByNumeroTicket(ticketTemporal.getNumeroTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        
        ConsultarTicketRequestDto dto = new ConsultarTicketRequestDto(
            null,
            ticketTemporal.getNumeroTicket()
        );
        
        // When
        ConsultarTicketResponseDto resultado = ventasService.consultarTicket(dto);
        
        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicket());
        assertEquals(ticketTemporal.getNumeroTicket(), resultado.numeroTicket());
        assertEquals(ticketTemporal.getEstadoTicket(), resultado.estado());
        
        verify(ticketRepository, times(1)).findByNumeroTicket(ticketTemporal.getNumeroTicket());
    }
    
    @Test
    void consultarTicket_IdTicketNoExiste_LanzaExcepcion() {
        // Given
        String idTicketInexistente = "ticket-inexistente";
        ConsultarTicketRequestDto dto = new ConsultarTicketRequestDto(
            idTicketInexistente,
            null
        );
        
        when(ticketRepository.findById(idTicketInexistente))
            .thenReturn(Optional.empty());
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.consultarTicket(dto)
        );
        
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Ticket no encontrado con ID"));
    }
    
    @Test
    void consultarTicket_NumeroTicketNoExiste_LanzaExcepcion() {
        // Given
        String numeroTicketInexistente = "T-2025-9999999";
        ConsultarTicketRequestDto dto = new ConsultarTicketRequestDto(
            null,
            numeroTicketInexistente
        );
        
        when(ticketRepository.findByNumeroTicket(numeroTicketInexistente))
            .thenReturn(Optional.empty());
        
        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.consultarTicket(dto)
        );
        
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Ticket no encontrado con número"));
    }
}