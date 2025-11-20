package com.isam.grpc.server.service;

import com.isam.grpc.inventario.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GrpcServerService extends InventarioServiceGrpc.InventarioServiceImplBase {

    @Autowired
    private com.isam.service.InventarioService inventarioService;
    @Autowired
    private com.isam.mapper.InventarioMapper inventarioMapper;
    @Autowired
    private Validator validator;

    @Override
    public void registrarNuevasExistencias(RegistrarNuevasExistenciasRequest request, StreamObserver<RegistrarNuevasExistenciasRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Method registrarNuevasExistencias not yet implemented")
                .asRuntimeException()
        );
    }

    @Override
    public void ajustarInventarioManual(AjustarInventarioManualRequest request, StreamObserver<AjustarInventarioManualRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Method ajustarInventarioManual not yet implemented")
                .asRuntimeException()
        );
    }

    @Override
    public void moverStockEstanteria(MoverStockEstanteriaRequest request, StreamObserver<MoverStockEstanteriaRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Method moverStockEstanteria not yet implemented")
                .asRuntimeException()
        );
    }

    @Override
    public void contabilizarStockManual(ContabilizarStockManualRequest request, StreamObserver<ContabilizarStockManualRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Method contabilizarStockManual not yet implemented")
                .asRuntimeException()
        );
    }

    @Override
    public void consultarInventario(ConsultarInventarioRequest request, StreamObserver<ConsultarInventarioRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Method consultarInventario not yet implemented")
                .asRuntimeException()
        );
    }

    @Override
    @Transactional
    public void agregarProveedor(AgregarProveedorRequest request, StreamObserver<AgregarProveedorRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        com.isam.dto.proveedor.AgregarProveedorRequestDto dto = inventarioMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<com.isam.dto.proveedor.AgregarProveedorRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errores = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(errores)
                    .asRuntimeException()
            );
            return;
        }
        
        // Llamar al servicio
        com.isam.dto.proveedor.ProveedorDto proveedorDto = inventarioService.agregarProveedor(dto);
        
        // Convertir a proto usando el mapper
        com.isam.model.Proveedor proveedorEntity = new com.isam.model.Proveedor();
        proveedorEntity.setIdProveedor(proveedorDto.idProveedor());
        proveedorEntity.setNombreProveedor(proveedorDto.nombreProveedor());
        proveedorEntity.setContacto(proveedorDto.contacto());
        proveedorEntity.setDireccion(proveedorDto.direccion());
        proveedorEntity.setTelefono(proveedorDto.telefono());
        proveedorEntity.setEmail(proveedorDto.email());
        
        ProveedorProto proveedorProto = inventarioMapper.toProto(proveedorEntity);
        
        // Construir respuesta
        AgregarProveedorRequest.Response response = AgregarProveedorRequest.Response.newBuilder()
            .setProveedor(proveedorProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}