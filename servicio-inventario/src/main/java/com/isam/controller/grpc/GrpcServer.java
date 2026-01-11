package com.isam.controller.grpc;



import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.isam.controller.interceptor.AuthorizationInterceptor;
import com.isam.controller.interceptor.ExceptionInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GrpcServer {

    private final Logger LOG = LoggerFactory.getLogger(getClass());


    @Value("${grpc.port:9091}")
    int port;
    private Server server;


    private final GrpcServerService grpcServerService;
    private final ExceptionInterceptor exceptionInterceptor;
    private final AuthorizationInterceptor authorizationInterceptor;


    public void start() throws IOException, InterruptedException {
        LOG.info("El servidor se está iniciado en el siguiente puerto {}", port);
        server = ServerBuilder.forPort(port)
                .addService(grpcServerService)
                .intercept(exceptionInterceptor)
                .intercept(authorizationInterceptor)
                .build()
                .start();
        LOG.info("Los siguientes servicios están disponibles:");
        server.getServices().stream().forEach(serv -> LOG.info(serv.getServiceDescriptor().getName()));

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
                    LOG.info("Apagando el servidor activo en el puerto {}", port);
                    GrpcServer.this.server.shutdown(); // El metodo shutdown() del objeto io.grpc.Server inicia un apagado elegante y ordenado del servidor.
                    LOG.info("Servidor apagado con exito");
                })
        );
    }

    private void stop(){
        if(this.server != null){
            server.shutdown();
        }

    }

    public void block() throws InterruptedException {
        // atender las solicitudes hasta que se reciba una solicitud de terminación
        if(this.server != null){
            server.awaitTermination();
        }
    }

}