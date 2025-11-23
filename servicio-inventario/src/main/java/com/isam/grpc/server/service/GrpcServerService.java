package com.isam.grpc.server.service;

import com.isam.dto.inventario.CrearInventarioRequestDto;
import com.isam.dto.inventario.InventarioDto;
import com.isam.dto.inventario.ConsultarInventarioRequestDto;
import com.isam.dto.inventario.ConsultarInventarioResponseDto;
import com.isam.grpc.inventario.*;
import com.isam.grpc.inventario.CrearInventarioRequest.Response;

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
    @Transactional
    public void registrarNuevasExistencias(RegistrarNuevasExistenciasRequest request, StreamObserver<RegistrarNuevasExistenciasRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto dto = inventarioMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<com.isam.dto.existencias.RegistrarNuevasExistenciasRequestDto>> violations = validator.validate(dto);
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
        com.isam.dto.existencias.RegistrarNuevasExistenciasResponseDto responseDto = inventarioService.registrarNuevasExistencias(dto);
        
        // Convertir DTOs directamente a proto
        LoteProto loteProto = inventarioMapper.toProto(responseDto.lote());
        InventarioProto inventarioProto = inventarioMapper.toProto(responseDto.inventario());
        
        // Construir respuesta
        RegistrarNuevasExistenciasRequest.Response response = RegistrarNuevasExistenciasRequest.Response.newBuilder()
            .setLote(loteProto)
            .setInventario(inventarioProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
    @Transactional
    public void moverStockEstanteria(MoverStockEstanteriaRequest request, StreamObserver<MoverStockEstanteriaRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        com.isam.dto.stock.MoverStockEstanteriaRequestDto dto = inventarioMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<com.isam.dto.stock.MoverStockEstanteriaRequestDto>> violations = validator.validate(dto);
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
        com.isam.dto.stock.MoverStockEstanteriaResponseDto responseDto = inventarioService.moverStockEstanteria(dto);
        
        // Convertir DTOs a proto
        InventarioProto inventarioProto = inventarioMapper.toProto(responseDto.inventario());
        MovimientoInventarioProto movimientoProto = inventarioMapper.toProto(responseDto.movimiento());
        
        // Construir respuesta
        MoverStockEstanteriaRequest.Response response = MoverStockEstanteriaRequest.Response.newBuilder()
            .setInventario(inventarioProto)
            .setMovimiento(movimientoProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
    @Transactional
    public void consultarInventario(ConsultarInventarioRequest request, StreamObserver<ConsultarInventarioRequest.Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        ConsultarInventarioRequestDto dto = inventarioMapper.toDto(request);
        
        // Validar
        Set<ConstraintViolation<ConsultarInventarioRequestDto>> violations = validator.validate(dto);
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
        ConsultarInventarioResponseDto responseDto = inventarioService.consultarInventario(dto);
        
        // Convertir DTO a proto
        DetallesInventarioCompleto detallesProto = inventarioMapper.toProto(responseDto);
        
        // Construir respuesta
        ConsultarInventarioRequest.Response response = ConsultarInventarioRequest.Response.newBuilder()
            .setDetallesInventario(detallesProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
        ProveedorProto proveedorProto = inventarioMapper.toProto(proveedorDto);
        
        // Construir respuesta
        AgregarProveedorRequest.Response response = AgregarProveedorRequest.Response.newBuilder()
            .setProveedor(proveedorProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void crearInventario(CrearInventarioRequest request, StreamObserver<Response> responseObserver) {
        // Convertir la solicitud gRPC a DTO
        CrearInventarioRequestDto dto = inventarioMapper.toDto(request);

        // Validar
        Set<ConstraintViolation<CrearInventarioRequestDto>> violations = validator.validate(dto);
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
        InventarioDto inventarioDto = inventarioService.crearInventario(dto);

        // Convertir a proto
        InventarioProto inventarioProto = inventarioMapper.toProto(inventarioDto);
        
        // Construir respuesta
        CrearInventarioRequest.Response response = CrearInventarioRequest.Response.newBuilder()
            .setInventario(inventarioProto)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();


    }

}