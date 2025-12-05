package com.isam.grpc.client;

import com.isam.grpc.usuarios.UsuarioServiceGrpc;
import com.isam.grpc.usuarios.VerificarTokenRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UsuariosGrpcClient {

    @Value("${grpc.client.usuarios.host:localhost}")
    private String usuariosHost;

    @Value("${grpc.client.usuarios.port:9093}")
    private int usuariosPort;

    private ManagedChannel channel;
    private UsuarioServiceGrpc.UsuarioServiceBlockingStub usuariosStub;

    @PostConstruct
    public void init() {
        log.info("Iniciando cliente gRPC de Usuarios en {}:{}", usuariosHost, usuariosPort);
        channel = ManagedChannelBuilder
                .forAddress(usuariosHost, usuariosPort)
                .usePlaintext()
                .build();

        usuariosStub = UsuarioServiceGrpc.newBlockingStub(channel);
        log.info("Cliente gRPC de Usuarios iniciado correctamente");
    }

    @PreDestroy
    public void cleanup() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            log.info("Canal gRPC con servicio de usuarios cerrado");
        }
    }

    /**
     * Verifica un token JWT llamando al servicio de usuarios.
     *
     * @param token Token JWT a verificar
     * @return Respuesta del servicio con los datos del usuario y validación
     * @throws StatusRuntimeException Si hay error en la comunicación
     */
    public VerificarTokenRequest.Response verificarToken(String token) throws StatusRuntimeException {
        log.debug("Verificando token con servicio de usuarios...");
        
        VerificarTokenRequest request = VerificarTokenRequest.newBuilder()
                .setTokenJwt(token)
                .build();

        return usuariosStub.verificarToken(request);
    }
}
