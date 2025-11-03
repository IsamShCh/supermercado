package com.isam.repository;

import com.isam.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {

    Optional<Proveedor> findByNombreProveedor(String nombreProveedor);

    @Query("SELECT p FROM Proveedor p WHERE p.nombreProveedor LIKE %:nombre%")
    List<Proveedor> findByNombreProveedorContaining(@Param("nombre") String nombre);

    @Query("SELECT p FROM Proveedor p WHERE p.email = :email")
    Optional<Proveedor> findByEmail(@Param("email") String email);

    boolean existsByNombreProveedor(String nombreProveedor);

    boolean existsByEmail(String email);
}