package com.isam.repository;

import com.isam.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Rol.
 * Proporciona métodos CRUD y búsqueda especializada para roles.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, String>, JpaSpecificationExecutor<Rol> {
    
    /**
     * Busca un rol por su nombre único.
     * @param nombreRol Nombre del rol a buscar
     * @return Optional con el rol encontrado
     */
    Optional<Rol> findByNombreRol(String nombreRol);
    
    /**
     * Verifica si existe un rol con el nombre especificado.
     * @param nombreRol Nombre del rol a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombreRol(String nombreRol);
    
    /**
     * Busca roles por nombre parcial (búsqueda case-insensitive).
     * @param nombreRol Parte del nombre del rol a buscar
     * @return Lista de roles que coinciden con el criterio
     */
    java.util.List<Rol> findByNombreRolContainingIgnoreCase(String nombreRol);
    
    /**
     * Busca roles por una lista de IDs.
     * @param ids Lista de IDs de roles a buscar
     * @return Lista de roles encontrados
     */
    java.util.List<Rol> findByIdRolIn(java.util.List<String> ids);
}