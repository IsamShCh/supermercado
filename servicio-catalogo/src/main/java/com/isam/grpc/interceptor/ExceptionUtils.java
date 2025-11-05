package com.isam.grpc.interceptor;


import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.rpc.Code;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;

import java.net.SocketException;

public class ExceptionUtils {


    public static StatusRuntimeException trazarException(Throwable exception){

        return trazarException(exception, null);
    }

    public static <T extends GeneratedMessage> StatusRuntimeException trazarException(Throwable excepcion, T mensajeGenerico){

        com.google.rpc.Status status = null;
        StatusRuntimeException statusRuntimeException;


        if(excepcion instanceof StatusRuntimeException){
            return (StatusRuntimeException) excepcion;
        }
        else{
            Throwable cause = excepcion;
            if(cause != null && cause.getCause() != null && cause != cause.getCause()){
                cause = cause.getCause();
            }

            if(cause instanceof SocketException){

// También podemos hacerlo asi:
//                Status status = Status.INVALID_ARGUMENT
//                        .withDescription("El ID del recurso no puede estar vacío.");
//                StatusRuntimeException exception = status.asRuntimeException();

                String mensajeError = "Error con los sockets o algo asi: ";
                status = com.google.rpc.Status.newBuilder()
                        .setCode(Code.UNAVAILABLE_VALUE)
                        .setMessage(mensajeError + cause.getMessage())
                        .addDetails(Any.pack(mensajeGenerico))
                        .build();

//                // 1. Crea los detalles estructurados del error (opcional pero potente)
//                BadRequest.FieldViolation violation = BadRequest.FieldViolation.newBuilder()
//                        .setField("name")
//                        .setDescription("El campo 'name' no puede exceder los 50 caracteres.")
//                        .build();
//                BadRequest badRequestDetails = BadRequest.newBuilder()
//                        .addFieldViolations(violation)
//                        .build();
//
//                // 2. Construye el objeto com.google.rpc.Status
//                com.google.rpc.Status statusProto = com.google.rpc.Status.newBuilder()
//                        .setCode(Code.INVALID_ARGUMENT_VALUE) // ¡Ojo! Es _VALUE para diferenciarlo del de grpc.core
//                        .setMessage("La petición contiene valores inválidos.")
//                        .addDetails(Any.pack(badRequestDetails)) // Empaqueta los detalles
//                        .build();
//
//                // 3. Convierte el objeto Protobuf a StatusRuntimeException
//                //    Aquí entra en juego la clase de utilidad StatusProto.
//                StatusRuntimeException exception = StatusProto.toStatusRuntimeException(statusProto);

            } else {
                status = com.google.rpc.Status.newBuilder()
                        .setCode(com.google.rpc.Code.INTERNAL_VALUE)
                        .setMessage("Internal server error")
                        .addDetails(Any.pack(mensajeGenerico))
                        .build();
            }
            statusRuntimeException = StatusProto.toStatusRuntimeException(status);
        }

        return statusRuntimeException;
    }

    public static <Generico extends GeneratedMessage> void observarError(
            StreamObserver<Generico> responseObserver,
            Throwable throwable
    ){
        responseObserver.onError(trazarException(throwable));

    }

    public static <Generico extends GeneratedMessage> void observarError(
            StreamObserver<Generico> responseObserver,
            Throwable throwable,
            Generico mensajeDetalles
    ){
        responseObserver.onError(trazarException(throwable, mensajeDetalles));
    }



}
