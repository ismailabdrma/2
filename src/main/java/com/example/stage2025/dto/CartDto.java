package com.example.stage2025.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private Long clientId;
    private List<CartItemDto> items;
    private int totalItems;
    private BigDecimal totalAmount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
