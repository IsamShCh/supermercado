package com.isam.grpc.interceptor;


import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.rpc.Code;
import io.grpc.StatusRuntimeException;
import org.hibernate.exception.ConstraintViolationException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.springframework.dao.DataIntegrityViolationException;

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

            if (cause instanceof ConstraintViolationException ) {
                status = com.google.rpc.Status.newBuilder()
                        .setCode(Code.ALREADY_EXISTS_VALUE)
                        .setMessage("La entidad que se intenta crear ya existe:\n" + cause.getMessage())
                        .build();
            }
            else if(cause instanceof SocketException){
                String mensajeError = "Error con los sockets o algo asi: ";
                status = com.google.rpc.Status.newBuilder()
                        .setCode(Code.INTERNAL_VALUE)
                        .setMessage(mensajeError + cause.getMessage())
                        .addDetails(mensajeGenerico != null ? Any.pack(mensajeGenerico) : Any.getDefaultInstance())
                        .build();

            } else {
                status = com.google.rpc.Status.newBuilder()
                        .setCode(com.google.rpc.Code.INTERNAL_VALUE)
                        .setMessage(cause != null ? "Internal server error:\n"+ cause.getMessage() : "Internal server error")
                        .addDetails(mensajeGenerico != null ? Any.pack(mensajeGenerico) : Any.getDefaultInstance())
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
