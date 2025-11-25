package com.isam.grpc.server.service;

import com.isam.grpc.ventas.*;
import com.isam.grpc.ventas.CrearNuevoTicketRequest.Response;

import io.grpc.Status;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class GrpcServerService extends VentasServiceGrpc.VentasServiceImplBase {
    
    @Override
    public void crearNuevoTicket(CrearNuevoTicketRequest request, StreamObserver<Response> responseObserver) {
        // TODO: Implementar
        responseObserver.onError(
            Status.UNIMPLEMENTED
                .withDescription("Metodo crearNuevoTicket no esta implementado todavía")
                .asRuntimeException()
        );
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