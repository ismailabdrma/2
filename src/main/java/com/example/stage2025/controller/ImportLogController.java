package com.example.stage2025.controller;

import com.example.stage2025.dto.ImportLogDto;
import com.example.stage2025.enums.ImportStatus;
import com.example.stage2025.service.ImportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/import-logs")
@PreAuthorize("hasRole('ADMIN')")
public class ImportLogController {

    private final ImportLogService importLogService;

    @Autowired
    public ImportLogController(ImportLogService importLogService) {
        this.importLogService = importLogService;
    }

    @GetMapping
    public ResponseEntity<Page<ImportLogDto>> getImportLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) ImportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<ImportLogDto> logs = importLogService.getImportLogs(page, size, sortBy, sortDir, supplierId, status, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
}
