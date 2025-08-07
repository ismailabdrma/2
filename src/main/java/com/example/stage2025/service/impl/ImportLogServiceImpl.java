// src/main/java/com/example/stage2025/service/impl/ImportLogServiceImpl.java
package com.example.stage2025.service.impl;

import com.example.stage2025.dto.ImportLogDto;
import com.example.stage2025.entity.ImportLog;
import com.example.stage2025.entity.Supplier;
import com.example.stage2025.enums.ImportStatus;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.ImportLogMapper;
import com.example.stage2025.repository.ImportLogRepository;
import com.example.stage2025.repository.SupplierRepository;
import com.example.stage2025.service.ImportLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImportLogServiceImpl implements ImportLogService {

    private final ImportLogRepository importLogRepository;
    private final SupplierRepository supplierRepository;
    private final ImportLogMapper importLogMapper;

    @Autowired
    public ImportLogServiceImpl(ImportLogRepository importLogRepository, SupplierRepository supplierRepository, ImportLogMapper importLogMapper) {
        this.importLogRepository = importLogRepository;
        this.supplierRepository = supplierRepository;
        this.importLogMapper = importLogMapper;
    }

    /* ---------- read methods ---------- */

    @Override
    public List<ImportLogDto> getLogsBySupplierId(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier not found: id=" + supplierId);
        }
        return importLogRepository.findBySupplierId(supplierId)
                .stream()
                .map(importLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ImportLogDto getLogById(Long id) {
        ImportLog log = importLogRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Import log not found: id=" + id));
        return importLogMapper.toDto(log);
    }

    /* ---------- write method ---------- */

    @Override
    public ImportLogDto saveLog(Long supplierId,
                                ImportStatus status,
                                int importedCount,
                                int errorCount,
                                String errorMessage) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Supplier not found: id=" + supplierId));

        ImportLog log = ImportLog.builder()
                .importDate(LocalDateTime.now())
                .status(status.name())      // write enum value as String
                .importedCount(importedCount)
                .errorCount(errorCount)
                .errorMessage(errorMessage)
                .supplier(supplier)
                .build();

        return importLogMapper.toDto(importLogRepository.save(log));
    }

    @Override
    public void logImport(Long supplierId, ImportStatus status, int productsProcessed, int errors, String errorMessage) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));

        ImportLog log = ImportLog.builder()
                .supplier(supplier)
                .status(status)
                .productsProcessed(productsProcessed)
                .errors(errors)
                .errorMessage(errorMessage)
                .build();
        importLogRepository.save(log);
    }

    @Override
    public Page<ImportLogDto> getImportLogs(int page, int size, String sortBy, String sortDir, Long supplierId, ImportStatus status, LocalDate startDate, LocalDate endDate) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<ImportLog> logs = importLogRepository.findByFilters(supplierId, status, startDateTime, endDateTime, pageable);
        return logs.map(importLogMapper::toDto);
    }
}
