package com.isam.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad para el manejo seguro de contraseñas.
 * Proporciona métodos para generar salt, hashear contraseñas y verificarlas.
 */
@Component
public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 32;
    
    /**
     * Genera un salt aleatorio para hashear contraseñas.
     * @return Salt codificado en Base64
     */
    public String generarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashea una contraseña usando SHA-256 con el salt proporcionado.
     * @param password Contraseña en texto plano
     * @param salt Salt para el hash
     * @return Hash de la contraseña codificado en Base64
     */
    public String hashearPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Combinar password y salt
            String passwordConSalt = password + salt;
            
            // Generar hash
            byte[] hashBytes = md.digest(passwordConSalt.getBytes());
            
            // Codificar en Base64
            return Base64.getEncoder().encodeToString(hashBytes);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear contraseña: algoritmo no disponible", e);
        }
    }
    
    /**
     * Verifica si una contraseña coincide con el hash almacenado.
     * @param password Contraseña en texto plano a verificar
     * @param hashAlmacenado Hash almacenado en la base de datos
     * @param salt Salt usado para generar el hash
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public boolean verificarPassword(String password, String hashAlmacenado, String salt) {
        String hashGenerado = hashearPassword(password, salt);
        return hashGenerado.equals(hashAlmacenado);
    }
}