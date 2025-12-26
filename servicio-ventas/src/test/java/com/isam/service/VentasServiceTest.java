package com.isam.service;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.AnadirProductoTicketResponseDto;
import com.isam.dto.CerrarTicketRequestDto;
import com.isam.dto.CerrarTicketResponseDto;
import com.isam.dto.ConsultarTicketRequestDto;
import com.isam.dto.ConsultarTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.dto.ProcesarPagoRequestDto;
import com.isam.service.ports.IProveedorCatalogo;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
    private IProveedorCatalogo proveedorCatalogo;

    @Mock
    private com.isam.service.ports.IVentaEventPublisher ventaEventPublisher;
    
    @Mock
    private com.isam.repository.ProductoCacheRepository productoCacheRepository;

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
    void anadirProductoTicket_ProductoNuevo_AnadeProductoExitosamente() {
        // Given
        String codigoBarras = "1234567890123";
        String sku = "SKU-001";
        AnadirProductoTicketRequestDto dto = new AnadirProductoTicketRequestDto(
            ticketTemporal.getIdTicket(),
            codigoBarras
        );
        
        com.isam.dto.CatalogoClient.ProductoDto producto = new com.isam.dto.CatalogoClient.ProductoDto(
            sku,
            "1234567890123",
            null,
            "Producto Test",
            "Descripción del producto",
            new BigDecimal("10.50"),
            false,
            false,
            null,
            "FIFO",
            com.isam.model.UnidadMedida.KILOGRAMO,
            null,
            "ACTIVO"
        );
        
        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));
        
        when(productoCacheRepository.findByEan(codigoBarras)).thenReturn(Optional.empty());
        when(productoCacheRepository.findByPlu(codigoBarras)).thenReturn(Optional.empty());
        when(productoCacheRepository.findById(codigoBarras)).thenReturn(Optional.empty());
        
        when(proveedorCatalogo.traducirCodigoBarrasASku(codigoBarras))
            .thenReturn(sku);
        when(proveedorCatalogo.consultarProducto(sku))
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
        
        verify(proveedorCatalogo, times(1)).traducirCodigoBarrasASku(codigoBarras);
        verify(proveedorCatalogo, times(1)).consultarProducto(sku);
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
        
        when(productoCacheRepository.findByEan(codigoBarras)).thenReturn(Optional.empty());
        when(productoCacheRepository.findByPlu(codigoBarras)).thenReturn(Optional.empty());
        when(productoCacheRepository.findById(codigoBarras)).thenReturn(Optional.empty());

        when(proveedorCatalogo.traducirCodigoBarrasASku(codigoBarras))
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


    @Test
    void anadirProductoTicket_TicketPagado_LanzaExcepcion() {
        // Given
        ticketTemporal.setEstadoTicket(EstadoTicket.PAGADO); // <--- El ticket ya se pagó
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
        // Verificamos tu mensaje nuevo
        assertTrue(exception.getMessage().contains("El ticket ya está PAGADO"));
    }

    @Test
    void procesarPago_TicketYaPagado_LanzaExcepcion() {
        // Given
        ticketTemporal.setEstadoTicket(EstadoTicket.PAGADO); // <--- Ya está pagado
        ProcesarPagoRequestDto dto = new ProcesarPagoRequestDto(
            ticketTemporal.getIdTicket(),
            com.isam.model.MetodoPago.EFECTIVO,
            new BigDecimal("50.00")
        );

        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.procesarPago(dto)
        );

        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("El ticket ya está PAGADO"));
    }

    @Test
    void cerrarTicket_TicketPagado_CierraCorrectamente() {
        // --- GIVEN (Preparación) ---
        String idTicket = "ticket-uuid-completo";
        
        // 1. Configurar el Ticket
        Ticket ticketListo = new Ticket();
        ticketListo.setIdTicket(idTicket);
        ticketListo.setIdUsuario("cajero-test");
        ticketListo.setFechaHora(java.time.LocalDateTime.now());
        // CRÍTICO: El estado debe ser PAGADO
        ticketListo.setEstadoTicket(EstadoTicket.PAGADO); 

        // 2. Configurar el Pago (Obligatorio para cerrar)
        com.isam.model.Pago pagoMock = new com.isam.model.Pago();
        pagoMock.setIdPago("pago-123");
        pagoMock.setMetodoPago(com.isam.model.MetodoPago.EFECTIVO);
        pagoMock.setMontoRecibido(new BigDecimal("50.00"));
        pagoMock.setMontoCambio(new BigDecimal("10.00"));
        ticketListo.setPago(pagoMock);

        // 3. Configurar Items (Obligatorio tener al menos uno y con importes para el cálculo)
        java.util.List<com.isam.model.ItemTicket> items = new java.util.ArrayList<>();
        
        com.isam.model.ItemTicket item1 = new com.isam.model.ItemTicket();
        item1.setSku("PROD-001");
        item1.setNombreProducto("Producto A");
        item1.setCantidad(new BigDecimal("2.000"));
        item1.setPrecioUnitario(new BigDecimal("10.00"));
        item1.setSubtotal(new BigDecimal("20.00")); // 2 * 10
        item1.setTicket(ticketListo);
        items.add(item1);

        com.isam.model.ItemTicket item2 = new com.isam.model.ItemTicket();
        item2.setSku("PROD-002");
        item2.setNombreProducto("Producto B");
        item2.setCantidad(new BigDecimal("1.000"));
        item2.setPrecioUnitario(new BigDecimal("20.00"));
        item2.setSubtotal(new BigDecimal("20.00")); // 1 * 20
        item2.setTicket(ticketListo);
        items.add(item2);

        ticketListo.setItems(items);
        // Nota: El servicio recalcula el subtotal, pero lo inicializamos aquí por coherencia
        ticketListo.setSubtotal(new BigDecimal("40.00")); 

        // 4. Configurar Mocks del Repositorio
        // Cuando busquen el ticket, devolvemos el que hemos preparado
        when(ticketRepository.findById(idTicket)).thenReturn(Optional.of(ticketListo));
        
        // Simulamos la secuencia de la base de datos (ej. devuelve 100)
        when(ticketRepository.getNextTicketNumber()).thenReturn(100L);
        
        // Cuando guarden, devolvemos el mismo objeto modificado
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // DTO de entrada
        CerrarTicketRequestDto dto = new CerrarTicketRequestDto(idTicket);

        // --- WHEN (Ejecución) ---
        CerrarTicketResponseDto resultado = ventasService.cerrarTicket(dto);

        // --- THEN (Verificación) ---
        assertNotNull(resultado);
        
        // 1. Verificar número de ticket generado (Formato: T-YYYY-XXXXXXX)
        String anioActual = String.valueOf(java.time.LocalDate.now().getYear());
        String numeroEsperado = "T-" + anioActual + "-0000100";
        assertEquals(numeroEsperado, resultado.numeroTicket());
        assertEquals(numeroEsperado, ticketListo.getNumeroTicket());

        // 2. Verificar cambio de estado
        assertEquals(EstadoTicket.CERRADO, ticketListo.getEstadoTicket());

        // 3. Verificar totales calculados
        // Total esperado: 20 + 20 = 40
        assertEquals(0, new BigDecimal("40.00").compareTo(resultado.total()));
        
        // 4. Verificar datos del pago en la respuesta
        assertEquals(com.isam.model.MetodoPago.EFECTIVO, resultado.metodoPago());
        assertEquals(0, new BigDecimal("50.00").compareTo(resultado.montoRecibido()));

        // 5. Verificar interacciones con dependencias
        verify(ticketRepository).save(ticketListo);
        
        verify(ventaEventPublisher).publicarVenta(any(Ticket.class), anyList(), anyString());
    }

    @Test
    void cerrarTicket_TicketTemporal_LanzaExcepcion() {
        // Given
        ticketTemporal.setEstadoTicket(EstadoTicket.TEMPORAL);
        
        CerrarTicketRequestDto dto = new CerrarTicketRequestDto(ticketTemporal.getIdTicket());

        when(ticketRepository.findById(ticketTemporal.getIdTicket()))
            .thenReturn(Optional.of(ticketTemporal));

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> ventasService.cerrarTicket(dto)
        );

        assertEquals(Status.FAILED_PRECONDITION.getCode(), exception.getStatus().getCode());
        // Verificamos tu mensaje específico
        assertTrue(exception.getMessage().contains("Debe procesar el pago antes de cerrarlo"));
    }


}