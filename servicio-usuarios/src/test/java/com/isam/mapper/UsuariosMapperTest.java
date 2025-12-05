package com.isam.mapper;

import com.isam.dto.permiso.ListarPermisosRequestDto;
import com.isam.dto.permiso.ListarPermisosResponseDto;
import com.isam.dto.permiso.PermisoDto;
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
import com.isam.grpc.usuarios.AsignarPermisosRequest;
import com.isam.grpc.usuarios.CrearRolRequest;
import com.isam.grpc.usuarios.CrearUsuarioRequest;
import com.isam.grpc.usuarios.ListarPermisosRequest;
import com.isam.grpc.usuarios.ListarRolesRequest;
import com.isam.grpc.usuarios.PermisoProto;
import com.isam.grpc.usuarios.RolProto;
import com.isam.grpc.usuarios.UsuarioProto;
import com.isam.model.Rol;
import com.isam.model.Usuario;
import com.isam.model.enums.EstadoUsuario;
import com.isam.model.enums.AccionPermiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para UsuariosMapper manual.
 * Prueba todas las conversiones entre DTOs y Proto Messages.
 */
class UsuariosMapperTest {

    private UsuariosMapper mapper;

    private UsuarioDto usuarioDto;
    private RolDto rolDto;
    private PermisoDto permisoDto;

    @BeforeEach
    void setUp() {
        mapper = new UsuariosMapper();

        rolDto = new RolDto("ROL-001", "Administrador", "Rol de administrador", new ArrayList<>());
        
        usuarioDto = new UsuarioDto(
            "USR-001",
            "jdoe",
            "John Doe",
            EstadoUsuario.ACTIVO,
            "2023-01-01T10:00:00",
            Optional.of("2023-01-02T15:30:00"),
            false,
            List.of(rolDto)
        );

        permisoDto = new PermisoDto("PERM-001", "Gestionar Usuarios", "Permite gestionar usuarios", "usuarios", AccionPermiso.CREAR);
    }

    // ========================================
    // SECCIÓN: Tests de Conversiones de DTO a Proto
    // ========================================

    @Test
    void toProto_UsuarioDtoConDatosCompletos_ConvierteCorrectamente() {
        // When
        UsuarioProto resultado = mapper.toProto(usuarioDto);

        // Then
        assertNotNull(resultado);
        assertEquals("USR-001", resultado.getIdUsuario());
        assertEquals("jdoe", resultado.getNombreUsuario());
        assertEquals("John Doe", resultado.getNombreCompleto());
        assertEquals(com.isam.grpc.usuarios.EstadoUsuario.ACTIVO, resultado.getEstado());
        assertEquals("2023-01-01T10:00:00", resultado.getFechaCreacion());
        assertEquals("2023-01-02T15:30:00", resultado.getFechaUltimoAcceso());
        assertFalse(resultado.getRequiereCambioPassword());
        assertEquals(1, resultado.getRolesCount());
    }

    @Test
    void toProto_UsuarioDtoConCamposNulos_ConvierteConValoresPorDefecto() {
        // Given
        UsuarioDto dtoConNulos = new UsuarioDto(
            "USR-002",
            "jsmith",
            null,
            null,
            "2023-01-15",
            Optional.empty(),
            true,
            List.of()
        );

        // When
        UsuarioProto resultado = mapper.toProto(dtoConNulos);

        // Then
        assertNotNull(resultado);
        assertEquals("USR-002", resultado.getIdUsuario());
        assertEquals("jsmith", resultado.getNombreUsuario());
        assertEquals("", resultado.getNombreCompleto()); // Campo opcional vacío
        assertEquals(com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO, resultado.getEstado());
        assertTrue(resultado.getRequiereCambioPassword());
        assertEquals(0, resultado.getRolesCount());
    }

    @Test
    void toProto_RolDto_ConvierteCorrectamente() {
        // When
        RolProto resultado = mapper.toProto(rolDto);

        // Then
        assertNotNull(resultado);
        assertEquals("ROL-001", resultado.getIdRol());
        assertEquals("Administrador", resultado.getNombreRol());
        assertEquals("Rol de administrador", resultado.getDescripcion());
    }

    @Test
    void toProto_PermisoDto_ConvierteCorrectamente() {
        // When
        PermisoProto resultado = mapper.toProto(permisoDto);

        // Then
        assertNotNull(resultado);
        assertEquals("PERM-001", resultado.getIdPermiso());
        assertEquals("Gestionar Usuarios", resultado.getNombrePermiso());
        assertEquals("Permite gestionar usuarios", resultado.getDescripcion());
        assertEquals("usuarios", resultado.getRecurso());
        assertEquals(com.isam.grpc.usuarios.AccionPermiso.CREAR, resultado.getAccion());
    }

    // ========================================
    // SECCIÓN: Tests de Conversiones de Proto a DTO
    // ========================================

