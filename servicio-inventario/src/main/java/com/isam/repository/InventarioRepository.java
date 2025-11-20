package com.isam.repository;

import com.isam.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, String>, JpaSpecificationExecutor<Inventario> {

    Optional<Inventario> findBySku(String sku);

    List<Inventario> findByUnidadMedida(String unidadMedida);

    @Query("SELECT i FROM Inventario i WHERE i.cantidadAlmacen > 0 OR i.cantidadEstanteria > 0")
    List<Inventario> findInventarioConStock();

    @Query("SELECT i FROM Inventario i WHERE i.sku = :sku AND (i.cantidadAlmacen > 0 OR i.cantidadEstanteria > 0)")
    Optional<Inventario> findBySkuWithStock(@Param("sku") String sku);
}