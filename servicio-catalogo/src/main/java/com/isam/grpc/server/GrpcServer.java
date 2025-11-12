package com.isam.grpc.server;

import com.isam.grpc.interceptor.ExceptionInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GrpcServer {

    private final Logger LOG = LoggerFactory.getLogger(getClass());


    @Value("${grpc.port:9090}")
    int port;
    private Server server;

    @Autowired
    private GrpcServerService grpcServerService;
    @Autowired
    private ExceptionInterceptor exceptionInterceptor;

    public GrpcServer(){

    }

    public void start() throws IOException, InterruptedException {
        LOG.info("El servidor se está iniciado en el siguiente puerto {}", port);
        server = ServerBuilder.forPort(port)
                .addService(grpcServerService)
                .intercept(exceptionInterceptor)
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