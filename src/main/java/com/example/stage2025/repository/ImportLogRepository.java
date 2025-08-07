package com.example.stage2025.repository;

import com.example.stage2025.enums.ImportStatus;
import com.example.stage2025.entity.ImportLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {

    @Query("SELECT il FROM ImportLog il WHERE " +
            "(:supplierId IS NULL OR il.supplier.id = :supplierId) AND " +
            "(:status IS NULL OR il.status = :status) AND " +
            "(:startDate IS NULL OR il.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR il.timestamp <= :endDate)")
    Page<ImportLog> findByFilters(
            @Param("supplierId") Long supplierId,
            @Param("status") ImportStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
