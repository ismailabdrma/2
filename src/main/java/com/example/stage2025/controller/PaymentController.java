package com.example.stage2025.controller;

import com.example.stage2025.dto.PaymentDto;
import com.example.stage2025.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles all payment operations for client orders (e.g., Stripe, CMI, PayPal).
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 1. Initiate a new payment (client only)
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentDto> create(@RequestBody PaymentDto dto) {
        return ResponseEntity.ok(paymentService.createPayment(dto));
    }

    // 2. Get payment details by ID (client/admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<PaymentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    // 3. List all payments (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentDto>> listAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // 4. Update payment status (admin only)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    // 5. Stripe Webhook Endpoint (public, NO authentication)
    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader,
            @Value("${stripe.webhook.secret}") String stripeWebhookSecret
    ) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid Stripe signature");
        }

        // Handle payment_intent.succeeded event (mark payment as PAID)
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
            String transactionRef = intent.getId(); // Stripe PaymentIntent ID
            paymentService.updatePaymentStatusByTransactionRef(transactionRef, "PAID");
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
            String transactionRef = intent.getId();
            paymentService.updatePaymentStatusByTransactionRef(transactionRef, "FAILED");
        }

        // (Optionally handle other Stripe events here...)

        return ResponseEntity.ok("Stripe webhook received");
    }

    // New endpoint to create a checkout session
    @PostMapping("/create-checkout-session")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(@RequestBody CreateCheckoutSessionRequest request) {
        Session session = paymentService.createCheckoutSession(request.getOrderId());
        return ResponseEntity.ok(new CheckoutSessionResponse(session.getUrl()));
    }

    // New endpoint to handle Stripe webhook
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return new ResponseEntity<>("Webhook received", HttpStatus.OK);
    }

    // New endpoint to get payment details by order ID
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable Long orderId) {
        // TODO: Add security check to ensure client can only view their own order's payment
        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    @Data
    static class CreateCheckoutSessionRequest {
        private Long orderId;
    }

    @Data
    static class CheckoutSessionResponse {
        private String url;

        public CheckoutSessionResponse(String url) {
            this.url = url;
        }
    }
}
