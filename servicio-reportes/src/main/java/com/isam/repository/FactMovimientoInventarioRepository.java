package com.isam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.isam.model.FactMovimientoInventario;

@Repository
public interface FactMovimientoInventarioRepository extends JpaRepository<FactMovimientoInventario, String> {

}
