package com.isam.service;

import com.isam.dto.rol.AsignarPermisosRequestDto;
import com.isam.dto.rol.AsignarPermisosResponseDto;
import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.CrearRolResponseDto;
import com.isam.dto.rol.RolDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.mapper.UsuariosMapperAuto;
import com.isam.model.Permiso;
import com.isam.model.Rol;
import com.isam.model.enums.AccionPermiso;
import com.isam.repository.PermisoRepository;
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

import java.util.List;

/**
 * Tests de integración para UsuariosService.
 * Prueba la funcionalidad de gestión de usuarios y roles.
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = {UsuariosService.class, UsuariosMapper.class, UsuariosMapperAuto.class}
))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuariosServiceTest {

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PermisoRepository permisoRepository;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        rolRepository.deleteAll();
        permisoRepository.deleteAll();
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

    @Test
    void asignarPermisosARol_RolExistenteYPermisosValidos_AsignaCorrectamente() {
        // Given
        // Crear rol
        Rol rol = new Rol();
        rol.setNombreRol("Gerente");
        rol.setDescripcionRol("Rol de gerencia");
        rol.setPermisos(new java.util.ArrayList<>());
        rol = rolRepository.save(rol);
        
        // Crear permisos con nombres únicos para evitar conflictos
        Permiso permiso1 = new Permiso("CREAR_USUARIOS_TEST", "Crear usuarios test", "usuarios", AccionPermiso.CREAR);
        Permiso permiso2 = new Permiso("LEER_USUARIOS_TEST", "Leer usuarios test", "usuarios", AccionPermiso.LEER);
        permiso1 = permisoRepository.save(permiso1);
        permiso2 = permisoRepository.save(permiso2);
        
        AsignarPermisosRequestDto dto = new AsignarPermisosRequestDto(
            rol.getIdRol(),
            List.of(permiso1.getIdPermiso(), permiso2.getIdPermiso())
        );

        // When
        AsignarPermisosResponseDto resultado = usuariosService.asignarPermisosARol(dto);

        // Then
        assertNotNull(resultado);
        
        // Verificar que los permisos se asignaron correctamente
        Rol rolActualizado = rolRepository.findById(rol.getIdRol()).orElse(null);
        assertNotNull(rolActualizado);
        assertEquals(2, rolActualizado.getPermisos().size());
        
        // Verificar que los permisos asignados son los correctos
        List<String> idsPermisosAsignados = rolActualizado.getPermisos().stream()
            .map(Permiso::getIdPermiso)
            .toList();
        assertTrue(idsPermisosAsignados.contains(permiso1.getIdPermiso()));
        assertTrue(idsPermisosAsignados.contains(permiso2.getIdPermiso()));
    }

    @Test
    void asignarPermisosARol_RolNoExistente_LanzaExcepcion() {
        // Given
        String idRolInexistente = "rol-inexistente";
        Permiso permiso = new Permiso("CREAR_USUARIOS_TEST2", "Crear usuarios test2", "usuarios", AccionPermiso.CREAR);
        permiso = permisoRepository.save(permiso);
        
        AsignarPermisosRequestDto dto = new AsignarPermisosRequestDto(
            idRolInexistente,
            List.of(permiso.getIdPermiso())
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.asignarPermisosARol(dto)
        );
        
        assertTrue(exception.getMessage().contains("Rol no encontrado con ID: '" + idRolInexistente + "'"));
        assertEquals(io.grpc.Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    void asignarPermisosARol_PermisoNoExistente_LanzaExcepcion() {
        // Given
        // Crear rol
        Rol rol = new Rol();
        rol.setNombreRol("Supervisor");
        rol.setDescripcionRol("Rol de supervisión");
        rol.setPermisos(new java.util.ArrayList<>());
        rol = rolRepository.save(rol);
        
        String idPermisoInexistente = "permiso-inexistente";
        
        AsignarPermisosRequestDto dto = new AsignarPermisosRequestDto(
            rol.getIdRol(),
            List.of(idPermisoInexistente)
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.asignarPermisosARol(dto)
        );
        
        assertTrue(exception.getMessage().contains("Permisos no encontrados: " + idPermisoInexistente));
        assertEquals(io.grpc.Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    void asignarPermisosARol_ReemplazaPermisosExistentes_ReemplazaCorrectamente() {
        // Given
        // Crear rol
        Rol rol = new Rol();
        rol.setNombreRol("Administrador");
        rol.setDescripcionRol("Rol de administración");
        rol.setPermisos(new java.util.ArrayList<>());
        rol = rolRepository.save(rol);
        
        // Crear permisos iniciales con nombres únicos
        Permiso permiso1 = new Permiso("CREAR_USUARIOS_TEST3", "Crear usuarios test3", "usuarios", AccionPermiso.CREAR);
        Permiso permiso2 = new Permiso("LEER_USUARIOS_TEST3", "Leer usuarios test3", "usuarios", AccionPermiso.LEER);
        permiso1 = permisoRepository.save(permiso1);
        permiso2 = permisoRepository.save(permiso2);
        
        // Asignar permisos iniciales
        rol.getPermisos().add(permiso1);
        rol = rolRepository.save(rol);
        
        // Crear nuevos permisos para reemplazar con nombres únicos
        Permiso permiso3 = new Permiso("ACTUALIZAR_USUARIOS_TEST3", "Actualizar usuarios test3", "usuarios", AccionPermiso.ACTUALIZAR);
        Permiso permiso4 = new Permiso("ELIMINAR_USUARIOS_TEST3", "Eliminar usuarios test3", "usuarios", AccionPermiso.ELIMINAR);
        permiso3 = permisoRepository.save(permiso3);
        permiso4 = permisoRepository.save(permiso4);
        
        AsignarPermisosRequestDto dto = new AsignarPermisosRequestDto(
            rol.getIdRol(),
            List.of(permiso3.getIdPermiso(), permiso4.getIdPermiso())
        );

        // When
        AsignarPermisosResponseDto resultado = usuariosService.asignarPermisosARol(dto);

        // Then
        assertNotNull(resultado);
        
        // Verificar que los permisos se reemplazaron correctamente
        Rol rolActualizado = rolRepository.findById(rol.getIdRol()).orElse(null);
        assertNotNull(rolActualizado);
        assertEquals(2, rolActualizado.getPermisos().size());
        
        // Verificar que los permisos son los nuevos (no los antiguos)
        List<String> idsPermisosAsignados = rolActualizado.getPermisos().stream()
            .map(Permiso::getIdPermiso)
            .toList();
        assertFalse(idsPermisosAsignados.contains(permiso1.getIdPermiso()));
        assertFalse(idsPermisosAsignados.contains(permiso2.getIdPermiso()));
        assertTrue(idsPermisosAsignados.contains(permiso3.getIdPermiso()));
        assertTrue(idsPermisosAsignados.contains(permiso4.getIdPermiso()));
    }

    @Test
    void asignarPermisosARol_ListaVacia_LimpiaPermisos() {
        // Given
        // Crear rol
        Rol rol = new Rol();
        rol.setNombreRol("Temporero");
        rol.setDescripcionRol("Rol temporal");
        rol.setPermisos(new java.util.ArrayList<>());
        rol = rolRepository.save(rol);
        
        // Crear y asignar permisos iniciales con nombre único
        Permiso permiso1 = new Permiso("CREAR_USUARIOS_TEST4", "Crear usuarios test4", "usuarios", AccionPermiso.CREAR);
        permiso1 = permisoRepository.save(permiso1);
        rol.getPermisos().add(permiso1);
        rol = rolRepository.save(rol);
        
        AsignarPermisosRequestDto dto = new AsignarPermisosRequestDto(
            rol.getIdRol(),
            List.of() // Lista vacía
        );

        // When
        AsignarPermisosResponseDto resultado = usuariosService.asignarPermisosARol(dto);

        // Then
        assertNotNull(resultado);
        
        // Verificar que se limpiaron los permisos
        Rol rolActualizado = rolRepository.findById(rol.getIdRol()).orElse(null);
        assertNotNull(rolActualizado);
        assertEquals(0, rolActualizado.getPermisos().size());
    }

    @Test
    void inicializarPermisos_BaseDatosVacia_CreaTodosLosPermisos() {
        // Given
        // La base de datos está vacía (se limpió en setUp)

        // When
        usuariosService.inicializarPermisos();

        // Then
        // Verificar que se crearon todos los permisos esperados
        List<Permiso> todosLosPermisos = permisoRepository.findAll();
        
        // Deberían haber 13 recursos x 5 acciones = 65 permisos
        assertEquals(65, todosLosPermisos.size());
        
        // Verificar permisos específicos
        assertTrue(permisoRepository.existsByNombrePermiso("CREAR_USUARIOS"));
        assertTrue(permisoRepository.existsByNombrePermiso("LEER_ROLES"));
        assertTrue(permisoRepository.existsByNombrePermiso("ACTUALIZAR_PERMISOS"));
        assertTrue(permisoRepository.existsByNombrePermiso("ELIMINAR_CATALOGO"));
        assertTrue(permisoRepository.existsByNombrePermiso("EJECUTAR_VENTAS"));
    }

    @Test
    void inicializarPermisos_PermisosExistentes_NoCreaDuplicados() {
        // Given
        // Crear algunos permisos manualmente con nombre único
        Permiso permisoExistente = new Permiso("CREAR_USUARIOS_TEST5", "Crear usuarios test5", "usuarios", AccionPermiso.CREAR);
        permisoRepository.save(permisoExistente);
        
        long countInicial = permisoRepository.count();
        
        // When
        usuariosService.inicializarPermisos();
        
        // Then
        // Verificar que no se crearon duplicados
        long countFinal = permisoRepository.count();
        assertEquals(66, countFinal); // 65 permisos base + 1 permiso de prueba
        
        // Verificar que el permiso existente sigue siendo el mismo
        List<Permiso> permisosEncontradosList = permisoRepository.findByNombrePermisoContainingIgnoreCase("CREAR_USUARIOS_TEST5");
        assertFalse(permisosEncontradosList.isEmpty());
        Permiso permisoVerificado = permisosEncontradosList.get(0);
        assertEquals(permisoExistente.getIdPermiso(), permisoVerificado.getIdPermiso());
    }

}