package com.isam.service;

import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.CrearRolResponseDto;
import com.isam.dto.rol.RolDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.model.Rol;
import com.isam.repository.RolRepository;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para UsuariosService.
 * Prueba la funcionalidad de gestión de usuarios y roles.
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = {UsuariosService.class, UsuariosMapper.class}
))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuariosServiceTest {

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private RolRepository rolRepository;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        rolRepository.deleteAll();
    }

    @Test
    void crearRol_DatosValidos_CreaRolExitosamente() {
        // Given
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "Administrador",
            "Rol con permisos completos de administración"
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.rol());
        
        RolDto rolDto = resultado.rol();
        assertNotNull(rolDto.idRol());
        assertEquals("Administrador", rolDto.nombreRol());
        assertEquals("Rol con permisos completos de administración", rolDto.descripcion());
        
        // Verificar que se guardó en la base de datos
        assertTrue(rolRepository.existsByNombreRol("Administrador"));
    }

    @Test
    void crearRol_SinDescripcion_CreaRolExitosamente() {
        // Given
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "Cajero",
            null
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.rol());
        
        RolDto rolDto = resultado.rol();
        assertNotNull(rolDto.idRol());
        assertEquals("Cajero", rolDto.nombreRol());
        assertNull(rolDto.descripcion());
        
        // Verificar que se guardó en la base de datos
        assertTrue(rolRepository.existsByNombreRol("Cajero"));
    }

    @Test
    void crearRol_NombreDuplicado_LanzaExcepcion() {
        // Given
        Rol rolExistente = new Rol();
        rolExistente.setNombreRol("Supervisor");
        rolExistente.setDescripcionRol("Rol de supervisión");
        rolRepository.save(rolExistente);
        
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "Supervisor",
            "Intento de crear rol duplicado"
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.crearRol(dto)
        );
        
        assertTrue(exception.getMessage().contains("Ya existe un rol con el nombre 'Supervisor'"));
        assertEquals(io.grpc.Status.Code.ALREADY_EXISTS, exception.getStatus().getCode());
    }

    @Test
    void crearRol_NombreMinimo_CreaRolExitosamente() {
        // Given
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "ABC",
            "Rol con nombre mínimo de 3 caracteres"
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.rol());
        assertEquals("ABC", resultado.rol().nombreRol());
        assertTrue(rolRepository.existsByNombreRol("ABC"));
    }

    @Test
    void crearRol_NombreLargo_CreaRolExitosamente() {
        // Given
        String nombreLargo = "A".repeat(100); // 100 caracteres (máximo permitido)
        CrearRolRequestDto dto = new CrearRolRequestDto(
            nombreLargo,
            "Rol con nombre de 100 caracteres"
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.rol());
        assertEquals(nombreLargo, resultado.rol().nombreRol());
        assertTrue(rolRepository.existsByNombreRol(nombreLargo));
    }

    @Test
    void crearRol_DescripcionLarga_CreaRolExitosamente() {
        // Given
        String descripcionLarga = "D".repeat(500); // 500 caracteres (máximo permitido)
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "RolDescripcionLarga",
            descripcionLarga
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.rol());
        assertEquals("RolDescripcionLarga", resultado.rol().nombreRol());
        assertEquals(descripcionLarga, resultado.rol().descripcion());
    }

    @Test
    void crearRol_MultiplesRoles_CadaUnoConIdUnico() {
        // Given
        CrearRolRequestDto dto1 = new CrearRolRequestDto("Gerente", "Rol de gerencia");
        CrearRolRequestDto dto2 = new CrearRolRequestDto("Vendedor", "Rol de ventas");
        CrearRolRequestDto dto3 = new CrearRolRequestDto("Almacenero", "Rol de almacén");

        // When
        CrearRolResponseDto resultado1 = usuariosService.crearRol(dto1);
        CrearRolResponseDto resultado2 = usuariosService.crearRol(dto2);
        CrearRolResponseDto resultado3 = usuariosService.crearRol(dto3);

        // Then
        assertNotNull(resultado1.rol().idRol());
        assertNotNull(resultado2.rol().idRol());
        assertNotNull(resultado3.rol().idRol());
        
        // Verificar que los IDs son únicos
        assertNotEquals(resultado1.rol().idRol(), resultado2.rol().idRol());
        assertNotEquals(resultado1.rol().idRol(), resultado3.rol().idRol());
        assertNotEquals(resultado2.rol().idRol(), resultado3.rol().idRol());
        
        // Verificar que todos se guardaron
        assertEquals(3, rolRepository.count());
    }

    @Test
    void crearRol_VerificarPersistencia_RolSeGuardaCorrectamente() {
        // Given
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "Auditor",
            "Rol de auditoría y control"
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        String idRolCreado = resultado.rol().idRol();
        
        // Verificar que el rol existe en la base de datos
        Rol rolGuardado = rolRepository.findById(idRolCreado).orElse(null);
        assertNotNull(rolGuardado);
        assertEquals("Auditor", rolGuardado.getNombreRol());
        assertEquals("Rol de auditoría y control", rolGuardado.getDescripcionRol());
    }

    @Test
    void crearRol_NombresConEspacios_CreaRolExitosamente() {
        // Given
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "Jefe de Almacén",
            "Rol con espacios en el nombre"
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertEquals("Jefe de Almacén", resultado.rol().nombreRol());
        assertTrue(rolRepository.existsByNombreRol("Jefe de Almacén"));
    }

    @Test
    void crearRol_NombresConCaracteresEspeciales_CreaRolExitosamente() {
        // Given
        CrearRolRequestDto dto = new CrearRolRequestDto(
            "Admin-TI",
            "Rol con guión en el nombre"
        );

        // When
        CrearRolResponseDto resultado = usuariosService.crearRol(dto);

        // Then
        assertNotNull(resultado);
        assertEquals("Admin-TI", resultado.rol().nombreRol());
        assertTrue(rolRepository.existsByNombreRol("Admin-TI"));
    }
}