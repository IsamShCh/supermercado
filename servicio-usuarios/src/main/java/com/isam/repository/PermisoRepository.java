package com.isam.repository;

import com.isam.model.Permiso;
import com.isam.model.enums.AccionPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de entidades Permiso.
 * Proporciona métodos CRUD y búsqueda especializada para permisos.
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, String>, JpaSpecificationExecutor<Permiso> {
    
    /**
     * Busca permisos por recurso específico.
     * @param recurso Recurso a buscar
     * @return Lista de permisos para el recurso especificado
     */
    java.util.List<Permiso> findByRecurso(String recurso);
    
    /**
     * Busca permisos por acción específica.
     * @param accion Acción a buscar
     * @return Lista de permisos con la acción especificada
     */
    java.util.List<Permiso> findByAccion(AccionPermiso accion);
    
    /**
     * Busca permisos por recurso y acción.
     * @param recurso Recurso del permiso
     * @param accion Acción del permiso
     * @return Lista de permisos que coinciden con recurso y acción
     */
    java.util.List<Permiso> findByRecursoAndAccion(String recurso, AccionPermiso accion);
    
    /**
     * Busca permisos por nombre parcial (búsqueda case-insensitive).
     * @param nombrePermiso Parte del nombre del permiso a buscar
     * @return Lista de permisos que coinciden con el criterio
     */
    java.util.List<Permiso> findByNombrePermisoContainingIgnoreCase(String nombrePermiso);
}