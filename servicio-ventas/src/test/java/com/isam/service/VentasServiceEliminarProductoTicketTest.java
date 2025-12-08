package com.isam.service;

import com.isam.dto.CatalogoClient.ProductoDto;
import com.isam.dto.EliminarProductoTicketRequestDto;
import com.isam.dto.EliminarProductoTicketResponseDto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.grpc.client.InventarioGrpcClient;
import com.isam.model.EstadoTicket;
import com.isam.model.ItemTicket;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para VentasService - Funcionalidad de Eliminar Producto de Ticket.
 * Prueba la funcionalidad de eliminación parcial y total de productos de tickets temporales.
 */
@ExtendWith(MockitoExtension.class)
class VentasServiceEliminarProductoTicketTest {

    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private PagoRepository pagoRepository;
    
    @Mock
    private CatalogoGrpcClient catalogoGrpcClient;
    
    @Mock
    private InventarioGrpcClient inventarioGrpcClient;

    @InjectMocks
    private VentasService ventasService;

    private String idTicket;
    private Ticket ticketTemporal;
    private Ticket ticketPagado;
    private Ticket ticketCerrado;
    private ItemTicket itemExistente;
    private ProductoDto productoMock;

    @BeforeEach
    void setUp() {
        // Crear ProductoDto mock con todos los campos
        productoMock = new ProductoDto(
            "PROD001",           // sku
            null,                // ean
            null,                // plu
            "Producto Test",     // nombre
            null,                // descripcion
            BigDecimal.valueOf(20.00), // precioVenta
            false,               // caduca
            false,               // esGranel
            null,                // categoria
            null,                // politicaRotacion
            com.isam.model.UnidadMedida.UNIDAD,            // unidadMedida
            List.of(),           // etiquetas
            "ACTIVO"             // estado
        );
        
        // Limpiar y preparar datos de prueba
        idTicket = UUID.randomUUID().toString();
        
        // Crear ticket TEMPORAL con items
        ticketTemporal = new Ticket();
        ticketTemporal.setIdTicket(idTicket);
        ticketTemporal.setIdUsuario("user123");
        ticketTemporal.setFechaHora(LocalDateTime.now());
        ticketTemporal.setEstadoTicket(EstadoTicket.TEMPORAL);
        ticketTemporal.setSubtotal(new BigDecimal("60.00"));
        
        // Crear item existente en el ticket
        itemExistente = new ItemTicket();
        itemExistente.setIdItemTicket(UUID.randomUUID().toString());
        itemExistente.setTicket(ticketTemporal);
        itemExistente.setNumeroLinea(1);
        itemExistente.setSku("PROD001");
        itemExistente.setNombreProducto("Producto Test");
        itemExistente.setCantidad(new BigDecimal("3"));
        itemExistente.setPrecioUnitario(new BigDecimal("20.00"));
        itemExistente.setDescuento(BigDecimal.ZERO);
        itemExistente.calcularSubtotal(); // 3 * 20.00 = 60.00
        
        ticketTemporal.getItems().add(itemExistente);
        
        // Crear ticket PAGADO
        ticketPagado = new Ticket();
        ticketPagado.setIdTicket(UUID.randomUUID().toString());
        ticketPagado.setIdUsuario("user123");
        ticketPagado.setFechaHora(LocalDateTime.now());
        ticketPagado.setEstadoTicket(EstadoTicket.PAGADO);
        ticketPagado.setSubtotal(new BigDecimal("40.00"));
        
        // Crear ticket CERRADO
        ticketCerrado = new Ticket();
        ticketCerrado.setIdTicket(UUID.randomUUID().toString());
        ticketCerrado.setIdUsuario("user123");
        ticketCerrado.setFechaHora(LocalDateTime.now());
        ticketCerrado.setEstadoTicket(EstadoTicket.CERRADO);
        ticketCerrado.setNumeroTicket("T-2025-0000001");
    }

