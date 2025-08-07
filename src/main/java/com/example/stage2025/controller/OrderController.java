package com.example.stage2025.controller;

import com.example.stage2025.dto.OrderDto;
import com.example.stage2025.enums.DeliveryStatus;
import com.example.stage2025.enums.OrderStatus;
import com.example.stage2025.service.OrderService;
import com.example.stage2025.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Client endpoints
    @PostMapping("/orders/create")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long clientId = SecurityUtils.getCurrentUserId();
        OrderDto newOrder = orderService.createOrder(clientId, request.getAddressId(), request.getShippingFee());
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<OrderDto>> getClientOrders() {
        Long clientId = SecurityUtils.getCurrentUserId();
        List<OrderDto> orders = orderService.getOrdersByClientId(clientId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        // Admin can view any order, client can only view their own
        OrderDto order = orderService.getOrderById(id);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (SecurityUtils.isAdmin() || order.getClientId().equals(currentUserId)) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Admin endpoints
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) DeliveryStatus deliveryStatus,
            @RequestParam(required = false) Long clientId) {
        Page<OrderDto> orders = orderService.getAllOrders(page, size, sortBy, sortDir, status, deliveryStatus, clientId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/admin/orders/{id}/shipping-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderDeliveryStatus(@PathVariable Long id, @RequestParam DeliveryStatus shippingStatus) {
        OrderDto updatedOrder = orderService.updateOrderDeliveryStatus(id, shippingStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @Data
    static class CreateOrderRequest {
        private Long addressId;
        @Min(0)
        private BigDecimal shippingFee;
    }
}
