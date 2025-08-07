package com.example.stage2025.service;

import com.example.stage2025.entity.OtpToken;

public interface OtpService {
    String generateAndSendOtp(String email, String type);
    boolean verifyOtp(String email, String otpCode, String type);
    void invalidateOtp(String email, String otpCode, String type);
}
