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
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Metodo anadirProductoTicket no esta implementado todavía")
                .asRuntimeException()
        );
    }

    @Override
    public void procesarPago(ProcesarPagoRequest request,
            StreamObserver<com.isam.grpc.ventas.ProcesarPagoRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Metodo procesarPago no esta implementado todavía")
                .asRuntimeException()
        );
    }
    
    @Override
    public void cerrarTicket(CerrarTicketRequest request,
            StreamObserver<com.isam.grpc.ventas.CerrarTicketRequest.Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Metodo cerrarTicket no esta implementado todavía")
                .asRuntimeException()
        );
    }
}