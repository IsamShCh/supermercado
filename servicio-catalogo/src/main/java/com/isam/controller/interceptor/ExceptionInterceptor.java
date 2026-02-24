package com.isam.controller.interceptor;


import io.grpc.*;
import org.springframework.stereotype.Component;

@Component
// NOTE - Este interceptor se aplicará automaticamente a todos los
// los servicios grpc sin distincion.
public class ExceptionInterceptor implements ServerInterceptor {

    // nos llega una llamada a un servicio y el correspondiente manejador de la llamada para poder iniciar la correspondiente llamaada.
    // Atendemos la llamada con el manejador de llamadas, haciendo uso del metodo iniciar llamada. Al iniciar la llamada obtenemos una
    // escucha para poder espiar la llamada.
    // Nosotros vamos a envolver esta escucha para personalizarla con la capacidad de capurar excepciones y de devolver los status de grpc correspondientes
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> llamada, Metadata metadatos, ServerCallHandler<ReqT, RespT> manejadorLlamada)
    {
        ServerCall.Listener<ReqT> listener = manejadorLlamada.startCall(llamada, metadatos);
        return new ExceptionListener<ReqT,RespT>(listener, llamada, metadatos);
    }
     private class ExceptionListener<ReqT, RespT>
             extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>{

        private final ServerCall<ReqT,RespT> serverCall;
        private final Metadata metadata;

         public ExceptionListener(ServerCall.Listener<ReqT> listener, ServerCall<ReqT, RespT> serverCall, Metadata metadata) {
             super(listener);
             this.serverCall = serverCall;
             this.metadata = metadata;
         }

         @Override
         // El cliente ha terminado de enviar su petición.
         public void onHalfClose(){
             try {
                 super.onHalfClose();
             } catch (RuntimeException exception){
                 manejarExcepcion(exception, serverCall, metadata);
                //  throw exception;
             }
         }
         @Override
         // El canal de comunicación hacia el cliente tiene capacidad para recibir más datos.
         public void onReady(){
             try{
                super.onReady();
             } catch (RuntimeException exception){
                    manejarExcepcion(exception, serverCall, metadata);
                    // throw exception;
             }

         }
         private void manejarExcepcion(RuntimeException exception, ServerCall<ReqT, RespT> serverCall, Metadata metadata){
             StatusRuntimeException statusRuntimeException = ExceptionUtils.trazarException(exception);
             try {
                 serverCall.close(statusRuntimeException.getStatus(), metadata);
             } catch (IllegalStateException e) {
                 // Ignorar si la llamada ya estaba cerrada
             }
         }
     }
}
