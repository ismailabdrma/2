package com.example.stage2025.repository;

import com.example.stage2025.entity.ExcelSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelSupplierRepository extends JpaRepository<ExcelSupplier, Long> {
}
