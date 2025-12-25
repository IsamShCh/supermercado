package com.isam.grpc.interceptor;

import com.isam.util.JwtValidationUtil;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptor para gestionar la autorización mediante validación local de JWT.
 * Desacoplado del servicio de usuarios para mayor resiliencia.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationInterceptor implements ServerInterceptor {

    private final JwtValidationUtil jwtUtil;

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
                // Validación LOCAL (Sin llamada remota)
                if (jwtUtil.validateToken(jwt)) {
                    Claims claims = jwtUtil.extractAllClaims(jwt);
                    String username = claims.getSubject();

                    // Extraer authorities del token
                    @SuppressWarnings("unchecked")
                    List<String> authoritiesStr = (List<String>) claims.get("authorities");

                    List<SimpleGrantedAuthority> authorities = authoritiesStr.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // También extraemos roles si es necesario para compatibilidad
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) claims.get("roles");
                    if (roles != null) {
                        roles.forEach(rol -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rol)));
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, jwt,
                            authorities);

                    log.debug("Usuario autenticado localmente: {} con {} permisos", username, authorities.size());

                    // Propagar contexto
                    return new ContextPropagatingListener<>(
                            serverCallHandler.startCall(serverCall, metadata),
                            auth);
                } else {
                    log.warn("Token inválido o expirado (Validación Local)");
                    serverCall.close(Status.UNAUTHENTICATED.withDescription("Token inválido o expirado"), metadata);
                    return new ServerCall.Listener<>() {
                    };
                }

            } catch (Exception e) {
                log.error("Error inesperado en autenticación local: {}", e.getMessage());
                serverCall.close(Status.INTERNAL.withDescription("Error interno de autenticación"), metadata);
                return new ServerCall.Listener<>() {
                };
            }
        }

        // Si no hay token, continuamos como Anónimo.
        return serverCallHandler.startCall(serverCall, metadata);
    }

    /**
     * Listener que establece el SecurityContext antes de delegar al listener
     * original.
     */
    private static class ContextPropagatingListener<ReqT>
            extends ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT> {
        private final UsernamePasswordAuthenticationToken authentication;

        public ContextPropagatingListener(ServerCall.Listener<ReqT> delegate,
                UsernamePasswordAuthenticationToken authentication) {
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