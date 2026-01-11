package com.isam.controller.grpc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

//@Profile("!test")
@Component
@RequiredArgsConstructor
public class GrpcServerRunner implements CommandLineRunner {

    private final GrpcServer grpcServer;


    @Override
    public void run(String... args) throws Exception {
        grpcServer.start();
        grpcServer.block();
    }
}
