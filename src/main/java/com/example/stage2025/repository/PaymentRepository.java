package com.example.stage2025.repository;

import com.example.stage2025.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // For Stripe/webhook/etc: Find payment by Stripe/PayPal transaction reference
    Optional<Payment> findByTransactionRef(String transactionRef);

    Optional<Payment> findByOrderId(Long orderId);

    // Total of all completed payments
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double sumTotalPayments();
}
