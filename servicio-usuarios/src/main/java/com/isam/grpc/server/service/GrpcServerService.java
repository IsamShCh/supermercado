package com.isam.grpc.server.service;

import org.springframework.stereotype.Service;

import com.isam.dto.rol.AsignarPermisosRequestDto;
import com.isam.dto.rol.AsignarPermisosResponseDto;
import com.isam.dto.rol.CrearRolRequestDto;
import com.isam.grpc.usuarios.AsignarPermisosRequest;
import com.isam.grpc.usuarios.CrearRolRequest;
import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.mapper.UsuariosMapper;
import com.isam.mapper.UsuariosMapperAuto;
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
    private final UsuariosMapperAuto usuariosMapperAuto;
    private final Validator validator;

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
            AsignarPermisosRequestDto dto = usuariosMapperAuto.toDto(request);

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
            AsignarPermisosRequest.Response grpcResponse = usuariosMapperAuto.toProto(response);
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("Permisos asignados exitosamente al rol: {}", request.getIdRol());

        } catch (Exception e) {
            log.error("Error al asignar permisos al rol", e);
            throw e; // Se encargara el interceptor de capturarla
        }
    }

}