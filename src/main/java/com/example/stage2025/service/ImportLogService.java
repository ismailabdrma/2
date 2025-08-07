package com.example.stage2025.service;

import com.example.stage2025.dto.ImportLogDto;
import com.example.stage2025.enums.ImportStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ImportLogService {
    void logImport(Long supplierId, ImportStatus status, int productsProcessed, int errors, String errorMessage);
    Page<ImportLogDto> getImportLogs(int page, int size, String sortBy, String sortDir, Long supplierId, ImportStatus status, LocalDate startDate, LocalDate endDate);
}
