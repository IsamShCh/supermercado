package com.isam.mapper;

import com.isam.dto.autenticacion.*;
import com.isam.dto.rol.*;
import com.isam.dto.usuario.*;
import com.isam.dto.permiso.*;
import com.isam.grpc.usuarios.*;
import com.isam.model.enums.EstadoUsuario;
import com.isam.model.Rol;
import com.isam.model.enums.AccionPermiso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Optional;

/**
 * Mapper para conversiones entre DTOs y Proto Messages de Usuarios.
 *
 * Este mapper maneja:
 * - Conversión de enums entre Java y Proto (EstadoUsuario, AccionPermiso)
 * - Campos opcionales (Optional<String> ↔ string)
 * - Listas de entidades relacionadas (roles, permisos, usuarios)
 * - Request/Response de autenticación y gestión de usuarios
 */
@Mapper(componentModel = "spring")
public interface UsuariosMapperAuto {

    /**
     * Instancia estática para uso sin inyección de dependencias.
     * Permite usar el mapper en contextos donde Spring no está disponible.
     */
    UsuariosMapperAuto INSTANCE = Mappers.getMapper(UsuariosMapperAuto.class);

    // ========================================
    // SECCIÓN: Conversiones de DTO a Proto
    // ========================================

    default UsuarioProto toProto(UsuarioDto dto) {
        if (dto == null) {
            return null;
        }
        
        UsuarioProto.Builder builder = UsuarioProto.newBuilder();
        
        builder.setIdUsuario(dto.idUsuario());
        builder.setNombreUsuario(dto.nombreUsuario());
        builder.setNombreCompleto(dto.nombreCompleto() != null ? dto.nombreCompleto() : "");
        builder.setEstado(mapEstadoUsuarioModelToProto(dto.estado()));
        builder.setFechaCreacion(dto.fechaCreacion());
        builder.setFechaUltimoAcceso(dto.fechaUltimoAcceso() != null && dto.fechaUltimoAcceso().isPresent() ? dto.fechaUltimoAcceso().get() : "");
        if (dto.requiereCambioPassword() != null) {
            builder.setRequiereCambioPassword(dto.requiereCambioPassword());
        }
        if (dto.roles() != null && !dto.roles().isEmpty()) {
            builder.addAllRoles(mapRolesListToProto(dto.roles()));
        }
        
        return builder.build();
    }

    RolProto toProto(RolDto dto);

    @Mapping(target = "accion", expression = "java(mapAccionPermisoModelToProto(dto.accion()))")
    PermisoProto toProto(PermisoDto dto);

    // ========================================
    // SECCIÓN: Conversiones de Proto a DTO
    // ========================================

    @Mapping(target = "estado", expression = "java(mapEstadoUsuarioProtoToModel(proto.getEstado()))")
    @Mapping(target = "fechaUltimoAcceso", source = "fechaUltimoAcceso", qualifiedByName = "mapOptionalString")
    @Mapping(target = "roles", expression = "java(mapRolesList(proto.getRolesList()))")
    UsuarioDto toDto(UsuarioProto proto);

    RolDto toDto(RolProto proto);

    @Mapping(target = "accion", expression = "java(mapAccionPermisoProtoToModel(proto.getAccion()))")
    PermisoDto toDto(PermisoProto proto);

    // ========================================
    // SECCIÓN: Conversiones de Entidad a DTO
    // ========================================

    @Mapping(target = "descripcion", source = "descripcionRol" )
    RolDto toDto(Rol rol);

    // ========================================
    // SECCIÓN: Conversiones de Request/Response
    // ========================================

    @Mapping(target = "idRoles", expression = "java(mapStringList(request.getIdRolesList()))")
    CrearUsuarioRequestDto toDto(CrearUsuarioRequest request);

    CrearUsuarioRequest.Response toProto(CrearUsuarioResponseDto dto);

    @Mapping(target = "nombreUsuario", source = "nombreUsuario", qualifiedByName = "mapOptionalString")
    @Mapping(target = "nombreCompleto", source = "nombreCompleto", qualifiedByName = "mapOptionalString")
    @Mapping(target = "idRoles", expression = "java(mapStringList(request.getIdRolesList()))")
    ModificarUsuarioRequestDto toDto(ModificarUsuarioRequest request);

    ModificarUsuarioRequest.Response toProto(ModificarUsuarioResponseDto dto);

    DesactivarUsuarioRequestDto toDto(DesactivarUsuarioRequest request);

