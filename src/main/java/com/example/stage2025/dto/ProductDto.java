package com.example.stage2025.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal displayedPrice;
    private Integer syncedStock;
    private String categoryName;
    private Long categoryId;
    private String supplierName;
    private Long supplierId;
    private List<String> imageUrls;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
