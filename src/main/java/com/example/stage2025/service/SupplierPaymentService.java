package com.example.stage2025.service;

import com.example.stage2025.dto.SupplierPaymentDto;
import com.example.stage2025.enums.PaymentStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface SupplierPaymentService {
    List<SupplierPaymentDto> getAllPayments();
    List<SupplierPaymentDto> getPaymentsBySupplierId(Long supplierId);
    SupplierPaymentDto getPaymentById(Long id);
    SupplierPaymentDto createPayment(SupplierPaymentDto dto);
    Page<SupplierPaymentDto> getSupplierPayments(int page, int size, String sortBy, String sortDir, Long supplierId, PaymentStatus status, LocalDate startDate, LocalDate endDate);
    SupplierPaymentDto updateSupplierPaymentStatus(Long paymentId, PaymentStatus newStatus);
    // Optionally: Add update/delete methods if needed
}
