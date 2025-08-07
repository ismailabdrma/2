package com.example.stage2025.service;

import com.example.stage2025.dto.OrderDto;
import com.example.stage2025.enums.DeliveryStatus;
import com.example.stage2025.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderDto createOrder(Long clientId, Long shippingAddressId, BigDecimal shippingFee);
    List<OrderDto> getOrdersByClientId(Long clientId);
    OrderDto getOrderById(Long orderId);
    Page<OrderDto> getAllOrders(int page, int size, String sortBy, String sortDir, OrderStatus status, DeliveryStatus deliveryStatus, Long clientId);
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);
    OrderDto updateOrderDeliveryStatus(Long orderId, DeliveryStatus newDeliveryStatus);
}