    @Test
    void toDto_UsuarioProtoConDatosCompletos_ConvierteCorrectamente() {
        // Given
        UsuarioProto proto = UsuarioProto.newBuilder()
            .setIdUsuario("USR-001")
            .setNombreUsuario("jdoe")
            .setNombreCompleto("John Doe")
            .setEstado(com.isam.grpc.usuarios.EstadoUsuario.ACTIVO)
            .setFechaCreacion("2023-01-15")
            .setFechaUltimoAcceso("2023-12-01")
            .setRequiereCambioPassword(false)
            .addRoles(RolProto.newBuilder()
                .setIdRol("ROL-001")
                .setNombreRol("Administrador")
                .setDescripcion("Rol de administrador"))
            .build();

        // When
        UsuarioDto resultado = mapper.toDto(proto);

        // Then
        assertNotNull(resultado);
        assertEquals("USR-001", resultado.idUsuario());
        assertEquals("jdoe", resultado.nombreUsuario());
        assertEquals("John Doe", resultado.nombreCompleto());
        assertEquals(EstadoUsuario.ACTIVO, resultado.estado());
        assertEquals("2023-01-15", resultado.fechaCreacion());
        assertTrue(resultado.fechaUltimoAcceso().isPresent());
        assertEquals("2023-12-01", resultado.fechaUltimoAcceso().get());
        assertFalse(resultado.requiereCambioPassword());
        assertEquals(1, resultado.roles().size());
    }

    @Test
    void toDto_UsuarioProtoConEstadoNoEspecificado_UsaValorPorDefecto() {
        // Given
        UsuarioProto proto = UsuarioProto.newBuilder()
            .setIdUsuario("USR-002")
            .setNombreUsuario("jsmith")
            .setEstado(com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO)
            .setFechaCreacion("2023-01-15")
            .build();

        // When
        UsuarioDto resultado = mapper.toDto(proto);

        // Then
        assertNotNull(resultado);
        assertEquals(EstadoUsuario.ACTIVO, resultado.estado()); // Valor por defecto
    }

    @Test
    void toDto_RolProto_ConvierteCorrectamente() {
        // Given
        RolProto proto = RolProto.newBuilder()
            .setIdRol("ROL-001")
            .setNombreRol("Administrador")
            .setDescripcion("Rol de administrador")
            .build();

        // When
        RolDto resultado = mapper.toDto(proto);

        // Then
        assertNotNull(resultado);
        assertEquals("ROL-001", resultado.idRol());
        assertEquals("Administrador", resultado.nombreRol());
        assertEquals("Rol de administrador", resultado.descripcion());
    }

    @Test
    void toDto_PermisoProto_ConvierteCorrectamente() {
        // Given
        PermisoProto proto = PermisoProto.newBuilder()
            .setIdPermiso("PERM-001")
            .setNombrePermiso("Gestionar Usuarios")
            .setDescripcion("Permiso para gestionar usuarios")
            .setRecurso("usuarios")
            .setAccion(com.isam.grpc.usuarios.AccionPermiso.CREAR)
            .build();

        // When
        PermisoDto resultado = mapper.toDto(proto);

        // Then
        assertNotNull(resultado);
        assertEquals("PERM-001", resultado.idPermiso());
        assertEquals("Gestionar Usuarios", resultado.nombrePermiso());
        assertEquals("Permiso para gestionar usuarios", resultado.descripcion());
        assertEquals("usuarios", resultado.recurso());
        assertEquals(AccionPermiso.CREAR, resultado.accion());
    }

    // ========================================
    // SECCIÓN: Tests de Conversiones de Request/Response
    // ========================================

    @Test
    void toDto_CrearUsuarioRequest_ConvierteCorrectamente() {
        // Given
        CrearUsuarioRequest request = CrearUsuarioRequest.newBuilder()
            .setNombreUsuario("newuser")
            .setPassword("securePassword123")
            .setNombreCompleto("New User")
            .addIdRoles("ROL-001")
            .addIdRoles("ROL-002")
            .build();

        // When
        CrearUsuarioRequestDto resultado = mapper.toDto(request);

        // Then
        assertNotNull(resultado);
        assertEquals("newuser", resultado.nombreUsuario());
        assertEquals("securePassword123", resultado.password());
        assertEquals("New User", resultado.nombreCompleto());
        assertEquals(2, resultado.idRoles().size());
        assertTrue(resultado.idRoles().containsAll(Arrays.asList("ROL-001", "ROL-002")));
    }

