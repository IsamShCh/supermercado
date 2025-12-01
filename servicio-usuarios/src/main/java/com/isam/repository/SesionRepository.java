package com.isam.repository;

import com.isam.model.Sesion;
import com.isam.model.enums.EstadoSesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de entidades Sesion.
 * Proporciona métodos CRUD y búsqueda especializada para sesiones de usuario.
 */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, String>, JpaSpecificationExecutor<Sesion> {
    
    /**
     * Busca una sesión por su token JWT.
     * @param tokenJWT Token JWT a buscar
     * @return Optional con la sesión encontrada
     */
    Optional<Sesion> findByTokenJWT(String tokenJWT);
    
    /**
     * Busca sesiones por usuario.
     * @param idUsuario ID del usuario a buscar
     * @return Lista de sesiones del usuario
     */
    List<Sesion> findByUsuarioIdUsuario(String idUsuario);
    
    /**
     * Busca sesiones activas.
     * @return Lista de sesiones con estado ACTIVA
     */
    List<Sesion> findByEstado(EstadoSesion estado);
    
    /**
     * Busca sesiones expiradas (antes de una fecha específica).
     * @param fechaLimite Fecha límite para buscar sesiones expiradas
     * @return Lista de sesiones expiradas antes de la fecha límite
     */
    List<Sesion> findByFechaHoraFinBeforeAndEstado(LocalDateTime fechaLimite, EstadoSesion estado);
    
    /**
     * Elimina todas las sesiones expiradas.
     * @param fechaLimite Fecha límite para considerar expiración
     * @return Número de sesiones eliminadas
     */
    long deleteByFechaHoraFinBeforeAndEstado(LocalDateTime fechaLimite, EstadoSesion estado);
    
    /**
     * Busca sesiones de un usuario en un rango de fechas.
     * @param idUsuario ID del usuario
     * @param fechaInicio Fecha de inicio del rango
     * @param fechaFin Fecha de fin del rango
     * @return Lista de sesiones en el rango especificado
     */
    List<Sesion> findByUsuarioIdUsuarioAndFechaHoraInicioBetween(String idUsuario, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}