    @Test
    void eliminarProductoTicket_EliminacionTotalExitosa_EliminaCompletamente() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.empty() // Eliminar todo
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenReturn(productoMock);
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        EliminarProductoTicketResponseDto resultado = ventasService.eliminarProductoTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicket());
        assertEquals("PROD001", resultado.sku());
        assertEquals("Producto Test", resultado.nombreProducto());
        assertEquals(new BigDecimal("3"), resultado.cantidadEliminada());
        assertEquals(0, resultado.cantidadRestante().compareTo(BigDecimal.ZERO));
        assertTrue(resultado.itemEliminadoCompletamente());
        assertEquals(0, resultado.subtotalTicketActual().compareTo(BigDecimal.ZERO));
        
        verify(ticketRepository, times(1)).findById(ticketTemporal.getIdTicket());
        verify(ticketRepository, times(1)).save(ticketTemporal);
    }

    @Test
    void eliminarProductoTicket_EliminacionParcial_ReduceCantidad() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(new BigDecimal("1")) // Eliminar 1 de 3
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenReturn(productoMock);
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        EliminarProductoTicketResponseDto resultado = ventasService.eliminarProductoTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(ticketTemporal.getIdTicket(), resultado.idTicket());
        assertEquals("PROD001", resultado.sku());
        assertEquals("Producto Test", resultado.nombreProducto());
        assertEquals(new BigDecimal("1"), resultado.cantidadEliminada());
        assertEquals(new BigDecimal("2"), resultado.cantidadRestante());
        assertFalse(resultado.itemEliminadoCompletamente());
        assertEquals(new BigDecimal("40.00"), resultado.subtotalTicketActual()); // 2 * 20.00
        
        verify(ticketRepository, times(1)).findById(ticketTemporal.getIdTicket());
        verify(ticketRepository, times(1)).save(ticketTemporal);
    }

    @Test
    void eliminarProductoTicket_IdTicketVacio_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            "",
            "PROD001",
            Optional.empty()
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("ID del ticket es obligatorio"));
        
        verify(ticketRepository, never()).findById(anyString());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void eliminarProductoTicket_IdTicketNull_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            null,
            "PROD001",
            Optional.empty()
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("ID del ticket es obligatorio"));
    }

    @Test
    void eliminarProductoTicket_SkuVacio_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "",
            Optional.empty()
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("SKU del producto es obligatorio"));
    }

    @Test
    void eliminarProductoTicket_SkuNull_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            null,
            Optional.empty()
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("SKU del producto es obligatorio"));
    }

    @Test
    void eliminarProductoTicket_CantidadNegativa_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(new BigDecimal("-1"))
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("cantidad a eliminar debe ser mayor que 0"));
    }

    @Test
    void eliminarProductoTicket_CantidadCero_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(BigDecimal.ZERO)
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("cantidad a eliminar debe ser mayor que 0"));
    }

    @Test
    void eliminarProductoTicket_TicketNoExiste_LanzaExcepcion() {
        // Given
        String idTicketInexistente = "ticket-inexistente";
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            idTicketInexistente,
            "PROD001",
            Optional.empty()
        );
        
        when(ticketRepository.findById(idTicketInexistente))
            .thenReturn(Optional.empty());

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Ticket no encontrado con ID"));
        assertTrue(exception.getMessage().contains(idTicketInexistente));
        
        verify(ticketRepository, times(1)).findById(idTicketInexistente);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void eliminarProductoTicket_TicketPagado_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketPagado.getIdTicket(),
            "PROD001",
            Optional.empty()
        );
        
        when(ticketRepository.findById(ticketPagado.getIdTicket()))
            .thenReturn(Optional.of(ticketPagado));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("El ticket ya está PAGADO"));
        assertTrue(exception.getMessage().contains("No se pueden eliminar productos"));
        
        verify(ticketRepository, times(1)).findById(ticketPagado.getIdTicket());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void eliminarProductoTicket_TicketCerrado_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketCerrado.getIdTicket(),
            "PROD001",
            Optional.empty()
        );
        
        when(ticketRepository.findById(ticketCerrado.getIdTicket()))
            .thenReturn(Optional.of(ticketCerrado));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("El ticket no está en estado TEMPORAL"));
        
        verify(ticketRepository, times(1)).findById(ticketCerrado.getIdTicket());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void eliminarProductoTicket_ProductoNoExisteEnTicket_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD999", // SKU que no existe en el ticket
            Optional.empty()
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("El producto con SKU 'PROD999' no se encuentra en el ticket"));
        
        verify(ticketRepository, times(1)).findById(ticketTemporal.getIdTicket());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void eliminarProductoTicket_CantidadMayorALaExistente_LanzaExcepcion() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(new BigDecimal("5")) // Mayor que la cantidad existente (3)
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenReturn(productoMock);

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.eliminarProductoTicket(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("cantidad a eliminar"));
        assertTrue(exception.getMessage().contains("es mayor que la cantidad existente"));
        
        verify(ticketRepository, times(1)).findById(ticketTemporal.getIdTicket());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void eliminarProductoTicket_EliminacionParcialCalculaSubtotalCorrectamente() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(new BigDecimal("2")) // Eliminar 2 de 3, queda 1
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenReturn(productoMock);
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        EliminarProductoTicketResponseDto resultado = ventasService.eliminarProductoTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("2"), resultado.cantidadEliminada());
        assertEquals(new BigDecimal("1"), resultado.cantidadRestante());
        assertFalse(resultado.itemEliminadoCompletamente());
        assertEquals(new BigDecimal("20.00"), resultado.subtotalTicketActual()); // 1 * 20.00
    }

    @Test
    void eliminarProductoTicket_ValidaEstructuraRespuesta() {
        // Given
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(new BigDecimal("1"))
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenReturn(productoMock);
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        EliminarProductoTicketResponseDto resultado = ventasService.eliminarProductoTicket(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.idTicket());
        assertNotNull(resultado.sku());
        assertNotNull(resultado.nombreProducto());
        assertNotNull(resultado.cantidadEliminada());
        assertNotNull(resultado.cantidadRestante());
        assertNotNull(resultado.itemEliminadoCompletamente());
        assertNotNull(resultado.subtotalTicketActual());
        
        assertFalse(resultado.idTicket().isEmpty());
        assertFalse(resultado.sku().isEmpty());
        assertFalse(resultado.nombreProducto().isEmpty());
    }

    @Test
    void eliminarProductoTicket_EliminacionConCantidadDecimal_FuncionaCorrectamente() {
        // Given
        itemExistente.setCantidad(new BigDecimal("2.5"));
        itemExistente.setPrecioUnitario(new BigDecimal("10.00"));
        itemExistente.calcularSubtotal(); // 2.5 * 10.00 = 25.00
        
        // Crear producto que permite decimales (granel o unidad de peso)
        ProductoDto productoGranel = new ProductoDto(
            "PROD001",
            null,
            null,
            "Producto Test",
            null,
            BigDecimal.valueOf(10.00),
            false,
            true,  // esGranel = true
            null,
            null,
            com.isam.model.UnidadMedida.KILOGRAMO,
            List.of(),
            "ACTIVO"
        );
        
        EliminarProductoTicketRequestDto dto = new EliminarProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            "PROD001",
            Optional.of(new BigDecimal("0.5")) // Eliminar 0.5 de 2.5
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenReturn(productoGranel);
        when(ticketRepository.save(any(Ticket.class)))
            .thenReturn(ticketTemporal);

        // When
        EliminarProductoTicketResponseDto resultado = ventasService.eliminarProductoTicket(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("0.5"), resultado.cantidadEliminada());
        assertEquals(new BigDecimal("2.0"), resultado.cantidadRestante());
        assertFalse(resultado.itemEliminadoCompletamente());
        assertEquals(new BigDecimal("20.00"), resultado.subtotalTicketActual()); // 2.0 * 10.00
    }
}