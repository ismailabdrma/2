package com.example.stage2025.service.impl;

import com.example.stage2025.dto.SupplierProductDto;
import com.example.stage2025.entity.*;
import com.example.stage2025.enums.ImportStatus;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.repository.CategoryRepository;
import com.example.stage2025.repository.ProductRepository;
import com.example.stage2025.repository.SupplierRepository;
import com.example.stage2025.service.ImportLogService;
import com.example.stage2025.service.ProductFetcher;
import com.example.stage2025.service.ProductFetcherFactory;
import com.example.stage2025.utils.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductFetcherFactory fetcherFactory;
    private final ImportLogService importLogService;

    @Autowired
    public ProductImportService(SupplierRepository supplierRepository,
                                ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                ProductFetcherFactory productFetcherFactory,
                                ImportLogService importLogService) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.fetcherFactory = productFetcherFactory;
        this.importLogService = importLogService;
    }

    // Scheduled task to run daily at a specific time (e.g., 2 AM)
    // You can configure the cron expression as needed
    @Scheduled(cron = "0 0 2 * * ?") // Runs every day at 2:00 AM
    @Transactional
    public void scheduledProductImport() {
        log.info("Starting scheduled product import for all active suppliers.");
        List<Supplier> activeSuppliers = supplierRepository.findByActiveTrue();
        for (Supplier supplier : activeSuppliers) {
            performImport(supplier.getId());
        }
        log.info("Finished scheduled product import.");
    }

    @Transactional
    public void performImport(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));

        log.info("Starting import for supplier: {}", supplier.getName());
        int productsProcessed = 0;
        int errors = 0;
        StringBuilder errorMessage = new StringBuilder();
        ImportStatus importStatus = ImportStatus.SUCCESS;

        try {
            ProductFetcher fetcher = fetcherFactory.getFetcher(supplier.getDataFormat());
            List<SupplierProductDto> fetchedProducts;

            // Prepare parameters based on supplier type
            Map<String, String> fetchParams = switch (supplier.getDataFormat()) {
                case API -> Map.of("apiUrl", supplier.getApiUrl());
                case EXCEL -> Map.of("excelSheetName", supplier.getExcelSheetName());
                case CSV -> Map.of("csvDelimiter", supplier.getCsvDelimiter());
                case SOAP -> {
                    if (supplier instanceof com.example.stage2025.entity.SoapSupplier soapSupplier && soapSupplier.getSoapOperationMeta() != null) {
                        yield Map.of(
                                "wsdlUrl", soapSupplier.getWsdlUrl(),
                                "operationName", soapSupplier.getSoapOperationMeta().getOperationName(),
                                "inputElement", soapSupplier.getSoapOperationMeta().getInputElement(),
                                "outputElement", soapSupplier.getSoapOperationMeta().getOutputElement(),
                                "soapAction", Optional.ofNullable(soapSupplier.getSoapOperationMeta().getSoapAction()).orElse(""),
                                "inputFields", "TODO: Implement inputFields mapping"
                                // Add dynamic input values if needed for SOAP
                        );
                    } else {
                        throw new IllegalArgumentException("SOAP supplier missing WSDL URL or operation metadata.");
                    }
                }
            };

            fetchedProducts = fetcher.fetchProducts(
                    supplier.getDataFormat() == com.example.stage2025.enums.DataFormat.SOAP ? supplier.getWsdlUrl() : supplier.getApiUrl(), // Pass WSDL URL for SOAP, API URL for others
                    fetchParams
            );

            for (SupplierProductDto spDto : fetchedProducts) {
                try {
                    // Find or create category
                    Category category = categoryRepository.findByName(spDto.getCategoryName())
                            .orElseGet(() -> categoryRepository.save(Category.builder()
                                    .name(spDto.getCategoryName())
                                    .description("Auto-created category for " + spDto.getCategoryName())
                                    .active(true)
                                    .build()));

                    // Find existing product by externalProductId and supplier, or create new
                    Optional<Product> existingProduct = productRepository.findAll().stream() // Consider adding findByExternalProductIdAndSupplier
                            .filter(p -> p.getExternalProductId() != null && p.getExternalProductId().equals(spDto.getExternalProductId()) &&
                                    p.getSupplier().getId().equals(supplier.getId()))
                            .findFirst();

                    Product product;
                    if (existingProduct.isPresent()) {
                        product = existingProduct.get();
                        product.setName(spDto.getName());
                        product.setDescription(spDto.getDescription());
                        product.setDisplayedPrice(spDto.getPrice());
                        product.setSyncedStock(spDto.getStock());
                        product.setCategory(category);
                        product.setImageUrls(spDto.getImageUrls());
                        product.setUpdatedAt(LocalDateTime.now());
                        log.debug("Updating product: {}", product.getName());
                    } else {
                        product = Product.builder()
                                .name(spDto.getName())
                                .description(spDto.getDescription())
                                .displayedPrice(spDto.getPrice())
                                .syncedStock(spDto.getStock())
                                .category(category)
                                .supplier(supplier)
                                .imageUrls(spDto.getImageUrls())
                                .active(true) // New products are active by default
                                .externalProductId(spDto.getExternalProductId())
                                .build();
                        log.debug("Creating new product: {}", product.getName());
                    }
                    productRepository.save(product);
                    productsProcessed++;
                } catch (Exception e) {
                    errors++;
                    errorMessage.append("Error processing product '").append(spDto.getName()).append("': ").append(e.getMessage()).append("; ");
                    log.error("Error processing product from supplier {}: {}", supplier.getName(), e.getMessage(), e);
                }
            }

            if (errors > 0) {
                importStatus = productsProcessed > 0 ? ImportStatus.PARTIAL : ImportStatus.FAILED;
            }
            supplier.setLastImport(LocalDateTime.now());
            supplierRepository.save(supplier);

        } catch (Exception e) {
            errors = fetchedProducts != null ? fetchedProducts.size() : 0; // All products failed if fetch itself failed
            importStatus = ImportStatus.FAILED;
            errorMessage.append("Failed to fetch products from supplier: ").append(e.getMessage());
            log.error("Critical error during import for supplier {}: {}", supplier.getName(), e.getMessage(), e);
        } finally {
            importLogService.logImport(supplier.getId(), importStatus, productsProcessed, errors, errorMessage.toString());
            log.info("Import for supplier {} finished with status: {}, processed: {}, errors: {}",
                    supplier.getName(), importStatus, productsProcessed, errors);
        }
    }
}
