package com.isam.mapper;

import com.isam.dto.autenticacion.*;
import com.isam.dto.rol.*;
import com.isam.dto.usuario.*;
import com.isam.dto.permiso.*;
import com.isam.grpc.usuarios.*;
import com.isam.model.enums.EstadoUsuario;
import com.isam.model.enums.AccionPermiso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UsuariosMapperAutoTest {

    private UsuariosMapperAuto mapper = UsuariosMapperAuto.INSTANCE;

    private UsuarioDto usuarioDto;
    private RolDto rolDto;
    private PermisoDto permisoDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        rolDto = new RolDto("ROL-001", "Administrador", "Rol de administrador");
        permisoDto = new PermisoDto("PERM-001", "Gestionar Usuarios", "Permiso para gestionar usuarios", "usuarios", AccionPermiso.CREAR);

        usuarioDto = new UsuarioDto(
            "USR-001",
            "jdoe",
            "John Doe",
            EstadoUsuario.ACTIVO,
            "2023-01-15",
            Optional.of("2023-12-01"),
            false,
            List.of(rolDto)
        );
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
        assertEquals("2023-01-15", resultado.getFechaCreacion());
        assertEquals("2023-12-01", resultado.getFechaUltimoAcceso());
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
        assertEquals("Permiso para gestionar usuarios", resultado.getDescripcion());
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

    // ========================================
    // SECCIÓN: Tests de Conversiones de Autenticación
    // ========================================

    @Test
    void toDto_IniciarSesionRequest_ConvierteCorrectamente() {
        // Given
        IniciarSesionRequest request = IniciarSesionRequest.newBuilder()
            .setNombreUsuario("jdoe")
            .setPassword("password123")
            .build();

        // When
        IniciarSesionRequestDto resultado = mapper.toDto(request);

        // Then
        assertNotNull(resultado);
        assertEquals("jdoe", resultado.nombreUsuario());
        assertEquals("password123", resultado.password());
    }

    @Test
    void toProto_IniciarSesionResponseDto_ConvierteCorrectamente() {
        // Given
        UsuarioDto usuarioDto = new UsuarioDto(
            "USR-001",
            "jdoe",
            "John Doe",
            EstadoUsuario.ACTIVO,
            "2023-01-15",
            Optional.empty(),
            false,
            List.of()
        );

        IniciarSesionResponseDto responseDto = new IniciarSesionResponseDto(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            usuarioDto
        );

        // When
        IniciarSesionRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c", resultado.getTokenJwt());
        assertNotNull(resultado.getUsuario());
        assertEquals("USR-001", resultado.getUsuario().getIdUsuario());
    }

    // ========================================
    // SECCIÓN: Tests de Conversiones de Roles y Permisos
    // ========================================

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
        RolDto rolDto = new RolDto("ROL-001", "Nuevo Rol", "Descripción del nuevo rol");
        CrearRolResponseDto responseDto = new CrearRolResponseDto(rolDto);

        // When
        CrearRolRequest.Response resultado = mapper.toProto(responseDto);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.getRol());
        assertEquals("ROL-001", resultado.getRol().getIdRol());
        assertEquals("Nuevo Rol", resultado.getRol().getNombreRol());
    }

    // ========================================
    // SECCIÓN: Tests de Conversiones de Enums
    // ========================================

    @Test
    void mapEstadoUsuarioToProto_EnumActivo_ConvierteCorrectamente() {
        // When
        com.isam.grpc.usuarios.EstadoUsuario resultado = mapper.mapEstadoUsuarioModelToProto(EstadoUsuario.ACTIVO);

        // Then
        assertEquals(com.isam.grpc.usuarios.EstadoUsuario.ACTIVO, resultado);
    }

    @Test
    void mapEstadoUsuarioToProto_EnumInactivo_ConvierteCorrectamente() {
        // When
        com.isam.grpc.usuarios.EstadoUsuario resultado = mapper.mapEstadoUsuarioModelToProto(EstadoUsuario.INACTIVO);

        // Then
        assertEquals(com.isam.grpc.usuarios.EstadoUsuario.INACTIVO, resultado);
    }

    @Test
    void mapEstadoUsuarioToProto_EnumNulo_UsaValorPorDefecto() {
        // When
        com.isam.grpc.usuarios.EstadoUsuario resultado = mapper.mapEstadoUsuarioModelToProto(null);

        // Then
        assertEquals(com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO, resultado);
    }

    @Test
    void mapAccionPermisoToProto_EnumCrear_ConvierteCorrectamente() {
        // When
        com.isam.grpc.usuarios.AccionPermiso resultado = mapper.mapAccionPermisoModelToProto(AccionPermiso.CREAR);

        // Then
        assertEquals(com.isam.grpc.usuarios.AccionPermiso.CREAR, resultado);
    }

    @Test
    void mapAccionPermisoToProto_EnumLeer_ConvierteCorrectamente() {
        // When
        com.isam.grpc.usuarios.AccionPermiso resultado = mapper.mapAccionPermisoModelToProto(AccionPermiso.LEER);

        // Then
        assertEquals(com.isam.grpc.usuarios.AccionPermiso.LEER, resultado);
    }

    // ========================================
    // SECCIÓN: Tests de Manejo de Nulos y Campos Opcionales
    // ========================================

    @Test
    void mapOptionalString_ValorNoNulo_ConvierteCorrectamente() {
        // When
        Optional<String> resultado = mapper.mapOptionalString("testValue");

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("testValue", resultado.get());
    }

    @Test
    void mapOptionalString_ValorNulo_ConvierteCorrectamente() {
        // When
        Optional<String> resultado = mapper.mapOptionalString(null);

        // Then
        assertFalse(resultado.isPresent());
    }

    @Test
    void mapOptionalToString_OpcionalConValor_ConvierteCorrectamente() {
        // When
        String resultado = mapper.mapOptionalToString(Optional.of("testValue"));

        // Then
        assertEquals("testValue", resultado);
    }

    @Test
    void mapOptionalToString_OpcionalVacio_ConvierteCorrectamente() {
        // When
        String resultado = mapper.mapOptionalToString(Optional.empty());

        // Then
        assertEquals("", resultado);
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
}