package com.example.stage2025.dto;

import com.example.stage2025.enums.ImportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImportLogDto {
    private Long id;
    private LocalDateTime timestamp;
    private Long supplierId;
    private String supplierName;
    private ImportStatus status;
    private int productsProcessed;
    private int errors;
    private String errorMessage;
}
