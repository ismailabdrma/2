package com.example.stage2025.dto;

import com.example.stage2025.enums.DeliveryStatus;
import com.example.stage2025.enums.OrderStatus;
import com.example.stage2025.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long clientId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private OrderStatus status;
    private DeliveryStatus deliveryStatus;
    private PaymentStatus paymentStatus;
    private List<OrderItemDto> orderItems;
    private AddressDto shippingAddress;
}
