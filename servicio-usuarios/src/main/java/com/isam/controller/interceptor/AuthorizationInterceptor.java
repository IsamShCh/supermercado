package com.isam.controller.interceptor;

import com.isam.repository.SesionRepository;
import com.isam.controller.util.JwtUtil;
import com.isam.model.enums.EstadoSesion;
import io.grpc.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptor para gestionar la autorización basada en tokens JWT.
 * Extrae el token de los metadatos, lo valida y establece el contexto de seguridad.
 */
@Component
public class AuthorizationInterceptor implements ServerInterceptor {

    private final JwtUtil jwtUtil;
    private final SesionRepository sesionRepository;

    public AuthorizationInterceptor(JwtUtil jwtUtil, SesionRepository sesionRepository) {
        this.jwtUtil = jwtUtil;
        this.sesionRepository = sesionRepository;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String token = metadata.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        // Si hay token, intentamos autenticar
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            
            // Validar firma y expiración del token, el JWT puro
            if (jwtUtil.validateToken(jwt)) {
                
                // Validar estado de la sesión en Base de Datos (Stateful check)
                boolean sesionActiva = sesionRepository.findByTokenJWT(jwt)
                        .map(sesion -> sesion.getEstado() == EstadoSesion.ACTIVA)
                        .orElse(false);

                if (sesionActiva) {
                    String username = jwtUtil.extractUsername(jwt);
                    
                    // Extraer permisos, el authorities
                    List<?> rawAuthorities = jwtUtil.extractClaim(jwt, claims -> claims.get("authorities", List.class));
                    
                    if (rawAuthorities != null) {
                        List<SimpleGrantedAuthority> authorities = rawAuthorities.stream()
                                .map(obj -> new SimpleGrantedAuthority(obj.toString()))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, jwt, authorities);

                        // Envolvemos el listener para propagar el contexto de seguridad
                        return new ContextPropagatingListener<>(
                                serverCallHandler.startCall(serverCall, metadata),
                                auth
                        );
                    }
                }
            }
            // Si token invalido o sesion cerrada/no encontrada -> Continuamos como Anónimo.
        }

        // Si no hay token o es inválido, continuamos sin contexto de seguridad (Anónimo)
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
        public void onReady() {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                super.onReady();
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
    }
}
