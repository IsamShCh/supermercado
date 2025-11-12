package com.isam.repository;

import com.isam.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombreCategoria(String nombreCategoria);

    @Query("SELECT c FROM Categoria c WHERE c.nombreCategoria LIKE %:nombre%")
    List<Categoria> findByNombreCategoriaContaining(@Param("nombre") String nombre);

    boolean existsByNombreCategoria(String nombreCategoria);
}