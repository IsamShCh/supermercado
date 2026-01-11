package com.isam.integration.client;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

/**
 * CallCredentials que inyecta un token JWT Bearer en las llamadas gRPC.
 * Esta es la forma idiomática recomendada por gRPC para autenticación.
 */
public class BearerTokenCallCredentials extends CallCredentials {
    
    private final String token;
    
    public BearerTokenCallCredentials(String token) {
        this.token = token;
    }
    
    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                headers.put(
                    Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
                    "Bearer " + token
                );
                applier.apply(headers);
            } catch (Throwable e) {
                applier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        // Este método es requerido por la interfaz pero no hace nada
    }
}