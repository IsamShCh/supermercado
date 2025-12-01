package com.isam.repository;

import com.isam.model.Usuario;
import com.isam.model.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Usuario.
 * Proporciona métodos CRUD y búsqueda especializada para usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>, JpaSpecificationExecutor<Usuario> {
    
    /**
     * Busca un usuario por su nombre de usuario único.
     * @param nombreUsuario Nombre de usuario a buscar
     * @return Optional con el usuario encontrado
     */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    
    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     * @param nombreUsuario Nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreUsuario(String nombreUsuario);
    
    /**
     * Busca usuarios por su estado.
     * @param estado Estado del usuario a buscar
     * @return Lista de usuarios con el estado especificado
     */
    java.util.List<Usuario> findByEstado(EstadoUsuario estado);
    
    /**
     * Busca usuarios que requieren cambio de contraseña.
     * @param true para buscar usuarios que requieren cambio, false para los que no
     * @return Lista de usuarios que requieren cambio de contraseña
     */
    java.util.List<Usuario> findByRequiereCambioContraseñaTrue();
    
    /**
     * Busca usuarios por nombre completo (búsqueda parcial).
     * @param nombreCompleto Parte del nombre completo a buscar
     * @return Lista de usuarios que coinciden con el criterio
     */
    java.util.List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);
}