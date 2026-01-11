package com.isam.controller.util;

import com.isam.model.Rol;
import com.isam.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilidad para la generación y validación de tokens JWT.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:secreto_por_defecto_muy_largo_para_que_sea_seguro_en_desarrollo_minimo_256_bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long expiration;

    private Key getSigningKey() {
        // Si el secreto está en Base64, habría que decodificarlo.
        // Aquí asumimos que es una cadena simple y usamos Keys.hmacShaKeyFor
        // Asegurarse de que el secreto tenga al menos 256 bits (32 caracteres)
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genera un token JWT para el usuario especificado.
     * @param usuario Usuario para el que se genera el token
     * @return Token JWT generado
     */
    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUsuario", usuario.getIdUsuario());
        claims.put("nombreCompleto", usuario.getNombreCompleto());
        
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombreRol)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        List<String> authorities = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .map(com.isam.model.Permiso::getNombrePermiso)
                .distinct()
                .collect(Collectors.toList());
        claims.put("authorities", authorities);

        return createToken(claims, usuario.getNombreUsuario());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida si un token JWT es válido.
     * @param token Token JWT a validar
     * @return true si es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) del token.
     * @param token Token JWT
     * @return Nombre de usuario
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID de usuario del token.
     * @param token Token JWT
     * @return ID de usuario
     */
    public String extractIdUsuario(String token) {
        return extractClaim(token, claims -> claims.get("idUsuario", String.class));
    }

    /**
     * Extrae la fecha de expiración del token.
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
