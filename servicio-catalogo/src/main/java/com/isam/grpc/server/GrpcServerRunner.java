package com.isam.grpc.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//@Profile("!test")
@Component
public class GrpcServerRunner implements CommandLineRunner {
    @Autowired
    private GrpcServer grpcServer;

    public GrpcServerRunner(){
    }

    @Override
    public void run(String... args) throws Exception {
        grpcServer.start();
        grpcServer.block();
    }
}
