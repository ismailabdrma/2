package com.example.stage2025.service;

public interface EmailService {
    void sendOtpEmail(String to, String otpCode, String type);
    void sendPasswordResetEmail(String to, String resetLink);
    void sendOrderConfirmationEmail(String to, String orderDetails);
    void sendPaymentConfirmationEmail(String to, String paymentDetails);
}
