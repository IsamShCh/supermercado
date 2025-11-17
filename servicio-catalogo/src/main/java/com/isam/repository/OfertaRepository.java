package com.isam.repository;

import com.isam.model.Oferta;
import com.isam.model.EstadoOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, String> {

    List<Oferta> findByProducto_Sku(String sku);

    List<Oferta> findByEstado(EstadoOferta estado);

    @Query("SELECT o FROM Oferta o WHERE o.producto.sku = :sku AND o.estado = :estado")
    List<Oferta> findByProductoSkuAndEstado(@Param("sku") String sku, @Param("estado") EstadoOferta estado);

    @Query("SELECT o FROM Oferta o WHERE o.fechaInicio <= :fecha AND o.fechaFin >= :fecha AND o.estado = :estado")
    List<Oferta> findActiveOffersByDate(@Param("fecha") LocalDate fecha, @Param("estado") EstadoOferta estado);

    @Query("SELECT o FROM Oferta o WHERE o.fechaFin < :fecha AND o.estado = :estado")
    List<Oferta> findExpiredOffers(@Param("fecha") LocalDate fecha, @Param("estado") EstadoOferta estado);

    @Query("SELECT COUNT(o) FROM Oferta o WHERE o.producto.sku = :sku AND o.estado = :estado")
    long countByProductoSkuAndEstado(@Param("sku") String sku, @Param("estado") EstadoOferta estado);
}