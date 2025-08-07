package com.example.stage2025.service.impl;

import com.example.stage2025.entity.OtpToken;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.repository.OtpTokenRepository;
import com.example.stage2025.service.EmailService;
import com.example.stage2025.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    @Autowired
    public OtpServiceImpl(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public String generateAndSendOtp(String email, String type) {
        // Invalidate any existing unused OTPs for this email and type
        otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByExpiryDateDesc(email, type)
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpTokenRepository.save(otp);
                });

        String otpCode = generateOtpCode();
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otpCode)
                .expiryDate(LocalDateTime.now().plusMinutes(5)) // OTP valid for 5 minutes
                .type(type)
                .used(false)
                .build();
        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(email, otpCode, type);
        return "OTP sent to your email.";
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode, String type) {
        OtpToken otpToken = otpTokenRepository.findByEmailAndOtpCodeAndTypeAndUsedFalse(email, otpCode, type)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid OTP or OTP already used."));

        if (otpToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            otpToken.setUsed(true); // Mark as used even if expired
            otpTokenRepository.save(otpToken);
            throw new IllegalArgumentException("OTP has expired.");
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        return true;
    }

    @Override
    @Transactional
    public void invalidateOtp(String email, String otpCode, String type) {
        otpTokenRepository.findByEmailAndOtpCodeAndTypeAndUsedFalse(email, otpCode, type)
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpTokenRepository.save(otp);
                });
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates a 6-digit OTP
        return String.valueOf(otp);
    }
}
