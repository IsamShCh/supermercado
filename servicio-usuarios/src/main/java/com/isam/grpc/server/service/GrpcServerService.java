package com.isam.grpc.server.service;

import org.springframework.stereotype.Service;

import com.isam.dto.autenticacion.IniciarSesionRequestDto;
import com.isam.dto.autenticacion.VerificarTokenRequestDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.isam.grpc.usuarios.*;
import com.isam.dto.permiso.ListarPermisosRequestDto;
import com.isam.dto.permiso.ListarPermisosResponseDto;
import com.isam.dto.rol.AsignarPermisosRequestDto;
import com.isam.dto.rol.AsignarPermisosResponseDto;
import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.dto.rol.ListarRolesRequestDto;
import com.isam.dto.rol.ListarRolesResponseDto;
import com.isam.dto.usuario.CrearUsuarioRequestDto;
import com.isam.dto.usuario.CrearUsuarioResponseDto;
import com.isam.mapper.UsuariosMapper;
import com.isam.service.UsuariosService;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcServerService extends UsuarioServiceGrpc.UsuarioServiceImplBase {
    
    private final UsuariosService usuariosService;
    private final UsuariosMapper usuariosMapper;
    private final Validator validator;

    // ... (métodos existentes)

    /**
     * Inicia sesión en el sistema.
     */
    @Override
    public void iniciarSesion(IniciarSesionRequest request, StreamObserver<IniciarSesionRequest.Response> responseObserver) {
        log.info("Recibida petición de inicio de sesión para: {}", request.getNombreUsuario());

        try {
            // Convertir request gRPC a DTO
            IniciarSesionRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO
            Set<ConstraintViolation<IniciarSesionRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.iniciarSesion(dto);

            // Convertir respuesta a gRPC y enviar
            IniciarSesionRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Sesión iniciada exitosamente para: {}", request.getNombreUsuario());

        } catch (Exception e) {
            log.error("Error al iniciar sesión", e);
            throw e;
        }
    }

    /**
     * Cierra la sesión actual.
     */
    @Override
    public void cerrarSesion(CerrarSesionRequest request, StreamObserver<CerrarSesionRequest.Response> responseObserver) {
        log.info("Recibida petición de cierre de sesión");

        try {
            // Obtener token del contexto de seguridad
            // El token se obtiene del header Authorization que fue procesado por AuthorizationInterceptor
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            String token = auth != null ? (String) auth.getCredentials() : null;
            
            // Ejecutar la lógica de negocio
            usuariosService.cerrarSesion(token);

            // Responder vacío (éxito)
            CerrarSesionRequest.Response grpcResponse = CerrarSesionRequest.Response.newBuilder().build();
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Sesión cerrada correctamente para usuario: {}", username);

        } catch (Exception e) {
            log.error("Error al cerrar sesión", e);
            throw e;
        }
    }

    /**
     * Verifica la validez de un token JWT.
     */
    @Override
    public void verificarToken(VerificarTokenRequest request, StreamObserver<VerificarTokenRequest.Response> responseObserver) {
        log.debug("Recibida petición de verificación de token");

        try {
            // Convertir request gRPC a DTO
            VerificarTokenRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO
            Set<ConstraintViolation<VerificarTokenRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.verificarToken(dto);

            // Convertir respuesta a gRPC y enviar
            VerificarTokenRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error al verificar token", e);
            throw e;
        }
    }
    
    // ... (otros métodos existentes)

    /**
     * Crea un nuevo usuario en el sistema.
     */
    @Override
    public void crearUsuario(CrearUsuarioRequest request, StreamObserver<CrearUsuarioRequest.Response> responseObserver) {
        log.info("Recibida petición para crear usuario: {}", request.getNombreUsuario());

        try {
            // Convertir request gRPC a DTO
            CrearUsuarioRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO
            Set<ConstraintViolation<CrearUsuarioRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            CrearUsuarioResponseDto response = usuariosService.crearUsuario(dto);

            // Convertir respuesta a gRPC y enviar
            CrearUsuarioRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Usuario creado exitosamente: {}", response.usuario().nombreUsuario());

        } catch (Exception e) {
            log.error("Error al crear usuario", e);
            throw e; // Se encargara el interceptor de capturarla
        }
    }

    /**
     * Crea un nuevo rol en el sistema.
     */
    @Override
    public void crearRol(CrearRolRequest request, StreamObserver<CrearRolRequest.Response> responseObserver) {
        log.info("Recibida petición para crear rol: {}", request.getNombreRol());

        try {
            // Convertir request gRPC a DTO
            CrearRolRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO
            Set<ConstraintViolation<CrearRolRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.crearRol(dto);

            // Convertir respuesta a gRPC y enviar
            CrearRolRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Rol creado exitosamente: {}", response.rol().nombreRol());

        } catch (Exception e) {
            log.error("Error al crear rol", e);
            throw e; // Se encargara el interceptor de capturarla
        }
    }

    /**
     * Asigna permisos a un rol existente.
     */
    @Override
    public void asignarPermisosARol(AsignarPermisosRequest request, StreamObserver<AsignarPermisosRequest.Response> responseObserver) {
        log.info("Recibida petición para asignar permisos al rol: {}", request.getIdRol());

        try {
            // Convertir request gRPC a DTO
            AsignarPermisosRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO
            Set<ConstraintViolation<AsignarPermisosRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.asignarPermisosARol(dto);

            // Convertir respuesta a gRPC y enviar
            AsignarPermisosRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Permisos asignados exitosamente al rol: {}", request.getIdRol());

        } catch (Exception e) {
            log.error("Error al asignar permisos al rol", e);
            throw e; // Se encargara el interceptor de capturarla
        }
    }

    /**
     * Lista todos los roles del sistema.
     */
    @Override
    public void listarRoles(ListarRolesRequest request, StreamObserver<ListarRolesRequest.Response> responseObserver) {
        log.info("Recibida petición para listar todos los roles");

        try {
            // Convertir request gRPC a DTO
            ListarRolesRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO (aunque está vacío, mantenemos la estructura)
            Set<ConstraintViolation<ListarRolesRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.listarRoles(dto);

            // Convertir respuesta a gRPC y enviar
            ListarRolesRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Roles listados exitosamente: {} roles encontrados", response.roles().size());

        } catch (Exception e) {
            log.error("Error al listar roles", e);
            throw e; // Se encargara el interceptor de capturarla
        }
    }

    /**
     * Lista todos los permisos del sistema.
     */
    @Override
    public void listarPermisos(ListarPermisosRequest request, StreamObserver<ListarPermisosRequest.Response> responseObserver) {
        log.info("Recibida petición para listar todos los permisos");

        try {
            // Convertir request gRPC a DTO
            ListarPermisosRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO (aunque está vacío, mantenemos la estructura)
            Set<ConstraintViolation<ListarPermisosRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.listarPermisos(dto);

            // Convertir respuesta a gRPC y enviar
            ListarPermisosRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Permisos listados exitosamente: {} permisos encontrados", response.permisos().size());

        } catch (Exception e) {
            log.error("Error al listar permisos", e);
            throw e; // Se encargara el interceptor de capturarla
        }
    }

    /**
     * Consulta usuarios con filtros opcionales.
     */
    @Override
    public void consultarUsuarios(com.isam.grpc.usuarios.ConsultarUsuariosRequest request, 
                                StreamObserver<com.isam.grpc.usuarios.ConsultarUsuariosRequest.Response> responseObserver) {
        log.info("Recibida petición para consultar usuarios");

        try {
            // Convertir request gRPC a DTO
            com.isam.dto.usuario.ConsultarUsuariosRequestDto dto = usuariosMapper.toDto(request);

            // Validar el DTO
            Set<ConstraintViolation<com.isam.dto.usuario.ConsultarUsuariosRequestDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                
                responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Errores de validación: " + errorMessage)
                    .asRuntimeException());
                return;
            }

            // Ejecutar la lógica de negocio
            var response = usuariosService.consultarUsuarios(dto);

            // Convertir respuesta a gRPC y enviar
            com.isam.grpc.usuarios.ConsultarUsuariosRequest.Response grpcResponse = usuariosMapper.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Consulta de usuarios completada: {} usuarios encontrados", response.usuarios().size());

        } catch (Exception e) {
            log.error("Error al consultar usuarios", e);
            throw e;
        }
    }

}