    @Mapping(target = "idUsuario", source = "idUsuario", qualifiedByName = "mapOptionalString")
    @Mapping(target = "nombreUsuario", source = "nombreUsuario", qualifiedByName = "mapOptionalString")
    @Mapping(target = "idRol", source = "idRol", qualifiedByName = "mapOptionalString")
    ConsultarUsuariosRequestDto toDto(ConsultarUsuariosRequest request);

    default ConsultarUsuariosRequest.Response toProto(ConsultarUsuariosResponseDto dto) {
        if (dto == null) {
            return null;
        }
        
        ConsultarUsuariosRequest.Response.Builder builder = ConsultarUsuariosRequest.Response.newBuilder();
        
        if (dto.usuarios() != null && !dto.usuarios().isEmpty()) {
            builder.addAllUsuarios(mapUsuariosListToProto(dto.usuarios()));
        }
        
        return builder.build();
    }

    // ========================================
    // SECCIÓN: Conversiones de Autenticación
    // ========================================

    IniciarSesionRequestDto toDto(IniciarSesionRequest request);

    IniciarSesionRequest.Response toProto(IniciarSesionResponseDto dto);

    CerrarSesionRequestDto toDto(CerrarSesionRequest request);

    VerificarTokenRequestDto toDto(VerificarTokenRequest request);

    @Mapping(target = "idUsuario", source = "idUsuario", qualifiedByName = "mapOptionalToString")
    @Mapping(target = "nombreUsuario", source = "nombreUsuario", qualifiedByName = "mapOptionalToString")
    VerificarTokenRequest.Response toProto(VerificarTokenResponseDto dto);

    // ========================================
    // SECCIÓN: Conversiones de Gestión de Contraseña
    // ========================================

    CambiarPasswordRequestDto toDto(CambiarPasswordRequest request);

    RestablecerPasswordRequestDto toDto(RestablecerPasswordRequest request);

    RestablecerPasswordRequest.Response toProto(RestablecerPasswordResponseDto dto);

    // ========================================
    // SECCIÓN: Conversiones de Roles y Permisos
    // ========================================

    CrearRolRequestDto toDto(CrearRolRequest request);

    CrearRolRequest.Response toProto(CrearRolResponseDto dto);

    @Mapping(target = "nombreRol", source = "nombreRol", qualifiedByName = "mapOptionalString")
    @Mapping(target = "descripcion", source = "descripcion", qualifiedByName = "mapOptionalString")
    ModificarRolRequestDto toDto(ModificarRolRequest request);

    ModificarRolRequest.Response toProto(ModificarRolResponseDto dto);

    EliminarRolRequestDto toDto(EliminarRolRequest request);

    @Mapping(target = "idPermisos", expression = "java(mapStringList(request.getIdPermisosList()))")
    AsignarPermisosRequestDto toDto(AsignarPermisosRequest request);

    AsignarPermisosRequest.Response toProto(AsignarPermisosResponseDto dto);

    @Mapping(target = "roles", expression = "java(mapRolesList(response.getRolesList()))")
    ListarRolesResponseDto toDto(ListarRolesRequest.Response response);

    @Mapping(target = "permisos", expression = "java(mapPermisosList(response.getPermisosList()))")
    ListarPermisosResponseDto toDto(ListarPermisosRequest.Response response);

    // ========================================
    // SECCIÓN: Conversiones de Enums
    // ========================================

    default com.isam.grpc.usuarios.EstadoUsuario mapEstadoUsuarioModelToProto(EstadoUsuario estado) {
        if (estado == null) {
            return com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO;
        }

        switch (estado) {
            case ACTIVO:
                return com.isam.grpc.usuarios.EstadoUsuario.ACTIVO;
            case INACTIVO:
                return com.isam.grpc.usuarios.EstadoUsuario.INACTIVO;
            default:
                return com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO;
        }
    }

    default EstadoUsuario mapEstadoUsuarioProtoToModel(com.isam.grpc.usuarios.EstadoUsuario estadoProto) {
        if (estadoProto == null || estadoProto == com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO) {
            return EstadoUsuario.ACTIVO; // Valor por defecto
        }

        switch (estadoProto) {
            case ACTIVO:
                return EstadoUsuario.ACTIVO;
            case INACTIVO:
                return EstadoUsuario.INACTIVO;
            default:
                return EstadoUsuario.ACTIVO;
        }
    }

