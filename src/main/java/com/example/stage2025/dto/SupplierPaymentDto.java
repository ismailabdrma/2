package com.example.stage2025.dto;

import com.example.stage2025.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SupplierPaymentDto {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
}
