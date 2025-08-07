package com.example.stage2025.service.impl;

import com.example.stage2025.dto.PaymentDto;
import com.example.stage2025.entity.Order;
import com.example.stage2025.entity.Payment;
import com.example.stage2025.enums.OrderStatus;
import com.example.stage2025.enums.PaymentStatus;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.PaymentMapper;
import com.example.stage2025.repository.OrderRepository;
import com.example.stage2025.repository.PaymentRepository;
import com.example.stage2025.service.EmailService;
import com.example.stage2025.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;

    @Autowired
    public PaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.emailService = emailService;
    }

    @Override
    public PaymentDto createPayment(PaymentDto dto) {
        Payment payment = Payment.builder()
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .transactionRef(dto.getTransactionRef())
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDateTime.now())
                .status(PaymentStatus.PENDING)
                .build();

        // Attach order if provided
        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + dto.getOrderId()));
            payment.setOrder(order);
        }

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé : ID = " + id));
        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentDto updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé : ID = " + id));
        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    public void updatePaymentStatusByTransactionRef(String transactionRef, String status) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ref: " + transactionRef));
        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        paymentRepository.save(payment);

        // Optionally, update the order to have payment and mark as paid
        if (payment.getOrder() != null && status.equalsIgnoreCase("PAID")) {
            Order order = payment.getOrder();
            order.setPayment(payment); // If you use @OneToOne in Order
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }
    }

    @Override
    public Session createCheckoutSession(Long orderId) {
        Stripe.apiKey = stripeApiKey;

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total amount must be greater than zero.");
        }

        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/orders/" + orderId + "?payment=success")
                    .setCancelUrl(frontendUrl + "/orders/" + orderId + "?payment=cancelled")
                    .setClientReferenceId(orderId.toString()); // Link Stripe session to our order ID

            // Add line items for each product in the order
            order.getOrderItems().forEach(item ->
                    paramsBuilder.addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity((long) item.getQuantity())
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd") // Or your desired currency
                                                    .setUnitAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(100)).longValue()) // Amount in cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(item.getProduct().getName())
                                                                    .addImages(item.getProduct().getImageUrls().isEmpty() ?
                                                                            List.of("https://via.placeholder.com/150") : item.getProduct().getImageUrls())
                                                                    .build())
                                                    .build())
                                    .build()));

            // Add shipping fee as a separate line item if applicable
            if (order.getShippingFee() != null && order.getShippingFee().compareTo(BigDecimal.ZERO) > 0) {
                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(order.getShippingFee().multiply(BigDecimal.valueOf(100)).longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Shipping Fee")
                                                                .build())
                                                .build())
                                .build());
            }

            return Session.create(paramsBuilder.build());
        } catch (StripeException e) {
            throw new RuntimeException("Error creating Stripe checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String sigHeader) {
        Stripe.apiKey = stripeApiKey;
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            throw new IllegalArgumentException("Invalid Stripe webhook signature", e);
        }

        // Deserialize the nested object inside the event
        StripeObject stripeObject = null;
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the `EventDataObjectDeserializer` documentation for more information.
            throw new IllegalStateException("Stripe webhook data deserialization failed.");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                handleCheckoutSessionCompleted(session);
                break;
            case "payment_intent.succeeded":
                // PaymentIntent is often used for direct charges, not typically for Checkout Sessions
                // unless you're handling post-checkout actions.
                // For Checkout Sessions, `checkout.session.completed` is usually sufficient.
                break;
            case "payment_intent.payment_failed":
                // Handle failed payments
                break;
            // ... handle other event types
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    }

    @Transactional
    private void handleCheckoutSessionCompleted(Session session) {
        Long orderId = Long.parseLong(session.getClientReferenceId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for Stripe session: " + orderId));

        // Check if a payment already exists for this order to prevent duplicates
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);

        if (existingPayment.isPresent() && existingPayment.get().getStatus() == PaymentStatus.COMPLETED) {
            System.out.println("Payment for order " + orderId + " already processed. Skipping.");
            return; // Already processed
        }

        Payment payment = existingPayment.orElseGet(Payment::new);

        payment.setOrder(order);
        payment.setAmount(BigDecimal.valueOf(session.getAmountTotal() / 100.0)); // Convert cents to dollars
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(session.getPaymentIntent()); // Store PaymentIntent ID
        payment.setPaymentMethod("Stripe"); // Or derive from session.getPaymentMethodTypes()

        paymentRepository.save(payment);

        // Update order status
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setStatus(OrderStatus.PROCESSING); // Move order to processing
        orderRepository.save(order);

        // Send confirmation email
        String emailContent = String.format("Order #%d has been paid. Total: %.2f USD. Transaction ID: %s",
                order.getId(), payment.getAmount(), payment.getTransactionId());
        emailService.sendPaymentConfirmationEmail(order.getClient().getEmail(), emailContent);
        emailService.sendOrderConfirmationEmail(order.getClient().getEmail(), emailContent); // Re-send order confirmation
    }

    @Override
    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));
        return paymentMapper.toDto(payment);
    }
}
