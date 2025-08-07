package com.example.stage2025.service.impl;

import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.dto.SupplierDto;
import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.entity.ApiSupplier;
import com.example.stage2025.entity.ExcelSupplier;
import com.example.stage2025.entity.SoapField;
import com.example.stage2025.entity.SoapSupplier;
import com.example.stage2025.entity.SoapSupplierOperationMeta;
import com.example.stage2025.entity.Supplier;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.SupplierMapper;
import com.example.stage2025.repository.ApiSupplierRepository;
import com.example.stage2025.repository.ExcelSupplierRepository;
import com.example.stage2025.repository.SoapSupplierOperationMetaRepository;
import com.example.stage2025.repository.SupplierRepository;
import com.example.stage2025.service.ProductImportService;
import com.example.stage2025.service.SupplierService;
import com.example.stage2025.utils.WsdlFullMetadataUtils;
import com.example.stage2025.utils.WsdlOperationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ApiSupplierRepository apiSupplierRepository;
    private final ExcelSupplierRepository excelSupplierRepository;
    private final SoapSupplierOperationMetaRepository soapSupplierOperationMetaRepository;
    private final SupplierMapper supplierMapper;

    // Use @Lazy to break circular dependency with ProductImportService
    private final ProductImportService productImportService;

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository,
                               ApiSupplierRepository apiSupplierRepository,
                               ExcelSupplierRepository excelSupplierRepository,
                               SoapSupplierOperationMetaRepository soapSupplierOperationMetaRepository,
                               SupplierMapper supplierMapper,
                               @Lazy ProductImportService productImportService) {
        this.supplierRepository = supplierRepository;
        this.apiSupplierRepository = apiSupplierRepository;
        this.excelSupplierRepository = excelSupplierRepository;
        this.soapSupplierOperationMetaRepository = soapSupplierOperationMetaRepository;
        this.supplierMapper = supplierMapper;
        this.productImportService = productImportService;
    }

    @Override
    public Page<SupplierDto> getSuppliers(int page, int size, String sortBy, String sortDir, String search, DataFormat dataFormat, Boolean active) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Supplier> suppliers = supplierRepository.findByFilters(search, dataFormat, active, pageable);
        return suppliers.map(supplierMapper::toDto);
    }

    @Override
    public SupplierDto getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional
    public SupplierDto createSupplier(SupplierDto supplierDto) {
        if (supplierRepository.findByName(supplierDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Supplier with name '" + supplierDto.getName() + "' already exists.");
        }

        Supplier supplier;
        switch (supplierDto.getDataFormat()) {
            case API:
                ApiSupplier apiSupplier = new ApiSupplier();
                apiSupplier.setApiUrl(supplierDto.getApiUrl());
                supplier = apiSupplier;
                break;
            case EXCEL:
                ExcelSupplier excelSupplier = new ExcelSupplier();
                excelSupplier.setExcelSheetName(supplierDto.getExcelSheetName());
                supplier = excelSupplier;
                break;
            case SOAP:
                SoapSupplier soapSupplier = new SoapSupplier();
                soapSupplier.setWsdlUrl(supplierDto.getWsdlUrl());
                if (supplierDto.getSoapOperationMeta() != null) {
                    SoapSupplierOperationMeta meta = new SoapSupplierOperationMeta();
                    meta.setOperationName(supplierDto.getSoapOperationMeta().getOperationName());
                    meta.setSoapAction(supplierDto.getSoapOperationMeta().getSoapAction());
                    meta.setInputElement(supplierDto.getSoapOperationMeta().getInputElement());
                    meta.setOutputElement(supplierDto.getSoapOperationMeta().getOutputElement());
                    meta.setInputFields(supplierMapper.mapSoapFieldDtosToEntity(supplierDto.getSoapOperationMeta().getInputFields()));
                    meta.setOutputFields(supplierMapper.mapSoapFieldDtosToEntity(supplierDto.getSoapOperationMeta().getOutputFields()));
                    soapSupplier.setSoapOperationMeta(soapSupplierOperationMetaRepository.save(meta));
                }
                supplier = soapSupplier;
                break;
            case CSV:
                // Assuming CsvSupplier exists and extends Supplier
                // CsvSupplier csvSupplier = new CsvSupplier();
                // csvSupplier.setCsvDelimiter(supplierDto.getCsvDelimiter());
                // supplier = csvSupplier;
                throw new UnsupportedOperationException("CSV supplier type not fully implemented yet.");
            default:
                throw new IllegalArgumentException("Unsupported data format: " + supplierDto.getDataFormat());
        }

        supplier.setName(supplierDto.getName());
        supplier.setContactEmail(supplierDto.getContactEmail());
        supplier.setContactPhone(supplierDto.getContactPhone());
        supplier.setAddress(supplierDto.getAddress());
        supplier.setPayoutFrequency(supplierDto.getPayoutFrequency());
        supplier.setActive(true); // New suppliers are active by default

        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierDto updateSupplier(Long id, SupplierDto supplierDto) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (!existingSupplier.getName().equals(supplierDto.getName()) &&
                supplierRepository.findByName(supplierDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Supplier with name '" + supplierDto.getName() + "' already exists.");
        }

        // If data format changes, we might need to delete the old supplier and create a new one
        // or handle the type conversion carefully. For simplicity, we'll assume dataFormat is immutable
        // or requires a separate process for change.
        if (existingSupplier.getDataFormat() != supplierDto.getDataFormat()) {
            throw new IllegalArgumentException("Changing data format for an existing supplier is not supported via update. Please delete and recreate.");
        }

        existingSupplier.setName(supplierDto.getName());
        existingSupplier.setContactEmail(supplierDto.getContactEmail());
        existingSupplier.setContactPhone(supplierDto.getContactPhone());
        existingSupplier.setAddress(supplierDto.getAddress());
        existingSupplier.setPayoutFrequency(supplierDto.getPayoutFrequency());
        existingSupplier.setActive(supplierDto.isActive()); // Allow updating active status

        switch (existingSupplier.getDataFormat()) {
            case API:
                ((ApiSupplier) existingSupplier).setApiUrl(supplierDto.getApiUrl());
                break;
            case EXCEL:
                ((ExcelSupplier) existingSupplier).setExcelSheetName(supplierDto.getExcelSheetName());
                break;
            case SOAP:
                SoapSupplier soapSupplier = (SoapSupplier) existingSupplier;
                soapSupplier.setWsdlUrl(supplierDto.getWsdlUrl());
                if (supplierDto.getSoapOperationMeta() != null) {
                    SoapSupplierOperationMeta meta = soapSupplier.getSoapOperationMeta();
                    if (meta == null) {
                        meta = new SoapSupplierOperationMeta();
                    }
                    meta.setOperationName(supplierDto.getSoapOperationMeta().getOperationName());
                    meta.setSoapAction(supplierDto.getSoapOperationMeta().getSoapAction());
                    meta.setInputElement(supplierDto.getSoapOperationMeta().getInputElement());
                    meta.setOutputElement(supplierDto.getSoapOperationMeta().getOutputElement());
                    meta.setInputFields(supplierMapper.mapSoapFieldDtosToEntity(supplierDto.getSoapOperationMeta().getInputFields()));
                    meta.setOutputFields(supplierMapper.mapSoapFieldDtosToEntity(supplierDto.getSoapOperationMeta().getOutputFields()));
                    soapSupplier.setSoapOperationMeta(soapSupplierOperationMetaRepository.save(meta));
                } else if (soapSupplier.getSoapOperationMeta() != null) {
                    // If meta was present but now null in DTO, delete it
                    soapSupplierOperationMetaRepository.delete(soapSupplier.getSoapOperationMeta());
                    soapSupplier.setSoapOperationMeta(null);
                }
                break;
            case CSV:
                // ((CsvSupplier) existingSupplier).setCsvDelimiter(supplierDto.getCsvDelimiter());
                throw new UnsupportedOperationException("CSV supplier type not fully implemented yet.");
        }

        return supplierMapper.toDto(supplierRepository.save(existingSupplier));
    }

    @Override
    @Transactional
    public void updateSupplierStatus(Long id, boolean active) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        supplier.setActive(active);
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        // TODO: Add logic to handle products associated with this supplier (e.g., set products inactive, or prevent deletion)
        supplierRepository.deleteById(id);
    }

    @Override
    public void triggerProductImport(Long supplierId) {
        // This method will call the ProductImportService to perform the import
        // It's separated to allow manual triggering from the admin panel
        productImportService.performImport(supplierId);
    }

    @Override
    public List<SoapOperationMeta> getWsdlOperations(String wsdlUrl) {
        try {
            return WsdlOperationUtils.getOperations(wsdlUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse WSDL operations from URL: " + wsdlUrl, e);
        }
    }

    @Override
    public SoapOperationMeta getWsdlOperationMetadata(String wsdlUrl, String operationName) {
        try {
            return WsdlFullMetadataUtils.getOperationMetadata(wsdlUrl, operationName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get WSDL operation metadata for " + operationName + " from URL: " + wsdlUrl, e);
        }
    }
}
