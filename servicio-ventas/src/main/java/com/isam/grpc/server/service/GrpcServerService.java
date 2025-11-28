package com.isam.grpc.server.service;

import com.isam.dto.CrearNuevoTicketResponseDto;
import com.isam.grpc.ventas.*;
import com.isam.grpc.ventas.CrearNuevoTicketRequest.Response;
import com.isam.mapper.VentasMapper;
import com.isam.service.VentasService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcServerService extends VentasServiceGrpc.VentasServiceImplBase {
    
    private final VentasService ventasService;
    private final VentasMapper ventasMapper;
    
    @Override
    public void crearNuevoTicket(CrearNuevoTicketRequest request, StreamObserver<Response> responseObserver) {
        log.info("Iniciando creación de nuevo ticket temporal");
        
        // TODO: Obtener ID de usuario y nombre del contexto de autenticación
        // Por ahora usamos valores de ejemplo
        String idUsuario = "usuario-temporal";
        String nombreCajero = "Cajero Temporal";
        
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
            com.isam.dto.AnadirProductoTicketRequestDto dto = ventasMapper.toDto(request);
            
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
            responseObserver.onError(e);
        }
    }

    @Override
    public void procesarPago(ProcesarPagoRequest request,
            StreamObserver<com.isam.grpc.ventas.ProcesarPagoRequest.Response> responseObserver) {
        log.info("Recibida solicitud para procesar pago: idTicket='{}', metodoPago='{}', montoRecibido='{}'",
            request.getIdTicketTemporal(), request.getMetodoPago(), request.getMontoRecibido());
        
        try {
            // Convertir proto a DTO
            com.isam.dto.ProcesarPagoRequestDto dto = ventasMapper.toDto(request);
            
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
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void cerrarTicket(CerrarTicketRequest request,
            StreamObserver<com.isam.grpc.ventas.CerrarTicketRequest.Response> responseObserver) {
        log.info("Recibida solicitud para cerrar ticket: idTicket='{}'", request.getIdTicketTemporal());
        
        try {
            // Convertir proto a DTO
            com.isam.dto.CerrarTicketRequestDto dto = ventasMapper.toDto(request);
            
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
            responseObserver.onError(e);
        }
    }
}