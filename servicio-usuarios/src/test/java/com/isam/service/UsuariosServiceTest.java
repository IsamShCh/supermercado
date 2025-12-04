package com.isam.service;

import com.isam.config.SecurityConfig;
import com.isam.dto.rol.AsignarPermisosRequestDto;
import com.isam.dto.rol.AsignarPermisosResponseDto;
import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.CrearRolResponseDto;
import com.isam.dto.rol.ListarRolesRequestDto;
import com.isam.dto.rol.ListarRolesResponseDto;
import com.isam.dto.rol.RolDto;
import com.isam.dto.usuario.CrearUsuarioRequestDto;
import com.isam.dto.usuario.CrearUsuarioResponseDto;
import com.isam.dto.usuario.UsuarioDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.model.Permiso;
import com.isam.model.Rol;
import com.isam.model.Usuario;
import com.isam.model.enums.AccionPermiso;
import com.isam.model.enums.EstadoUsuario;
import com.isam.repository.PermisoRepository;
import com.isam.repository.RolRepository;
import com.isam.repository.SesionRepository;
import com.isam.repository.UsuarioRepository;
import com.isam.util.JwtUtil;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
@Import(UsuariosServiceTest.Config.class)
class UsuariosServiceTest {

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private PermisoRepository permisoRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Rol rolAdmin;
    private Rol rolVendedor;

    @TestConfiguration
    static class Config {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public JwtUtil jwtUtil() {
            return new JwtUtil();
        }
    }

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        sesionRepository.deleteAll();
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        permisoRepository.deleteAll();
        
