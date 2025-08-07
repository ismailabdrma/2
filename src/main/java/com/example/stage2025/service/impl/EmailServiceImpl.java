package com.example.stage2025.service.impl;

import com.example.stage2025.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String to, String otpCode, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for Stage2025 - " + type);
        message.setText("Dear user,\n\nYour One-Time Password (" + type + ") is: " + otpCode + "\n\nThis OTP is valid for 5 minutes.\n\nIf you did not request this, please ignore this email.\n\nRegards,\nStage2025 Team");
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request for Stage2025");
        message.setText("Dear user,\n\nYou have requested to reset your password. Please click on the following link to reset your password: " + resetLink + "\n\nIf you did not request this, please ignore this email.\n\nRegards,\nStage2025 Team");
        mailSender.send(message);
    }

    @Override
    public void sendOrderConfirmationEmail(String to, String orderDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Stage2025 Order Confirmation");
        message.setText("Dear customer,\n\nThank you for your order! Here are your order details:\n\n" + orderDetails + "\n\nWe will notify you once your order has been shipped.\n\nRegards,\nStage2025 Team");
        mailSender.send(message);
    }

    @Override
    public void sendPaymentConfirmationEmail(String to, String paymentDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Stage2025 Payment Confirmation");
        message.setText("Dear customer,\n\nYour payment has been successfully processed! Here are your payment details:\n\n" + paymentDetails + "\n\nThank you for shopping with us!\n\nRegards,\nStage2025 Team");
        mailSender.send(message);
    }
}
