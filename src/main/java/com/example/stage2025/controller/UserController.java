package com.example.stage2025.controller;

import com.example.stage2025.dto.UserDto;
import com.example.stage2025.service.impl.UserServiceImpl;
import com.example.stage2025.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    @Autowired
    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateCurrentUserProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserDto updatedUser = userService.updateUserProfile(userId, request.getFirstName(), request.getLastName(), request.getPhone(), request.getUsername(), request.getEmail());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeCurrentUserPassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully!");
    }

    // Admin endpoints for user management
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto newUser = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.ok(newUser);
    }

    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        UserDto updatedUser = userService.updateUserStatus(id, active);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    static class UpdateProfileRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
    }

    @Data
    static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Size(min = 6)
        private String newPassword;
    }

    @Data
    static class CreateUserRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String email;
        @NotBlank
        @Size(min = 6)
        private String password;
        private String role; // "ADMIN" or "CLIENT"
    }
}
