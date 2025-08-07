package com.example.stage2025.repository;

import com.example.stage2025.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmailAndOtpCodeAndTypeAndUsedFalse(String email, String otpCode, String type);
    Optional<OtpToken> findTopByEmailAndTypeAndUsedFalseOrderByExpiryDateDesc(String email, String type);
    Optional<OtpToken> findTopByEmailAndTypeOrderByExpiresAtDesc(String email, String type);
    void deleteByEmail(String email); // optional, to clear used tokens
}