    default com.isam.grpc.usuarios.AccionPermiso mapAccionPermisoModelToProto(AccionPermiso accion) {
        if (accion == null) {
            return com.isam.grpc.usuarios.AccionPermiso.ACCION_PERMISO_NO_ESPECIFICADO;
        }

        switch (accion) {
            case CREAR:
                return com.isam.grpc.usuarios.AccionPermiso.CREAR;
            case LEER:
                return com.isam.grpc.usuarios.AccionPermiso.LEER;
            case ACTUALIZAR:
                return com.isam.grpc.usuarios.AccionPermiso.ACTUALIZAR;
            case ELIMINAR:
                return com.isam.grpc.usuarios.AccionPermiso.ELIMINAR;
            case EJECUTAR:
                return com.isam.grpc.usuarios.AccionPermiso.EJECUTAR;
            default:
                return com.isam.grpc.usuarios.AccionPermiso.ACCION_PERMISO_NO_ESPECIFICADO;
        }
    }

    default AccionPermiso mapAccionPermisoProtoToModel(com.isam.grpc.usuarios.AccionPermiso accionProto) {
        if (accionProto == null || accionProto == com.isam.grpc.usuarios.AccionPermiso.ACCION_PERMISO_NO_ESPECIFICADO) {
            return AccionPermiso.CREAR; // Valor por defecto
        }

        switch (accionProto) {
            case CREAR:
                return AccionPermiso.CREAR;
            case LEER:
                return AccionPermiso.LEER;
            case ACTUALIZAR:
                return AccionPermiso.ACTUALIZAR;
            case ELIMINAR:
                return AccionPermiso.ELIMINAR;
            case EJECUTAR:
                return AccionPermiso.EJECUTAR;
            default:
                return AccionPermiso.CREAR;
        }
    }

    /**
     * Convierte Optional<String> a String para proto.
     * Los proto builders no aceptan null, por lo que se retorna string vacío.
     *
     * @param optional Optional a convertir
     * @return String para proto, nunca null
     */
    @Named("mapOptionalToString")
    default String mapOptionalToString(Optional<String> optional) {
        return optional != null && optional.isPresent() ? optional.get() : "";
    }
    
    /**
     * Convierte String de proto a Optional<String> para DTO.
     * Maneja campos opcionales del proto de forma segura.
     *
     * @param value String del proto
     * @return Optional<String> para DTO
     */
    @Named("mapOptionalString")
    default Optional<String> mapOptionalString(String value) {
        return value != null ? Optional.of(value) : Optional.empty();
    }

    // ========================================
    // SECCIÓN: Conversiones de Listas
    // ========================================

    /**
     * Convierte lista de RolProto a lista de RolDto.
     * Siempre retorna una lista mutable para evitar UnsupportedOperationException.
     *
     * @param roles Lista de proto roles
     * @return Lista mutable de DTO roles, nunca null
     */
    default List<RolDto> mapRolesList(List<RolProto> roles) {
        if (roles == null || roles.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return roles.stream()
            .map(this::toDto)
            .collect(java.util.stream.Collectors.toList());
    }

    default List<RolProto> mapRolesListToProto(List<RolDto> roles) {
        if (roles == null || roles.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return roles.stream()
            .map(this::toProto)
            .collect(java.util.stream.Collectors.toList());
    }

    default List<PermisoDto> mapPermisosList(List<PermisoProto> permisos) {
        if (permisos == null || permisos.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return permisos.stream()
            .map(this::toDto)
            .collect(java.util.stream.Collectors.toList());
    }

    default List<PermisoProto> mapPermisosListToProto(List<PermisoDto> permisos) {
        if (permisos == null || permisos.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return permisos.stream()
            .map(this::toProto)
            .collect(java.util.stream.Collectors.toList());
    }

    default List<UsuarioDto> mapUsuariosList(List<UsuarioProto> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return usuarios.stream()
            .map(this::toDto)
            .collect(java.util.stream.Collectors.toList());
    }

    default List<UsuarioProto> mapUsuariosListToProto(List<UsuarioDto> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return usuarios.stream()
            .map(this::toProto)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convierte lista de strings a lista mutable.
     * Usado para campos repeated del proto que se mapean a List<String> en DTOs.
     * Siempre retorna una lista mutable para evitar problemas con colecciones inmutables.
     *
     * @param strings Lista original
     * @return Lista mutable, nunca null
     */
    default List<String> mapStringList(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return strings.stream()
            .collect(java.util.stream.Collectors.toList());
    }
}