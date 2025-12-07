package com.isam.service;

import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.dto.producto.ConsultarProductoDto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.model.Inventario;
import com.isam.model.UnidadMedida;
import com.isam.repository.InventarioRepository;
import com.isam.repository.LoteRepository;
import com.isam.repository.MovimientoInventarioRepository;
import com.isam.repository.ProveedorRepository;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para InventarioService usando mocks.
 * Prueban la lógica de negocio de manera aislada.
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;
    
    @Mock
    private LoteRepository loteRepository;
    
    @Mock
    private MovimientoInventarioRepository movimientoRepository;
    
    @Mock
    private ProveedorRepository proveedorRepository;
    
    @Mock
    private AjusteInventarioService ajusteInventarioService;
    
    @Mock
    private CatalogoGrpcClient catalogoGrpcClient;
    
    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private InventarioService inventarioService;

    private CrearInventarioRequestDto dtoValido;
    private ConsultarProductoDto productoEnCatalogo;

    @BeforeEach
    void setUp() {
        // Crear DTO de prueba válido
        dtoValido = new CrearInventarioRequestDto(
            "TEST-001",
            "1234567890123",
            null,
            UnidadMedida.UNIDAD
        );
        
        // Crear producto de prueba del catálogo
        productoEnCatalogo = new ConsultarProductoDto(
            "TEST-001",
            "1234567890123",
            null,
            UnidadMedida.UNIDAD
        );
    }

    @Test
    void crearInventario_ProductoExisteEnCatalogo_CreaInventarioExitosamente() {
        // Given
        when(catalogoGrpcClient.consultarProducto("TEST-001")).thenReturn(productoEnCatalogo);
        when(inventarioRepository.findBySku("TEST-001")).thenReturn(Optional.empty());
        
        Inventario inventarioGuardado = new Inventario("TEST-001", "1234567890123", null, UnidadMedida.UNIDAD);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioGuardado);
        
        // Mock TransactionTemplate to execute the callback
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // When
        InventarioDto resultado = inventarioService.crearInventario(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("TEST-001", resultado.sku());
        assertEquals("1234567890123", resultado.ean());
        assertEquals(UnidadMedida.UNIDAD.name(), resultado.unidadMedida());
        
        // Verificar que se llamó al cliente de catálogo
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ProductoNoExisteEnCatalogo_LanzaExcepcionNotFound() {
        // Given
        when(catalogoGrpcClient.consultarProducto("TEST-001"))
            .thenThrow(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Producto no encontrado")));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Producto no encontrado"));
        
        // Verificar que se llamó al cliente de catálogo
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_InventarioYaExisteConMismaUnidadMedida_RetornaInventarioExistente() {
        // Given
        when(catalogoGrpcClient.consultarProducto("TEST-001")).thenReturn(productoEnCatalogo);
        
        Inventario inventarioExistente = new Inventario("TEST-001", "1234567890123", null, UnidadMedida.UNIDAD);
        when(inventarioRepository.findBySku("TEST-001")).thenReturn(Optional.of(inventarioExistente));
        
        // Mock TransactionTemplate to execute the callback
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // When
        InventarioDto resultado = inventarioService.crearInventario(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("TEST-001", resultado.sku());
        assertEquals("1234567890123", resultado.ean());
        assertEquals(UnidadMedida.UNIDAD.name(), resultado.unidadMedida());
        
        // Verificar que se llamó al cliente de catálogo pero no se guardó nuevamente
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_InventarioYaExisteConDiferenteUnidadMedida_LanzaExcepcionAlreadyExists() {
        // Given
        when(catalogoGrpcClient.consultarProducto("TEST-001")).thenReturn(productoEnCatalogo);
        
        Inventario inventarioExistente = new Inventario("TEST-001", "1234567890123", null, UnidadMedida.KILOGRAMO);
        when(inventarioRepository.findBySku("TEST-001")).thenReturn(Optional.of(inventarioExistente));
        
        // Mock TransactionTemplate to execute the callback
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.ALREADY_EXISTS, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Ya existe un inventario oficial para SKU 'TEST-001' con unidad de medida diferente"));
        
        // Verificar que se llamó al cliente de catálogo pero no se guardó
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ErrorEnComunicacionConCatalogo_PropagaExcepcion() {
        // Given
        when(catalogoGrpcClient.consultarProducto(anyString()))
            .thenThrow(new StatusRuntimeException(Status.INTERNAL.withDescription("Error de comunicación")));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.INTERNAL, exception.getStatus().getCode());
        assertEquals("Error de comunicación", exception.getStatus().getDescription());
        
        // Verificar que no se guardó
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ConPluEnLugarDeEan_ProductoExiste_CreaInventarioExitosamente() {
        // Given
        CrearInventarioRequestDto dtoConPlu = new CrearInventarioRequestDto(
            "TEST-002",
            null,
            "12345",
            UnidadMedida.UNIDAD
        );
        
        ConsultarProductoDto productoConPlu = new ConsultarProductoDto(
            "TEST-002",
            null,
            "12345",
            UnidadMedida.UNIDAD
        );
        
        when(catalogoGrpcClient.consultarProducto("TEST-002")).thenReturn(productoConPlu);
        when(inventarioRepository.findBySku("TEST-002")).thenReturn(Optional.empty());
        
        Inventario inventarioGuardado = new Inventario("TEST-002", null, "12345", UnidadMedida.UNIDAD);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioGuardado);
        
        // Mock TransactionTemplate to execute the callback
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            org.springframework.transaction.support.TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        // When
        InventarioDto resultado = inventarioService.crearInventario(dtoConPlu);

        // Then
        assertNotNull(resultado);
        assertEquals("TEST-002", resultado.sku());
        assertEquals("12345", resultado.plu());
        assertEquals(UnidadMedida.UNIDAD.name(), resultado.unidadMedida());
        
        // Verificar que se llamó al cliente de catálogo
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-002");
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ConEanYPlu_LanzaExcepcionInvalidArgument() {
        // Given
        CrearInventarioRequestDto dtoConAmbos = new CrearInventarioRequestDto(
            "TEST-003",
            "1234567890123",
            "12345",
            UnidadMedida.UNIDAD
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoConAmbos));
        
        assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Un producto solo puede tener o EAN o PLU, pero no ambos"));
        
        // Verificar que no se llamó al cliente de catálogo porque la validación de EAN/PLU es primero
        verify(catalogoGrpcClient, never()).consultarProducto(anyString());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_EanNoCoincidenConCatalogo_LanzaExcepcionInvalidArgument() {
        // Given
        ConsultarProductoDto productoCatalogoDiferente = new ConsultarProductoDto(
            "TEST-001",
            "9999999999999", // EAN diferente
            null,
            UnidadMedida.UNIDAD
        );
        
        when(catalogoGrpcClient.consultarProducto("TEST-001")).thenReturn(productoCatalogoDiferente);

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("El ean del producto no coincide con el del catalogo"));
        
        // Verificar que se llamó al catálogo pero no se guardó
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_UnidadMedidaNoCoincidenConCatalogo_LanzaExcepcionInvalidArgument() {
        // Given
        ConsultarProductoDto productoCatalogoDiferente = new ConsultarProductoDto(
            "TEST-001",
            "1234567890123",
            null,
            UnidadMedida.KILOGRAMO // Unidad diferente
        );
        
        when(catalogoGrpcClient.consultarProducto("TEST-001")).thenReturn(productoCatalogoDiferente);

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("La unidad de medida del producto no coincide con la del catalogo"));
        
        // Verificar que se llamó al catálogo pero no se guardó
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_VerificaOrdenValidaciones_CatalogoSeVerificaPrimero() {
        // Given
        when(catalogoGrpcClient.consultarProducto("TEST-001"))
            .thenThrow(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Producto no existe en el catálogo")));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        // Verificar que la excepción es por catálogo no encontrado (primera validación)
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("no existe en el catálogo"));
        
        // Verificar que se llamó al cliente de catálogo pero no al repositorio
        verify(catalogoGrpcClient, times(1)).consultarProducto("TEST-001");
        verify(inventarioRepository, never()).findBySku(anyString());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}