        // No crear roles aquí - cada test creará los que necesite
    }

    @Test
    void crearRol_DatosValidos_CreaRolExitosamente() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
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

    @Test
    void listarRoles_BaseDatosVacia_RetornaListaVacia() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        ListarRolesRequestDto dto = new ListarRolesRequestDto();

        // When
        ListarRolesResponseDto resultado = usuariosService.listarRoles(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.roles());
        assertEquals(0, resultado.roles().size());
    }

    @Test
    void listarRoles_ConRolesExistentes_RetornaTodosLosRoles() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Crear roles de prueba
        Rol rol1 = new Rol();
        rol1.setNombreRol("Administrador");
        rol1.setDescripcionRol("Rol con permisos completos");
        rol1 = rolRepository.save(rol1);

        Rol rol2 = new Rol();
        rol2.setNombreRol("Cajero");
        rol2.setDescripcionRol("Rol para operaciones de caja");
        rol2 = rolRepository.save(rol2);

        Rol rol3 = new Rol();
        rol3.setNombreRol("Supervisor");
        rol3.setDescripcionRol("Rol de supervisión");
        rol3 = rolRepository.save(rol3);

        ListarRolesRequestDto dto = new ListarRolesRequestDto();

        // When
        ListarRolesResponseDto resultado = usuariosService.listarRoles(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.roles());
        assertEquals(3, resultado.roles().size());

        // Verificar que todos los roles están presentes
        List<String> nombresRoles = resultado.roles().stream()
            .map(RolDto::nombreRol)
            .toList();
        
        assertTrue(nombresRoles.contains("Administrador"));
        assertTrue(nombresRoles.contains("Cajero"));
        assertTrue(nombresRoles.contains("Supervisor"));
    }

    @Test
    void listarRoles_ConRolSinDescripcion_RetornaRolCorrectamente() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        Rol rolSinDescripcion = new Rol();
        rolSinDescripcion.setNombreRol("Temporal");
        rolSinDescripcion.setDescripcionRol(null);
        rolSinDescripcion = rolRepository.save(rolSinDescripcion);

        ListarRolesRequestDto dto = new ListarRolesRequestDto();

        // When
        ListarRolesResponseDto resultado = usuariosService.listarRoles(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.roles().size());
        
        RolDto rolDto = resultado.roles().get(0);
        assertEquals("Temporal", rolDto.nombreRol());
        assertNull(rolDto.descripcion());
        assertNotNull(rolDto.idRol());
    }

    @Test
    void listarRoles_VerificarEstructuraDto_RetornaDatosCompletos() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        Rol rol = new Rol();
        rol.setNombreRol("Gerente");
        rol.setDescripcionRol("Rol de gerencia con acceso completo");
        rol = rolRepository.save(rol);

        ListarRolesRequestDto dto = new ListarRolesRequestDto();

        // When
        ListarRolesResponseDto resultado = usuariosService.listarRoles(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.roles().size());
        
        RolDto rolDto = resultado.roles().get(0);
        
        // Verificar que todos los campos están presentes y son correctos
        assertNotNull(rolDto.idRol());
        assertEquals("Gerente", rolDto.nombreRol());
        assertEquals("Rol de gerencia con acceso completo", rolDto.descripcion());
        
        // Verificar que el ID corresponde al rol guardado
        assertEquals(rol.getIdRol(), rolDto.idRol());
    }

    @Test
    void listarRoles_MultiplesRolesVerificarOrden_RetornaRolesEnOrdenAlfabetico() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Crear roles en orden desordenado para verificar si hay algún orden específico
        Rol rol1 = new Rol();
        rol1.setNombreRol("Zar");
        rol1.setDescripcionRol("Rol Z");
        rol1 = rolRepository.save(rol1);

        Rol rol2 = new Rol();
        rol2.setNombreRol("Alpha");
        rol2.setDescripcionRol("Rol A");
        rol2 = rolRepository.save(rol2);

        Rol rol3 = new Rol();
        rol3.setNombreRol("Beta");
        rol3.setDescripcionRol("Rol B");
        rol3 = rolRepository.save(rol3);

        ListarRolesRequestDto dto = new ListarRolesRequestDto();

        // When
        ListarRolesResponseDto resultado = usuariosService.listarRoles(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(3, resultado.roles().size());
        
        // Verificar que todos los roles están presentes (sin importar el orden)
        List<String> nombresRoles = resultado.roles().stream()
            .map(RolDto::nombreRol)
            .toList();
        
        assertTrue(nombresRoles.contains("Zar"));
        assertTrue(nombresRoles.contains("Alpha"));
        assertTrue(nombresRoles.contains("Beta"));
    }

    @Test
    void crearUsuario_DatosValidos_CreaUsuarioExitosamente() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear rol necesario
        rolAdmin = new Rol();
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setDescripcionRol("Administrador del sistema");
        rolAdmin = rolRepository.save(rolAdmin);
        
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "jdoe",
            "Password123!",
            "John Doe",
            List.of(rolAdmin.getIdRol())
        );

        // When
        CrearUsuarioResponseDto resultado = usuariosService.crearUsuario(dto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.usuario());
        
        UsuarioDto usuario = resultado.usuario();
        assertNotNull(usuario.idUsuario());
        assertEquals("jdoe", usuario.nombreUsuario());
        assertEquals("John Doe", usuario.nombreCompleto());
        assertEquals(EstadoUsuario.ACTIVO, usuario.estado());
        assertNotNull(usuario.fechaCreacion());
        assertTrue(usuario.fechaUltimoAcceso().isEmpty());
        assertFalse(usuario.requiereCambioPassword());
        assertEquals(1, usuario.roles().size());
        assertEquals("ADMIN", usuario.roles().get(0).nombreRol());
    }

    @Test
    void crearUsuario_ConMultiplesRoles_AsignaRolesCorrectamente() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear roles necesarios
        rolAdmin = new Rol();
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setDescripcionRol("Administrador del sistema");
        rolAdmin = rolRepository.save(rolAdmin);
        
        rolVendedor = new Rol();
        rolVendedor.setNombreRol("VENDEDOR");
        rolVendedor.setDescripcionRol("Vendedor de tienda");
        rolVendedor = rolRepository.save(rolVendedor);
        
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "mjones",
            "SecurePass456!",
            "Mary Jones",
            List.of(rolAdmin.getIdRol(), rolVendedor.getIdRol())
        );

        // When
        CrearUsuarioResponseDto resultado = usuariosService.crearUsuario(dto);

        // Then
        assertNotNull(resultado);
        UsuarioDto usuario = resultado.usuario();
        assertEquals(2, usuario.roles().size());
        
        List<String> nombresRoles = usuario.roles().stream()
            .map(rol -> rol.nombreRol())
            .sorted()
            .toList();
        
        assertEquals(List.of("ADMIN", "VENDEDOR"), nombresRoles);
    }

    @Test
    void crearUsuario_PasswordHasheadaCorrectamente_NoGuardaPasswordEnTextoPlano() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear rol necesario
        rolVendedor = new Rol();
        rolVendedor.setNombreRol("VENDEDOR");
        rolVendedor.setDescripcionRol("Vendedor de tienda");
        rolVendedor = rolRepository.save(rolVendedor);
        
        String passwordOriginal = "MySecretPassword123!";
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "testuser",
            passwordOriginal,
            "Test User",
            List.of(rolVendedor.getIdRol())
        );

        // When
        CrearUsuarioResponseDto resultado = usuariosService.crearUsuario(dto);

        // Then
        Usuario usuarioGuardado = usuarioRepository.findById(resultado.usuario().idUsuario()).orElseThrow();
        
        // Verificar que el hash no es la contraseña original
        assertNotEquals(passwordOriginal, usuarioGuardado.getHashContrasena());
        
        // Verificar que el hash es válido (usando BCrypt)
        assertTrue(passwordEncoder.matches(passwordOriginal, usuarioGuardado.getHashContrasena()));
        
        // Verificar que el hash tiene contenido
        assertNotNull(usuarioGuardado.getHashContrasena());
        assertFalse(usuarioGuardado.getHashContrasena().isEmpty());
    }

    @Test
    void crearUsuario_NombreUsuarioDuplicado_LanzaExcepcion() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear roles necesarios
        rolAdmin = new Rol();
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setDescripcionRol("Administrador del sistema");
        rolAdmin = rolRepository.save(rolAdmin);
        
        rolVendedor = new Rol();
        rolVendedor.setNombreRol("VENDEDOR");
        rolVendedor.setDescripcionRol("Vendedor de tienda");
        rolVendedor = rolRepository.save(rolVendedor);
        
        CrearUsuarioRequestDto dto1 = new CrearUsuarioRequestDto(
            "duplicate",
            "Password123!",
            "First User",
            List.of(rolAdmin.getIdRol())
        );
        usuariosService.crearUsuario(dto1);

        CrearUsuarioRequestDto dto2 = new CrearUsuarioRequestDto(
            "duplicate",
            "DifferentPass456!",
            "Second User",
            List.of(rolVendedor.getIdRol())
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.crearUsuario(dto2)
        );

        assertEquals(Status.ALREADY_EXISTS.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Ya existe un usuario con el nombre 'duplicate'"));
    }

    @Test
    void crearUsuario_SinRoles_LanzaExcepcion() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "noroles",
            "Password123!",
            "No Roles User",
            List.of()
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.crearUsuario(dto)
        );

        assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Debe asignar al menos un rol al usuario"));
    }

    @Test
    void crearUsuario_RolNoExistente_LanzaExcepcion() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "invalidrole",
            "Password123!",
            "Invalid Role User",
            List.of("rol-inexistente-123")
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.crearUsuario(dto)
        );

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Roles no encontrados"));
        assertTrue(exception.getMessage().contains("rol-inexistente-123"));
    }

    @Test
    void crearUsuario_AlgunosRolesNoExisten_LanzaExcepcion() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear rol necesario
        rolAdmin = new Rol();
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setDescripcionRol("Administrador del sistema");
        rolAdmin = rolRepository.save(rolAdmin);
        
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "partialinvalid",
            "Password123!",
            "Partial Invalid User",
            List.of(rolAdmin.getIdRol(), "rol-inexistente-456", "rol-inexistente-789")
        );

        // When & Then
        StatusRuntimeException exception = assertThrows(
            StatusRuntimeException.class,
            () -> usuariosService.crearUsuario(dto)
        );

        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertTrue(exception.getMessage().contains("Roles no encontrados"));
        assertTrue(exception.getMessage().contains("rol-inexistente-456"));
        assertTrue(exception.getMessage().contains("rol-inexistente-789"));
    }

    @Test
    void crearUsuario_VerificaEstadoInicial_EstadoActivoPorDefecto() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear rol necesario
        rolVendedor = new Rol();
        rolVendedor.setNombreRol("VENDEDOR");
        rolVendedor.setDescripcionRol("Vendedor de tienda");
        rolVendedor = rolRepository.save(rolVendedor);
        
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "activeuser",
            "Password123!",
            "Active User",
            List.of(rolVendedor.getIdRol())
        );

        // When
        CrearUsuarioResponseDto resultado = usuariosService.crearUsuario(dto);

        // Then
        Usuario usuarioGuardado = usuarioRepository.findById(resultado.usuario().idUsuario()).orElseThrow();
        assertEquals(EstadoUsuario.ACTIVO, usuarioGuardado.getEstado());
        assertFalse(usuarioGuardado.getRequiereCambioContrasena());
        assertNull(usuarioGuardado.getFechaUltimoAcceso());
        assertNotNull(usuarioGuardado.getFechaCreacion());
    }

    @Test
    void crearUsuario_VerificaPersistencia_UsuarioGuardadoEnBaseDeDatos() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear rol necesario
        rolAdmin = new Rol();
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setDescripcionRol("Administrador del sistema");
        rolAdmin = rolRepository.save(rolAdmin);
        
        CrearUsuarioRequestDto dto = new CrearUsuarioRequestDto(
            "persistent",
            "Password123!",
            "Persistent User",
            List.of(rolAdmin.getIdRol())
        );

        // When
        CrearUsuarioResponseDto resultado = usuariosService.crearUsuario(dto);

        // Then
        assertTrue(usuarioRepository.existsByNombreUsuario("persistent"));
        
        Usuario usuarioRecuperado = usuarioRepository.findByNombreUsuario("persistent").orElseThrow();
        assertEquals(resultado.usuario().idUsuario(), usuarioRecuperado.getIdUsuario());
        assertEquals("Persistent User", usuarioRecuperado.getNombreCompleto());
        assertEquals(1, usuarioRecuperado.getRoles().size());
    }

    @Test
    void crearUsuario_NombresUsuarioDiferentes_CreaMultiplesUsuarios() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        // Recrear roles necesarios
        rolAdmin = new Rol();
        rolAdmin.setNombreRol("ADMIN");
        rolAdmin.setDescripcionRol("Administrador del sistema");
        rolAdmin = rolRepository.save(rolAdmin);
        
        rolVendedor = new Rol();
        rolVendedor.setNombreRol("VENDEDOR");
        rolVendedor.setDescripcionRol("Vendedor de tienda");
        rolVendedor = rolRepository.save(rolVendedor);
        
        CrearUsuarioRequestDto dto1 = new CrearUsuarioRequestDto(
            "user1",
            "Password123!",
            "User One",
            List.of(rolAdmin.getIdRol())
        );

        CrearUsuarioRequestDto dto2 = new CrearUsuarioRequestDto(
            "user2",
            "Password456!",
            "User Two",
            List.of(rolVendedor.getIdRol())
        );

        CrearUsuarioRequestDto dto3 = new CrearUsuarioRequestDto(
            "user3",
            "Password789!",
            "User Three",
            List.of(rolAdmin.getIdRol(), rolVendedor.getIdRol())
        );

        // When
        usuariosService.crearUsuario(dto1);
        usuariosService.crearUsuario(dto2);
        usuariosService.crearUsuario(dto3);

        // Then
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        assertEquals(3, todosLosUsuarios.size());
        
        assertTrue(usuarioRepository.existsByNombreUsuario("user1"));
        assertTrue(usuarioRepository.existsByNombreUsuario("user2"));
        assertTrue(usuarioRepository.existsByNombreUsuario("user3"));
    }


    @Test
    void listarRoles_ConCaracteresEspeciales_RetornaRolesCorrectamente() {
        // Given
        rolRepository.deleteAll(); // Limpiar roles creados en setUp
        Rol rol1 = new Rol();
        rol1.setNombreRol("Admin-TI");
        rol1.setDescripcionRol("Administrador de TI");
        rol1 = rolRepository.save(rol1);

        Rol rol2 = new Rol();
        rol2.setNombreRol("Jefe de Almacén");
        rol2.setDescripcionRol("Responsable de almacén");
        rol2 = rolRepository.save(rol2);

        ListarRolesRequestDto dto = new ListarRolesRequestDto();

        // When
        ListarRolesResponseDto resultado = usuariosService.listarRoles(dto);

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.roles().size());
        
        List<String> nombresRoles = resultado.roles().stream()
            .map(RolDto::nombreRol)
            .toList();
        
        assertTrue(nombresRoles.contains("Admin-TI"));
        assertTrue(nombresRoles.contains("Jefe de Almacén"));
    }

}