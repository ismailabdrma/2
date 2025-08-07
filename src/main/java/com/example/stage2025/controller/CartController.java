package com.example.stage2025.controller;

import com.example.stage2025.dto.CartDto;
import com.example.stage2025.service.CartService;
import com.example.stage2025.utils.SecurityUtils;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('CLIENT')")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        Long clientId = SecurityUtils.getCurrentUserId();
        CartDto cart = cartService.getCartByClientId(clientId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addProductToCart(@RequestBody AddToCartRequest request) {
        Long clientId = SecurityUtils.getCurrentUserId();
        CartDto updatedCart = cartService.addProductToCart(clientId, request.getProductId(), request.getQuantity());
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<CartDto> updateCartItemQuantity(@RequestBody UpdateCartItemRequest request) {
        Long clientId = SecurityUtils.getCurrentUserId();
        CartDto updatedCart = cartService.updateProductQuantityInCart(clientId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartDto> removeProductFromCart(@PathVariable Long productId) {
        Long clientId = SecurityUtils.getCurrentUserId();
        CartDto updatedCart = cartService.removeProductFromCart(clientId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        Long clientId = SecurityUtils.getCurrentUserId();
        cartService.clearCart(clientId);
        return ResponseEntity.noContent().build();
    }

    @Data
    static class AddToCartRequest {
        private Long productId;
        @Min(1)
        private int quantity;
    }

    @Data
    static class UpdateCartItemRequest {
        private Long productId;
        @Min(0) // Allow 0 to remove item
        private int quantity;
    }
}
