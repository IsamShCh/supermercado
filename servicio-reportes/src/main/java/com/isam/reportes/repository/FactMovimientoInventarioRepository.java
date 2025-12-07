package com.isam.reportes.repository;

import com.isam.reportes.model.FactMovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactMovimientoInventarioRepository extends JpaRepository<FactMovimientoInventario, String> {

}
