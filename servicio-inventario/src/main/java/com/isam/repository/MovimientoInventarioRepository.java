package com.isam.repository;

import com.isam.model.MovimientoInventario;
import com.isam.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, String>, JpaSpecificationExecutor<MovimientoInventario> {

    List<MovimientoInventario> findBySku(String sku);

    List<MovimientoInventario> findByIdLote(String idLote);

    List<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento);

    List<MovimientoInventario> findByIdUsuario(String idUsuario);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<MovimientoInventario> findMovimientosEntreFechas(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.sku = :sku AND m.fechaHora BETWEEN :fechaInicio AND :fechaFin")
    List<MovimientoInventario> findMovimientosPorSkuEntreFechas(@Param("sku") String sku, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT m FROM MovimientoInventario m WHERE m.tipoMovimiento = :tipo AND m.fechaHora >= :fechaDesde ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findMovimientosRecientesPorTipo(@Param("tipo") TipoMovimiento tipo, @Param("fechaDesde") LocalDateTime fechaDesde);
}