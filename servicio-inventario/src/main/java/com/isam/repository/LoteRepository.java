package com.isam.repository;

import com.isam.model.Lote;
import com.isam.model.EstadoLote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, String>, JpaSpecificationExecutor<Lote> {

    List<Lote> findBySku(String sku);

    List<Lote> findByIdInventario(String idInventario);

    List<Lote> findByEstado(EstadoLote estado);

    List<Lote> findBySkuAndEstado(String sku, EstadoLote estado);

    Optional<Lote> findByNumeroLoteAndSku(String numeroLote, String sku);

    @Query("SELECT l FROM Lote l WHERE l.fechaCaducidad <= :fecha")
    List<Lote> findLotesCaducados(@Param("fecha") LocalDate fecha);

    @Query("SELECT l FROM Lote l WHERE l.fechaCaducidad BETWEEN :fechaInicio AND :fechaFin")
    List<Lote> findLotesCaducandoEntre(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT l FROM Lote l WHERE l.sku = :sku AND l.estado = :estado AND (l.cantidadAlmacen + l.cantidadEstanteria) > 0")
    List<Lote> findLotesDisponiblesPorSku(@Param("sku") String sku, @Param("estado") EstadoLote estado);

    List<Lote> findBySkuAndEstadoOrderByFechaIngresoAsc(String sku, EstadoLote estado);
}