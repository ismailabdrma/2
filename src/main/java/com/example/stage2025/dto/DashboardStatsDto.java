package com.example.stage2025.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDto {
    private Long totalUsers;
    private Long totalProducts;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long pendingOrders;
    private Long completedOrders;
    private Long outOfStockProducts;
    private Long activeProducts;
    private Long inactiveProducts;
}
