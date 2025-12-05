package com.isam.mapper;

import com.isam.dto.autenticacion.IniciarSesionRequestDto;
import com.isam.dto.autenticacion.IniciarSesionResponseDto;
import com.isam.dto.autenticacion.CerrarSesionRequestDto;
import com.isam.dto.autenticacion.CerrarSesionResponseDto;
import com.isam.dto.autenticacion.VerificarTokenRequestDto;
import com.isam.dto.autenticacion.VerificarTokenResponseDto;
import com.isam.grpc.usuarios.IniciarSesionRequest;
import com.isam.grpc.usuarios.CerrarSesionRequest;
import com.isam.grpc.usuarios.VerificarTokenRequest;

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
import com.isam.dto.permiso.PermisoDto;
import com.isam.dto.permiso.ListarPermisosRequestDto;
import com.isam.dto.permiso.ListarPermisosResponseDto;
import com.isam.grpc.usuarios.AsignarPermisosRequest;
import com.isam.grpc.usuarios.CrearRolRequest;
import com.isam.grpc.usuarios.CrearUsuarioRequest;
import com.isam.grpc.usuarios.ListarRolesRequest;
import com.isam.grpc.usuarios.ListarPermisosRequest;
import com.isam.grpc.usuarios.RolProto;
import com.isam.grpc.usuarios.UsuarioProto;
import com.isam.grpc.usuarios.PermisoProto;
import com.isam.model.Rol;
import com.isam.model.Usuario;
import com.isam.model.Permiso;
import com.isam.model.enums.EstadoUsuario;
import com.isam.model.enums.AccionPermiso;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UsuariosMapper {
    // ========================================
    // SECCIÓN: Autenticación y Sesión
    // ========================================

    /**
     * Convierte gRPC IniciarSesionRequest a DTO.
     */
    public IniciarSesionRequestDto toDto(IniciarSesionRequest request) {
        return new IniciarSesionRequestDto(
            request.getNombreUsuario(),
            request.getPassword()
        );
    }

    /**
     * Convierte IniciarSesionResponseDto a proto IniciarSesionRequest.Response.
     */
    public IniciarSesionRequest.Response toProto(IniciarSesionResponseDto dto) {
        return IniciarSesionRequest.Response.newBuilder()
            .setTokenJwt(dto.tokenJwt())
            .setUsuario(toProto(dto.usuario()))
            .build();
    }

    /**
     * Convierte gRPC CerrarSesionRequest a DTO.
     */
    public CerrarSesionRequestDto toDto(CerrarSesionRequest request) {
        return new CerrarSesionRequestDto();
    }

    /**
     * Convierte CerrarSesionResponseDto a proto CerrarSesionRequest.Response.
     */
    public CerrarSesionRequest.Response toProto(CerrarSesionResponseDto dto) {
        return CerrarSesionRequest.Response.newBuilder().build();
    }

    /**
     * Convierte gRPC VerificarTokenRequest a DTO.
     */
    public VerificarTokenRequestDto toDto(VerificarTokenRequest request) {
        return new VerificarTokenRequestDto(
            request.getTokenJwt()
        );
    }

    /**
     * Convierte VerificarTokenResponseDto a proto VerificarTokenRequest.Response.
     */
    public VerificarTokenRequest.Response toProto(VerificarTokenResponseDto dto) {
        VerificarTokenRequest.Response.Builder builder = VerificarTokenRequest.Response.newBuilder()
            .setEsValido(dto.esValido());
        
        if (dto.esValido()) {
            if (dto.idUsuario() != null && dto.idUsuario().isPresent()) {
                builder.setIdUsuario(dto.idUsuario().get());
            }
            
            if (dto.nombreUsuario() != null && dto.nombreUsuario().isPresent()) {
                builder.setNombreUsuario(dto.nombreUsuario().get());
            }
            
            if (dto.roles() != null && !dto.roles().isEmpty()) {
                List<RolProto> rolesProto = dto.roles().stream()
                    .map(this::toProto)
                    .collect(Collectors.toList());
                builder.addAllRoles(rolesProto);
            }
        }
        
        return builder.build();
    }

    /**
     * Convierte gRPC CrearRolRequest a DTO.
     */
    public CrearRolRequestDto toDto(CrearRolRequest request) {
        return new CrearRolRequestDto(
            request.getNombreRol(),
            request.hasDescripcion() ? request.getDescripcion() : ""
        );
    }

    /**
     * Convierte entidad Rol a RolDto.
     */
    public RolDto toDto(Rol rol) {
        List<PermisoDto> permisosDto = new ArrayList<>();
        if (rol.getPermisos() != null) {
            permisosDto = rol.getPermisos().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        }

        return new RolDto(
            rol.getIdRol(),
            rol.getNombreRol(),
            rol.getDescripcionRol(),
            permisosDto
        );
    }

    /**
     * Convierte RolDto a proto RolProto.
     */
    public RolProto toProto(RolDto rolDto) {
        if (rolDto == null) {
            return null;
        }

        RolProto.Builder builder = RolProto.newBuilder()
            .setIdRol(rolDto.idRol())
            .setNombreRol(rolDto.nombreRol());
        
        if (rolDto.descripcion() != null) {
            builder.setDescripcion(rolDto.descripcion());
        }

        if (rolDto.permisos() != null && !rolDto.permisos().isEmpty()) {
            List<PermisoProto> permisosProto = rolDto.permisos().stream()
                .map(this::toProto)
                .collect(Collectors.toList());
            builder.addAllPermisos(permisosProto);
        }
        
        return builder.build();
    }

    /**
     * Convierte CrearRolResponseDto a proto CrearRolRequestResponse.
     */
    public CrearRolRequest.Response toProto(CrearRolResponseDto response) {
        return CrearRolRequest.Response.newBuilder()
            .setRol(toProto(response.rol()))
            .build();
    }

    // ========================================
    // SECCIÓN: Conversiones de Usuario
    // ========================================

    /**
     * Convierte gRPC CrearUsuarioRequest a DTO.
     */
    public CrearUsuarioRequestDto toDto(CrearUsuarioRequest request) {
        return new CrearUsuarioRequestDto(
            request.getNombreUsuario(),
            request.getPassword(),
            request.getNombreCompleto(),
            request.getIdRolesList()
        );
    }

    /**
     * Convierte entidad Usuario a UsuarioDto.
     */
    public UsuarioDto toDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        // Convertir roles
        List<RolDto> rolesDto = new ArrayList<>();
        if (usuario.getRoles() != null && !usuario.getRoles().isEmpty()) {
            rolesDto = usuario.getRoles().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        }

        return new UsuarioDto(
            usuario.getIdUsuario(),
            usuario.getNombreUsuario(),
            usuario.getNombreCompleto(),
            usuario.getEstado(),
            usuario.getFechaCreacion().format(formatter),
            usuario.getFechaUltimoAcceso() != null
                ? Optional.of(usuario.getFechaUltimoAcceso().format(formatter))
                : Optional.empty(),
            usuario.getRequiereCambioContrasena(),
            rolesDto
        );
    }

    /**
     * Convierte UsuarioDto a proto UsuarioProto.
     */
    public UsuarioProto toProto(UsuarioDto dto) {
        if (dto == null) {
            return null;
        }

        UsuarioProto.Builder builder = UsuarioProto.newBuilder();
        
        builder.setIdUsuario(dto.idUsuario());
        builder.setNombreUsuario(dto.nombreUsuario());
        builder.setNombreCompleto(dto.nombreCompleto() != null ? dto.nombreCompleto() : "");
        builder.setEstado(mapEstadoModelToProto(dto.estado()));
        builder.setFechaCreacion(dto.fechaCreacion());
        
        if (dto.fechaUltimoAcceso() != null && dto.fechaUltimoAcceso().isPresent()) {
            builder.setFechaUltimoAcceso(dto.fechaUltimoAcceso().get());
        }
        
        if (dto.requiereCambioPassword() != null) {
            builder.setRequiereCambioPassword(dto.requiereCambioPassword());
        }
        
        if (dto.roles() != null && !dto.roles().isEmpty()) {
            List<RolProto> rolesProto = dto.roles().stream()
                .map(this::toProto)
                .collect(Collectors.toList());
            builder.addAllRoles(rolesProto);
        }
        
        return builder.build();
    }

    /**
     * Convierte CrearUsuarioResponseDto a proto CrearUsuarioRequest.Response.
     */
    public CrearUsuarioRequest.Response toProto(CrearUsuarioResponseDto response) {
        return CrearUsuarioRequest.Response.newBuilder()
            .setUsuario(toProto(response.usuario()))
            .build();
    }

    // ========================================
    // SECCIÓN: Helpers de Conversión de Enums
    // ========================================

    /**
     * Convierte enum EstadoUsuario de Java a proto.
     */
    private com.isam.grpc.usuarios.EstadoUsuario mapEstadoModelToProto(EstadoUsuario estado) {
        if (estado == null) {
            return com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO;
        }
        
        return switch (estado) {
            case ACTIVO -> com.isam.grpc.usuarios.EstadoUsuario.ACTIVO;
            case INACTIVO -> com.isam.grpc.usuarios.EstadoUsuario.INACTIVO;
        };
    }

    /**
     * Convierte enum EstadoUsuario de proto a Java.
     */
    private EstadoUsuario mapEstadoProtoToModel(com.isam.grpc.usuarios.EstadoUsuario estado) {
        if (estado == null || estado == com.isam.grpc.usuarios.EstadoUsuario.ESTADO_USUARIO_NO_ESPECIFICADO) {
            return EstadoUsuario.ACTIVO;
        }
        
        return switch (estado) {
            case ACTIVO -> EstadoUsuario.ACTIVO;
            case INACTIVO -> EstadoUsuario.INACTIVO;
            default -> EstadoUsuario.ACTIVO;
        };
    }

    // ========================================
    // SECCIÓN: Conversiones de Permisos
    // ========================================

    /**
     * Convierte entidad Permiso a PermisoDto.
     */
    public PermisoDto toDto(Permiso permiso) {
        if (permiso == null) {
            return null;
        }

        return new PermisoDto(
            permiso.getIdPermiso(),
            permiso.getNombrePermiso(),
            permiso.getDescripcion(),
            permiso.getRecurso(),
            permiso.getAccion()
        );
    }

    /**
     * Convierte PermisoDto a proto PermisoProto.
     */
    public PermisoProto toProto(PermisoDto dto) {
        if (dto == null) {
            return null;
        }

        PermisoProto.Builder builder = PermisoProto.newBuilder()
            .setIdPermiso(dto.idPermiso())
            .setNombrePermiso(dto.nombrePermiso())
            .setDescripcion(dto.descripcion())
            .setRecurso(dto.recurso())
            .setAccion(mapAccionModelToProto(dto.accion()));

        return builder.build();
    }

    /**
     * Convierte proto PermisoProto a PermisoDto.
     */
    public PermisoDto toDtoFromProto(PermisoProto proto) {
        if (proto == null) {
            return null;
        }

        return new PermisoDto(
            proto.getIdPermiso(),
            proto.getNombrePermiso(),
            proto.getDescripcion(),
            proto.getRecurso(),
            mapAccionProtoToModel(proto.getAccion())
        );
    }

    // ========================================
    // SECCIÓN: Conversiones de Request/Response
    // ========================================

    /**
     * Convierte gRPC AsignarPermisosRequest a DTO.
     */
    public AsignarPermisosRequestDto toDto(AsignarPermisosRequest request) {
        return new AsignarPermisosRequestDto(
            request.getIdRol(),
            new ArrayList<>(request.getIdPermisosList())
        );
    }

    /**
     * Convierte AsignarPermisosResponseDto a proto AsignarPermisosRequest.Response.
     */
    public AsignarPermisosRequest.Response toProto(AsignarPermisosResponseDto response) {
        return AsignarPermisosRequest.Response.newBuilder().build();
    }

    /**
     * Convierte gRPC ListarRolesRequest a DTO.
     */
    public ListarRolesRequestDto toDto(ListarRolesRequest request) {
        return new ListarRolesRequestDto();
    }

    /**
     * Convierte ListarRolesResponseDto a proto ListarRolesRequest.Response.
     */
    public ListarRolesRequest.Response toProto(ListarRolesResponseDto dto) {
        if (dto == null) {
            return null;
        }

        ListarRolesRequest.Response.Builder builder = ListarRolesRequest.Response.newBuilder();
        
        if (dto.roles() != null && !dto.roles().isEmpty()) {
            List<RolProto> rolesProto = dto.roles().stream()
                .map(this::toProto)
                .collect(Collectors.toList());
            builder.addAllRoles(rolesProto);
        }
        
        return builder.build();
    }

    /**
     * Convierte gRPC ListarPermisosRequest a DTO.
     */
    public ListarPermisosRequestDto toDto(ListarPermisosRequest request) {
        return new ListarPermisosRequestDto();
    }

    /**
     * Convierte ListarPermisosResponseDto a proto ListarPermisosRequest.Response.
     */
    public ListarPermisosRequest.Response toProto(ListarPermisosResponseDto dto) {
        if (dto == null) {
            return null;
        }

        ListarPermisosRequest.Response.Builder builder = ListarPermisosRequest.Response.newBuilder();
        
        if (dto.permisos() != null && !dto.permisos().isEmpty()) {
            List<PermisoProto> permisosProto = dto.permisos().stream()
                .map(this::toProto)
                .collect(Collectors.toList());
            builder.addAllPermisos(permisosProto);
        }
        
        return builder.build();
    }

    // ========================================
    // SECCIÓN: Helpers de Conversión de Enums
    // ========================================

    /**
     * Convierte enum AccionPermiso de Java a proto.
     */
    private com.isam.grpc.usuarios.AccionPermiso mapAccionModelToProto(AccionPermiso accion) {
        if (accion == null) {
            return com.isam.grpc.usuarios.AccionPermiso.ACCION_PERMISO_NO_ESPECIFICADO;
        }
        
        return switch (accion) {
            case CREAR -> com.isam.grpc.usuarios.AccionPermiso.CREAR;
            case LEER -> com.isam.grpc.usuarios.AccionPermiso.LEER;
            case ACTUALIZAR -> com.isam.grpc.usuarios.AccionPermiso.ACTUALIZAR;
            case ELIMINAR -> com.isam.grpc.usuarios.AccionPermiso.ELIMINAR;
            case EJECUTAR -> com.isam.grpc.usuarios.AccionPermiso.EJECUTAR;
        };
    }

    /**
     * Convierte enum AccionPermiso de proto a Java.
     */
    private AccionPermiso mapAccionProtoToModel(com.isam.grpc.usuarios.AccionPermiso accion) {
        if (accion == null || accion == com.isam.grpc.usuarios.AccionPermiso.ACCION_PERMISO_NO_ESPECIFICADO) {
            return AccionPermiso.CREAR;
        }
        
        return switch (accion) {
            case CREAR -> AccionPermiso.CREAR;
            case LEER -> AccionPermiso.LEER;
            case ACTUALIZAR -> AccionPermiso.ACTUALIZAR;
            case ELIMINAR -> AccionPermiso.ELIMINAR;
            case EJECUTAR -> AccionPermiso.EJECUTAR;
            default -> AccionPermiso.CREAR;
        };
    }

    /**
     * Convierte gRPC ConsultarUsuariosRequest a DTO.
     */
    public com.isam.dto.usuario.ConsultarUsuariosRequestDto toDto(com.isam.grpc.usuarios.ConsultarUsuariosRequest request) {
        return new com.isam.dto.usuario.ConsultarUsuariosRequestDto(
            request.hasIdUsuario() ? Optional.of(request.getIdUsuario()) : Optional.empty(),
            request.hasNombreUsuario() ? Optional.of(request.getNombreUsuario()) : Optional.empty(),
            request.hasIdRol() ? Optional.of(request.getIdRol()) : Optional.empty()
        );
    }

    /**
     * Convierte ConsultarUsuariosResponseDto a proto ConsultarUsuariosRequest.Response.
     */
    public com.isam.grpc.usuarios.ConsultarUsuariosRequest.Response toProto(com.isam.dto.usuario.ConsultarUsuariosResponseDto dto) {
        if (dto == null) {
            return null;
        }

        com.isam.grpc.usuarios.ConsultarUsuariosRequest.Response.Builder builder = 
            com.isam.grpc.usuarios.ConsultarUsuariosRequest.Response.newBuilder();
        
        if (dto.usuarios() != null && !dto.usuarios().isEmpty()) {
            List<UsuarioProto> usuariosProto = dto.usuarios().stream()
                .map(this::toProto)
                .collect(Collectors.toList());
            builder.addAllUsuarios(usuariosProto);
        }
        
        return builder.build();
    }

    // ========================================
    // SECCIÓN: Conversiones de Proto a DTO (Request)
    // ========================================

    /**
     * Convierte gRPC UsuarioProto a UsuarioDto.
     */
    public UsuarioDto toDto(UsuarioProto proto) {
        if (proto == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        // Convertir roles
        List<RolDto> rolesDto = new ArrayList<>();
        if (proto.getRolesCount() > 0) {
            rolesDto = proto.getRolesList().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        }

        return new UsuarioDto(
            proto.getIdUsuario(),
            proto.getNombreUsuario(),
            proto.getNombreCompleto(),
            mapEstadoProtoToModel(proto.getEstado()),
            proto.getFechaCreacion(),
            proto.hasFechaUltimoAcceso() && !proto.getFechaUltimoAcceso().isEmpty()
                ? Optional.of(proto.getFechaUltimoAcceso())
                : Optional.empty(),
            proto.getRequiereCambioPassword(),
            rolesDto
        );
    }

    /**
     * Convierte gRPC RolProto a RolDto.
     */
    public RolDto toDto(RolProto proto) {
        if (proto == null) {
            return null;
        }

        List<PermisoDto> permisos = new ArrayList<>();
        if (proto.getPermisosCount() > 0) {
            permisos = proto.getPermisosList().stream()
                .map(this::toDtoFromProto)
                .collect(Collectors.toList());
        }

        return new RolDto(
            proto.getIdRol(),
            proto.getNombreRol(),
            proto.getDescripcion(),
            permisos
        );
    }

    /**
     * Convierte gRPC PermisoProto a PermisoDto.
     */
    public PermisoDto toDto(PermisoProto proto) {
        if (proto == null) {
            return null;
        }

        return new PermisoDto(
            proto.getIdPermiso(),
            proto.getNombrePermiso(),
            proto.getDescripcion(),
            proto.getRecurso(),
            mapAccionProtoToModel(proto.getAccion())
        );
    }
}
