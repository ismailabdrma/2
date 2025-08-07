package com.example.stage2025.service;

import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.dto.SupplierDto;
import com.example.stage2025.enums.DataFormat;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SupplierService {
    Page<SupplierDto> getSuppliers(int page, int size, String sortBy, String sortDir, String search, DataFormat dataFormat, Boolean active);
    SupplierDto getSupplierById(Long id);
    SupplierDto createSupplier(SupplierDto supplierDto);
    SupplierDto updateSupplier(Long id, SupplierDto supplierDto);
    void updateSupplierStatus(Long id, boolean active);
    void deleteSupplier(Long id);
    void triggerProductImport(Long supplierId);
    List<SoapOperationMeta> getWsdlOperations(String wsdlUrl);
    SoapOperationMeta getWsdlOperationMetadata(String wsdlUrl, String operationName);
}
