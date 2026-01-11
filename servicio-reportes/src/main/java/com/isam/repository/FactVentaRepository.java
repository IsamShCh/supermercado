package com.isam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.isam.model.FactVenta;

@Repository
public interface FactVentaRepository extends JpaRepository<FactVenta, String> {
}
