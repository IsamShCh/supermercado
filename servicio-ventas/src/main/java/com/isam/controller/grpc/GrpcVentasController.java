package com.isam.controller.grpc;

import com.isam.dto.AnadirProductoTicketRequestDto;
import com.isam.dto.CancelarTicketRequestDto;
import com.isam.dto.CancelarTicketResponseDto;
import com.isam.dto.CerrarTicketRequestDto;
import com.isam.dto.ConsultarTicketRequestDto;
import com.isam.dto.ConsultarTicketResponseDto;
import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.dto.EliminarProductoTicketRequestDto;
import com.isam.dto.EliminarProductoTicketResponseDto;
import com.isam.dto.ProcesarPagoRequestDto;
import com.isam.grpc.ventas.*;
import com.isam.grpc.ventas.CrearNuevoTicketRequest.Response;
import com.isam.mapper.VentasMapper;
import com.isam.service.VentasService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcVentasController extends VentasServiceGrpc.VentasServiceImplBase {
    
    private final VentasService ventasService;
    private final VentasMapper ventasMapper;
    private final Validator validator;
    
    @Override
    public void crearNuevoTicket(CrearNuevoTicketRequest request, StreamObserver<Response> responseObserver) {
        log.info("Iniciando creación de nuevo ticket temporal");
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
             responseObserver.onError(Status.UNAUTHENTICATED.withDescription("Usuario no autenticado").asRuntimeException());
             return;
        }

        String idUsuario = auth.getName();
        String nombreCajero = auth.getName();
        
        // Llamar al servicio
        CrearNuevoTicketResponseDto responseDto = ventasService.crearNuevoTicket(idUsuario, nombreCajero);
        
        // Convertir a proto de gRPC
        Response responseProto = ventasMapper.toProto(responseDto);
        
        log.info("Ticket temporal creado exitosamente: {}", responseDto.idTicketTemporal());
        
        // Construir respuest
        responseObserver.onNext(responseProto);
        responseObserver.onCompleted();
    }

    @Override
    public void anadirProductoTicket(AnadirProductoTicketRequest request,
            StreamObserver<com.isam.grpc.ventas.AnadirProductoTicketRequest.Response> responseObserver) {
        log.info("Recibida solicitud para añadir producto al ticket: idTicket='{}', codigoBarras='{}'",
            request.getIdTicketTemporal(), request.getCodigoBarras());
        
        try {
            // Convertir proto a DTO
            AnadirProductoTicketRequestDto dto = ventasMapper.toDto(request);
            // Validar
            Set<ConstraintViolation<AnadirProductoTicketRequestDto>> violations = validator.validate(dto);
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
            com.isam.dto.AnadirProductoTicketResponseDto responseDto = ventasService.anadirProductoTicket(dto);
            
            // Convertir DTO a proto
            com.isam.grpc.ventas.AnadirProductoTicketRequest.Response responseProto = ventasMapper.toProto(responseDto);
            
            log.info("Producto añadido exitosamente al ticket: SKU='{}', Subtotal={}",
                responseDto.sku(), responseDto.subtotal());
            
            // Construir respuesta
            responseObserver.onNext(responseProto);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error al añadir producto al ticket: {}", e.getMessage());
            // responseObserver.onError(e);
            throw e;
        }
    }

    @Override
    public void procesarPago(ProcesarPagoRequest request,
            StreamObserver<com.isam.grpc.ventas.ProcesarPagoRequest.Response> responseObserver) {
        log.info("Recibida solicitud para procesar pago: idTicket='{}', metodoPago='{}', montoRecibido='{}'",
            request.getIdTicketTemporal(), request.getMetodoPago(), request.getMontoRecibido());
        
        try {
            // Convertir proto a DTO
            ProcesarPagoRequestDto dto = ventasMapper.toDto(request);
            // Validar
            Set<ConstraintViolation<ProcesarPagoRequestDto>> violations = validator.validate(dto);
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
            com.isam.dto.ProcesarPagoResponseDto responseDto = ventasService.procesarPago(dto);
            
            // Convertir DTO a proto
            com.isam.grpc.ventas.ProcesarPagoRequest.Response responseProto = ventasMapper.toProto(responseDto);
            
            log.info("Pago procesado exitosamente: idPago='{}', montoCambio={}",
                responseDto.idPago(), responseDto.montoCambio());
            
            // Construir respuesta
            responseObserver.onNext(responseProto);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error al procesar pago: {}", e.getMessage());
            // responseObserver.onError(e);
            throw e;
        }
    }
    
    @Override
    public void cerrarTicket(CerrarTicketRequest request,
            StreamObserver<com.isam.grpc.ventas.CerrarTicketRequest.Response> responseObserver) {
        log.info("Recibida solicitud para cerrar ticket: idTicket='{}'", request.getIdTicketTemporal());
        
        try {
            // Convertir proto a DTO
            CerrarTicketRequestDto dto = ventasMapper.toDto(request);
            // Validar
            Set<ConstraintViolation<CerrarTicketRequestDto>> violations = validator.validate(dto);
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
            com.isam.dto.CerrarTicketResponseDto responseDto = ventasService.cerrarTicket(dto);
            
            // Convertir DTO a proto
            com.isam.grpc.ventas.CerrarTicketRequest.Response responseProto = ventasMapper.toProto(responseDto);
            
            log.info("Ticket cerrado exitosamente: numeroTicket='{}', total={}",
                responseDto.numeroTicket(), responseDto.total());
            
            // Construir respuesta
            responseObserver.onNext(responseProto);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error al cerrar ticket: {}", e.getMessage());
            // responseObserver.onError(e);
            throw e;
        }
    }
    
    @Override
    public void consultarTicket(ConsultarTicketRequest request,
            StreamObserver<com.isam.grpc.ventas.ConsultarTicketRequest.Response> responseObserver) {
        log.info("Recibida solicitud para consultar ticket: idTicket='{}', numeroTicket='{}'",
            request.hasIdTicket() ? request.getIdTicket() : "null",
            request.hasNumeroTicket() ? request.getNumeroTicket() : "null");
        
        try {
            // Convertir proto a DTO
            ConsultarTicketRequestDto dto = ventasMapper.toDto(request);
            // Validar con el validator
            Set<ConstraintViolation<ConsultarTicketRequestDto>> violations = validator.validate(dto);
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
            // Validar el DTO
            dto.validate();
            
            // Llamar al servicio
            ConsultarTicketResponseDto responseDto = ventasService.consultarTicket(dto);
            
            // Convertir DTO a proto
            com.isam.grpc.ventas.ConsultarTicketRequest.Response responseProto = ventasMapper.toProto(responseDto);
            
            log.info("Ticket consultado exitosamente: idTicket='{}', estado={}",
                responseDto.idTicket(), responseDto.estado());
            
            // Construir respuesta
            responseObserver.onNext(responseProto);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error al consultar ticket: {}", e.getMessage());
            // responseObserver.onError(e);
            throw e;
        }
    }
    
    @Override
    public void cancelarTicket(CancelarTicketRequest request,
            StreamObserver<com.isam.grpc.ventas.CancelarTicketRequest.Response> responseObserver) {
        log.info("Recibida solicitud para cancelar ticket: idTicket='{}'", request.getIdTicket());
        
        try {
            // Convertir proto a DTO
            CancelarTicketRequestDto dto = ventasMapper.toDto(request);
            // Validar
            Set<ConstraintViolation<CancelarTicketRequestDto>> violations = validator.validate(dto);
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
            CancelarTicketResponseDto responseDto = ventasService.cancelarTicket(dto);
            
            // Convertir DTO a proto
            com.isam.grpc.ventas.CancelarTicketRequest.Response responseProto = ventasMapper.toProto(responseDto);
            
            log.info("Ticket cancelado exitosamente: idTicket='{}'", responseDto.idTicket());
            
            // Construir respuesta
            responseObserver.onNext(responseProto);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error al cancelar ticket: {}", e.getMessage());
            // responseObserver.onError(e);
            throw e;
        }
    }
    
    @Override
    public void eliminarProductoTicket(EliminarProductoTicketRequest request,
            StreamObserver<EliminarProductoTicketRequest.Response> responseObserver) {
        log.info("Recibida solicitud para eliminar producto del ticket: idTicket='{}', sku='{}', cantidadAEliminar={}",
            request.getIdTicket(), request.getSku(), request.hasCantidadAEliminar() ? request.getCantidadAEliminar() : "null");
        
        try {
            // Convertir proto a DTO
            EliminarProductoTicketRequestDto dto = ventasMapper.toDto(request);
            // Validar
            Set<ConstraintViolation<EliminarProductoTicketRequestDto>> violations = validator.validate(dto);
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
            EliminarProductoTicketResponseDto responseDto = ventasService.eliminarProductoTicket(dto);
            
            // Convertir DTO a proto
            EliminarProductoTicketRequest.Response responseProto = ventasMapper.toProto(responseDto);
            
            log.info("Producto eliminado exitosamente del ticket: SKU='{}', cantidadEliminada={}, subtotalActual={}",
                responseDto.sku(), responseDto.cantidadEliminada(), responseDto.subtotalTicketActual());
            
            // Construir respuesta
            responseObserver.onNext(responseProto);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error al eliminar producto del ticket: {}", e.getMessage());
            // responseObserver.onError(e);
            throw e;
        }
    }
}