package com.isam.service;

import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.grpc.client.CatalogoGrpcClient;
import com.isam.model.Inventario;
import com.isam.model.UnidadMedida;
import com.isam.repository.InventarioRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private CatalogoGrpcClient catalogoGrpcClient;

    @InjectMocks
    private InventarioService inventarioService;

    private CrearInventarioRequestDto dtoValido;

    @BeforeEach
    void setUp() {
        // Crear DTO de prueba válido
        dtoValido = new CrearInventarioRequestDto(
            "TEST-001",
            "1234567890123",
            null,
            UnidadMedida.UNIDAD
        );
    }

    @Test
    void crearInventario_ProductoExisteEnCatalogo_CreaInventarioExitosamente() {
        // Given
        when(catalogoGrpcClient.existeProducto("TEST-001")).thenReturn(true);
        when(inventarioRepository.findBySku("TEST-001")).thenReturn(Optional.empty());
        
        Inventario inventarioGuardado = new Inventario("TEST-001", "1234567890123", null, UnidadMedida.UNIDAD);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioGuardado);

        // When
        InventarioDto resultado = inventarioService.crearInventario(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("TEST-001", resultado.sku());
        assertEquals("1234567890123", resultado.ean());
        assertEquals(UnidadMedida.UNIDAD.name(), resultado.unidadMedida());
        
        // Verificar que se llamó al cliente de catálogo
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-001");
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ProductoNoExisteEnCatalogo_LanzaExcepcionNotFound() {
        // Given
        when(catalogoGrpcClient.existeProducto("TEST-001")).thenReturn(false);

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, 
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("No se puede crear inventario para el SKU 'TEST-001' porque no existe en el catálogo"));
        
        // Verificar que se llamó al cliente de catálogo
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_InventarioYaExisteConMismaUnidadMedida_RetornaInventarioExistente() {
        // Given
        when(catalogoGrpcClient.existeProducto("TEST-001")).thenReturn(true);
        
        Inventario inventarioExistente = new Inventario("TEST-001", "1234567890123", null, UnidadMedida.UNIDAD);
        when(inventarioRepository.findBySku("TEST-001")).thenReturn(Optional.of(inventarioExistente));

        // When
        InventarioDto resultado = inventarioService.crearInventario(dtoValido);

        // Then
        assertNotNull(resultado);
        assertEquals("TEST-001", resultado.sku());
        assertEquals("1234567890123", resultado.ean());
        assertEquals(UnidadMedida.UNIDAD.name(), resultado.unidadMedida());
        
        // Verificar que se llamó al cliente de catálogo pero no se guardó nuevamente
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_InventarioYaExisteConDiferenteUnidadMedida_LanzaExcepcionAlreadyExists() {
        // Given
        when(catalogoGrpcClient.existeProducto("TEST-001")).thenReturn(true);
        
        Inventario inventarioExistente = new Inventario("TEST-001", "1234567890123", null, UnidadMedida.KILOGRAMO);
        when(inventarioRepository.findBySku("TEST-001")).thenReturn(Optional.of(inventarioExistente));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, 
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.ALREADY_EXISTS, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Ya existe un inventario para SKU 'TEST-001' con unidad de medida diferente"));
        
        // Verificar que se llamó al cliente de catálogo pero no se guardó
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-001");
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_ErrorEnComunicacionConCatalogo_PropagaExcepcion() {
        // Given
        when(catalogoGrpcClient.existeProducto(anyString()))
            .thenThrow(new StatusRuntimeException(Status.INTERNAL.withDescription("Error de comunicación")));

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, 
            () -> inventarioService.crearInventario(dtoValido));
        
        assertEquals(Status.Code.INTERNAL, exception.getStatus().getCode());
        assertEquals("Error de comunicación", exception.getStatus().getDescription());
        
        // Verificar que no se guardó
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-001");
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
        when(catalogoGrpcClient.existeProducto("TEST-002")).thenReturn(true);
        when(inventarioRepository.findBySku("TEST-002")).thenReturn(Optional.empty());
        
        Inventario inventarioGuardado = new Inventario("TEST-002", null, "12345", UnidadMedida.UNIDAD);
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioGuardado);

        // When
        InventarioDto resultado = inventarioService.crearInventario(dtoConPlu);

        // Then
        assertNotNull(resultado);
        assertEquals("TEST-002", resultado.sku());
        assertEquals("12345", resultado.plu());
        assertEquals(UnidadMedida.UNIDAD.name(), resultado.unidadMedida());
        
        // Verificar que se llamó al cliente de catálogo
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-002");
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
        verify(catalogoGrpcClient, never()).existeProducto(anyString());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void crearInventario_VerificaOrdenValidaciones_CatalogoSeVerificaPrimero() {
        // Given
        when(catalogoGrpcClient.existeProducto("TEST-001")).thenReturn(false);

        // When & Then
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
            () -> inventarioService.crearInventario(dtoValido));
        
        // Verificar que la excepción es por catálogo no encontrado (primera validación)
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("no existe en el catálogo"));
        
        // Verificar que se llamó al cliente de catálogo pero no al repositorio
        verify(catalogoGrpcClient, times(1)).existeProducto("TEST-001");
        verify(inventarioRepository, never()).findBySku(anyString());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}