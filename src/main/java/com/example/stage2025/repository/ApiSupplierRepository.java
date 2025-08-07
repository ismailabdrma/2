package com.example.stage2025.repository;

import com.example.stage2025.entity.ApiSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiSupplierRepository extends JpaRepository<ApiSupplier, Long> {
}
