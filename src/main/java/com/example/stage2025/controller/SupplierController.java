package com.example.stage2025.controller;

import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.dto.SupplierDto;
import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@PreAuthorize("hasRole('ADMIN')")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<Page<SupplierDto>> getSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DataFormat dataFormat,
            @RequestParam(required = false) Boolean active) {
        Page<SupplierDto> suppliers = supplierService.getSuppliers(page, size, sortBy, sortDir, search, dataFormat, active);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDto> getSupplierById(@PathVariable Long id) {
        SupplierDto supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @PostMapping
    public ResponseEntity<SupplierDto> createSupplier(@Valid @RequestBody SupplierDto supplierDto) {
        SupplierDto newSupplier = supplierService.createSupplier(supplierDto);
        return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDto> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDto supplierDto) {
        SupplierDto updatedSupplier = supplierService.updateSupplier(id, supplierDto);
        return ResponseEntity.ok(updatedSupplier);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateSupplierStatus(@PathVariable Long id, @RequestParam boolean active) {
        supplierService.updateSupplierStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/trigger-import")
    public ResponseEntity<String> triggerProductImport(@PathVariable Long id) {
        supplierService.triggerProductImport(id);
        return ResponseEntity.ok("Product import triggered for supplier ID: " + id);
    }
}
