package com.isam.grpc.interceptor;

import com.isam.grpc.client.UsuariosGrpcClient;
import com.isam.grpc.usuarios.RolProto;
import com.isam.grpc.usuarios.VerificarTokenRequest;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptor para gestionar la autorización delegando la validación al servicio de usuarios.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationInterceptor implements ServerInterceptor {

    private final UsuariosGrpcClient usuariosClient;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String token = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        // Si hay token, intentamos autenticar
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            
            try {
                // 1. Llamada remota a Servicio Usuarios
                VerificarTokenRequest.Response response = usuariosClient.verificarToken(jwt);

                if (response.getEsValido()) {
                    String username = response.getNombreUsuario();
                    
                    
                    List<SimpleGrantedAuthority> authorities = response.getRolesList().stream()
                        .flatMap(rol -> rol.getPermisosList().stream()) 
                        .map(permiso -> new SimpleGrantedAuthority(permiso.getNombrePermiso()))
                        .collect(Collectors.toList());

                    // Agregamos también los nombres de los roles como authorities por si los vamos a utilizar en el futuro, por si acaso
                    response.getRolesList().forEach(rol -> 
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol()))
                    );

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, jwt, authorities);

                    log.debug("Usuario autenticado remotamente: {} con {} permisos", username, authorities.size());

                    // 3. Propagar contexto
                    return new ContextPropagatingListener<>(
                            serverCallHandler.startCall(serverCall, metadata),
                            auth
                    );
                } else {
                    log.warn("Token rechazado por servicio de usuarios");
                    // Si el token es explícitamente inválido, rechazar
                    serverCall.close(Status.UNAUTHENTICATED.withDescription("Token inválido o expirado"), metadata);
                    return new ServerCall.Listener<>() {};
                }

            } catch (StatusRuntimeException e) {
                log.error("Error al validar token con servicio de usuarios: {}", e.getMessage());
                // Si el servicio de usuarios está caído, Fallar (Fail Safe).
                serverCall.close(Status.UNAVAILABLE.withDescription("No se pudo verificar la autenticación"), metadata);
                return new ServerCall.Listener<>() {};
            } catch (Exception e) {
                log.error("Error inesperado en autenticación: {}", e.getMessage());
                serverCall.close(Status.INTERNAL.withDescription("Error interno de autenticación"), metadata);
                return new ServerCall.Listener<>() {};
            }
        }

        // Si no hay token, continuamos como Anónimo. Los métodos protegidos por @PreAuthorize lo rechazarán si es necesario.
        return serverCallHandler.startCall(serverCall, metadata);
    }

    /**
     * Listener que establece el SecurityContext antes de delegar al listener original.
     */
    private static class ContextPropagatingListener<ReqT> extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
        private final UsernamePasswordAuthenticationToken authentication;

        public ContextPropagatingListener(ServerCall.Listener<ReqT> delegate, UsernamePasswordAuthenticationToken authentication) {
            super(delegate);
            this.authentication = authentication;
        }

        @Override
        public void onMessage(ReqT message) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                super.onMessage(message);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }

        @Override
        public void onHalfClose() {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                super.onHalfClose();
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        
        @Override
        public void onCancel() {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                super.onCancel();
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        
        @Override
        public void onComplete() {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                super.onComplete();
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        
        @Override
        public void onReady() {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                super.onReady();
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }
}