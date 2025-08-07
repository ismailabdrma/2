package com.example.stage2025.repository;

import com.example.stage2025.enums.PaymentStatus;
import com.example.stage2025.entity.SupplierPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {

    @Query("SELECT sp FROM SupplierPayment sp WHERE " +
            "(:supplierId IS NULL OR sp.supplier.id = :supplierId) AND " +
            "(:status IS NULL OR sp.status = :status) AND " +
            "(:startDate IS NULL OR sp.paymentDate >= :startDate) AND " +
            "(:endDate IS NULL OR sp.paymentDate <= :endDate)")
    Page<SupplierPayment> findByFilters(
            @Param("supplierId") Long supplierId,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
