package com.isam.repository;

import com.isam.model.Producto;
import com.isam.model.EstadoProducto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    Optional<Producto> findByEan(String ean);

    Optional<Producto> findByPlu(String plu);

    Optional<Producto> findBySku(String sku);

    List<Producto> findByEstado(EstadoProducto estado);

    List<Producto> findByCategoria_IdCategoria(long idCategoria);

    @Query("SELECT p FROM Producto p WHERE p.nombre LIKE %:nombre%")
    List<Producto> findByNombreContaining(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.etiquetas LIKE %:etiqueta%")
    List<Producto> findByEtiquetasContaining(@Param("etiqueta") String etiqueta);

    @Query("SELECT p FROM Producto p WHERE p.estado = :estado AND p.nombre LIKE %:nombre%")
    List<Producto> findByEstadoAndNombreContaining(@Param("estado") EstadoProducto estado, @Param("nombre") String nombre);

    boolean existsByEan(String ean);

    boolean existsByPlu(String plu);
}