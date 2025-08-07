package com.example.stage2025.controller;

import com.example.stage2025.dto.SupplierPaymentDto;
import com.example.stage2025.enums.PaymentStatus;
import com.example.stage2025.service.SupplierPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/supplier-payments")
@PreAuthorize("hasRole('ADMIN')")
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;

    @Autowired
    public SupplierPaymentController(SupplierPaymentService supplierPaymentService) {
        this.supplierPaymentService = supplierPaymentService;
    }

    @GetMapping
    public ResponseEntity<Page<SupplierPaymentDto>> getSupplierPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<SupplierPaymentDto> payments = supplierPaymentService.getSupplierPayments(page, size, sortBy, sortDir, supplierId, status, startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SupplierPaymentDto> updatePaymentStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        SupplierPaymentDto updatedPayment = supplierPaymentService.updateSupplierPaymentStatus(id, status);
        return ResponseEntity.ok(updatedPayment);
    }
}
