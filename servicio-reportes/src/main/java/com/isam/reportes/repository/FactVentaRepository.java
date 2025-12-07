package com.isam.reportes.repository;

import com.isam.reportes.model.FactVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FactVentaRepository extends JpaRepository<FactVenta, String> {
}
