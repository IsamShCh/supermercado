package com.isam.repository;

import com.isam.model.ProductoCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoCacheRepository extends JpaRepository<ProductoCache, String> {
    
    Optional<ProductoCache> findByEan(String ean);
    
    Optional<ProductoCache> findByPlu(String plu);
}
