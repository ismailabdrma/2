package com.example.stage2025.service;

import com.example.stage2025.dto.PaymentDto;
import com.stripe.model.checkout.Session;

public interface PaymentService {
    Session createCheckoutSession(Long orderId);
    void handleStripeWebhook(String payload, String sigHeader);
    PaymentDto getPaymentByOrderId(Long orderId);
}
