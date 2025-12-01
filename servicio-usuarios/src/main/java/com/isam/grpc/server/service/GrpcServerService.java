package com.isam.grpc.server.service;

import org.springframework.stereotype.Service;

import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.mapper.UsuariosMapper;
import com.isam.service.UsuariosService;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcServerService extends UsuarioServiceGrpc.UsuarioServiceImplBase {
    
    private final UsuariosService ventasService;
    private final UsuariosMapper ventasMapper;
    private final Validator validator;
    



}