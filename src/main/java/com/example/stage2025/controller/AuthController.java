package com.example.stage2025.controller;

import com.example.stage2025.entity.User;
import com.example.stage2025.enums.Role;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.repository.UserRepository;
import com.example.stage2025.security.JwtUtils;
import com.example.stage2025.service.OtpService;
import com.example.stage2025.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        userService.registerUser(signupRequest.getUsername(), signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getRole());
        otpService.generateAndSendOtp(signupRequest.getEmail(), "REGISTER");
        return ResponseEntity.ok("User registered successfully! Please check your email for OTP verification.");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody OtpVerifyRequest otpVerifyRequest) {
        if (otpService.verifyOtp(otpVerifyRequest.getEmail(), otpVerifyRequest.getOtp(), "REGISTER")) {
            userService.activateUser(otpVerifyRequest.getEmail());
            return ResponseEntity.ok("Email verified successfully! You can now log in.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired OTP.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getIdentifier());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsername(loginRequest.getIdentifier());
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        }

        // If 2FA is enabled (e.g., for all logins or specific roles)
        // For this example, let's assume 2FA is always required after initial login for simplicity
        // In a real app, you'd have a flag on the user or a configuration.
        // For now, we'll always send OTP for login.
        otpService.generateAndSendOtp(user.getEmail(), "LOGIN");
        return ResponseEntity.ok("OTP_REQUIRED"); // Indicate that OTP is required
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<JwtResponse> authenticateUserWithOtp(@Valid @RequestBody OtpVerifyRequest otpVerifyRequest) {
        if (otpService.verifyOtp(otpVerifyRequest.getEmail(), otpVerifyRequest.getOtp(), "LOGIN")) {
            User user = userRepository.findByEmail(otpVerifyRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + otpVerifyRequest.getEmail()));

            // Manually authenticate the user after OTP verification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()) // Use stored password for authentication manager
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateToken(authentication);
            return ResponseEntity.ok(new JwtResponse(jwt, user.getUsername(), user.getEmail(), user.getRole().name()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or a more specific error response
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@Valid @RequestBody ResendOtpRequest resendOtpRequest) {
        String message = otpService.generateAndSendOtp(resendOtpRequest.getEmail(), resendOtpRequest.getType());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody EmailRequest emailRequest) {
        User user = userRepository.findByEmail(emailRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + emailRequest.getEmail()));

        String otpCode = otpService.generateAndSendOtp(user.getEmail(), "RESET_PASSWORD");
        // In a real application, you might send a link with the OTP embedded or a token
        // For simplicity, we're just sending the OTP directly.
        return ResponseEntity.ok("Password reset OTP sent to your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        if (otpService.verifyOtp(resetPasswordRequest.getEmail(), resetPasswordRequest.getOtp(), "RESET_PASSWORD")) {
            userService.resetPassword(resetPasswordRequest.getEmail(), resetPasswordRequest.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully.");
        }
        return ResponseEntity.badRequest().body("Invalid or expired OTP for password reset.");
    }

    @Data
    static class SignupRequest {
        private String username;
        private String email;
        private String password;
        private Role role;
    }

    @Data
    static class LoginRequest {
        private String identifier; // Can be username or email
        private String password;
    }

    @Data
    static class OtpVerifyRequest {
        private String email;
        private String otp;
    }

    @Data
    static class ResendOtpRequest {
        private String email;
        private String type; // e.g., "REGISTER", "LOGIN", "RESET_PASSWORD"
    }

    @Data
    static class EmailRequest {
        private String email;
    }

    @Data
    static class ResetPasswordRequest {
        private String email;
        private String otp;
        private String newPassword;
    }

    @Data
    static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private String username;
        private String email;
        private String role;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;

        public JwtResponse(String accessToken, String username, String email, String role) {
            this.token = accessToken;
            this.username = username;
            this.email = email;
            this.role = role;
            this.issuedAt = LocalDateTime.now();
            this.expiresAt = issuedAt.plusMinutes(jwtUtils.getJwtExpirationMs() / (1000 * 60)); // Convert ms to minutes
        }
    }

    @Data
    static class ApiResponse {
        private String message;
        private boolean success;

        public ApiResponse(String message, boolean success) {
            this.message = message;
            this.success = success;
        }
    }
}