    @Test
    void toProto_CrearUsuarioResponseDto_ConvierteCorrectamente() {
        // Given
        UsuarioDto usuarioDto = new UsuarioDto(
            "USR-001",
            "newuser",
            "New User",
            EstadoUsuario.ACTIVO,
            "2023-01-15",
            Optional.empty(),
            true,
            List.of()
        );

        CrearUsuarioResponseDto responseDto = new CrearUsuarioResponseDto(usuarioDto);

        // When
        CrearUsuarioRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.getUsuario());
        assertEquals("USR-001", resultado.getUsuario().getIdUsuario());
        assertEquals("newuser", resultado.getUsuario().getNombreUsuario());
    }

    @Test
    void toDto_CrearRolRequest_ConvierteCorrectamente() {
        // Given
        CrearRolRequest request = CrearRolRequest.newBuilder()
            .setNombreRol("Nuevo Rol")
            .setDescripcion("Descripción del nuevo rol")
            .build();

        // When
        CrearRolRequestDto resultado = mapper.toDto(request);

        // Then
        assertNotNull(resultado);
        assertEquals("Nuevo Rol", resultado.nombreRol());
        assertEquals("Descripción del nuevo rol", resultado.descripcion());
    }

    @Test
    void toProto_CrearRolResponseDto_ConvierteCorrectamente() {
        // Given
        RolDto rolDto = new RolDto("ROL-001", "Nuevo Rol", "Descripción del nuevo rol", new ArrayList<>());
        CrearRolResponseDto responseDto = new CrearRolResponseDto(rolDto);

        // When
        CrearRolRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.getRol());
        assertEquals("ROL-001", resultado.getRol().getIdRol());
        assertEquals("Nuevo Rol", resultado.getRol().getNombreRol());
    }

    @Test
    void toDto_AsignarPermisosRequest_ConvierteCorrectamente() {
        // Given
        AsignarPermisosRequest request = AsignarPermisosRequest.newBuilder()
            .setIdRol("ROL-001")
            .addIdPermisos("PERM-001")
            .addIdPermisos("PERM-002")
            .build();

        // When
        AsignarPermisosRequestDto resultado = mapper.toDto(request);

        // Then
        assertNotNull(resultado);
        assertEquals("ROL-001", resultado.idRol());
        assertEquals(2, resultado.idPermisos().size());
        assertTrue(resultado.idPermisos().containsAll(Arrays.asList("PERM-001", "PERM-002")));
    }

    @Test
    void toProto_AsignarPermisosResponseDto_ConvierteCorrectamente() {
        // Given
        AsignarPermisosResponseDto responseDto = new AsignarPermisosResponseDto();

        // When
        AsignarPermisosRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
    }

    @Test
    void toDto_ListarRolesRequest_ConvierteCorrectamente() {
        // Given
        ListarRolesRequest request = ListarRolesRequest.newBuilder().build();

        // When
        ListarRolesRequestDto resultado = mapper.toDto(request);

        // Then
        assertNotNull(resultado);
    }

    @Test
    void toProto_ListarRolesResponseDto_ConvierteCorrectamente() {
        // Given
        ListarRolesResponseDto responseDto = new ListarRolesResponseDto(List.of(rolDto));

        // When
        ListarRolesRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getRolesCount());
        assertEquals("ROL-001", resultado.getRoles(0).getIdRol());
        assertEquals("Administrador", resultado.getRoles(0).getNombreRol());
    }

    @Test
    void toDto_ListarPermisosRequest_ConvierteCorrectamente() {
        // Given
        ListarPermisosRequest request = ListarPermisosRequest.newBuilder().build();

        // When
        ListarPermisosRequestDto resultado = mapper.toDto(request);

        // Then
        assertNotNull(resultado);
    }

    @Test
    void toProto_ListarPermisosResponseDto_ConvierteCorrectamente() {
        // Given
        ListarPermisosResponseDto responseDto = new ListarPermisosResponseDto(List.of(permisoDto));

        // When
        ListarPermisosRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getPermisosCount());
        assertEquals("PERM-001", resultado.getPermisos(0).getIdPermiso());
        assertEquals("Gestionar Usuarios", resultado.getPermisos(0).getNombrePermiso());
    }

    // ========================================
    // SECCIÓN: Tests de Casos Límite y Errores
    // ========================================

    @Test
    void toProto_UsuarioDtoNulo_RetornaNulo() {
        // When
        UsuarioProto resultado = mapper.toProto((UsuarioDto) null);

        // Then
        assertNull(resultado);
    }

    @Test
    void toDto_UsuarioProtoNulo_RetornaNulo() {
        // When
        UsuarioDto resultado = mapper.toDto((UsuarioProto) null);

        // Then
        assertNull(resultado);
    }

    @Test
    void toProto_RolDtoNulo_RetornaNulo() {
        // When
        RolProto resultado = mapper.toProto((RolDto) null);

        // Then
        assertNull(resultado);
    }

    @Test
    void toDto_RolProtoNulo_RetornaNulo() {
        // When
        RolDto resultado = mapper.toDto((RolProto) null);

        // Then
        assertNull(resultado);
    }

    @Test
    void toProto_PermisoDtoNulo_RetornaNulo() {
        // When
        PermisoProto resultado = mapper.toProto((PermisoDto) null);

        // Then
        assertNull(resultado);
    }

    @Test
    void toDto_PermisoProtoNulo_RetornaNulo() {
        // When
        PermisoDto resultado = mapper.toDto((PermisoProto) null);

        // Then
        assertNull(resultado);
    }
}