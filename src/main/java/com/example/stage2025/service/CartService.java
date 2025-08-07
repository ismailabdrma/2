package com.example.stage2025.service;

import com.example.stage2025.dto.CartDto;
import com.example.stage2025.dto.CartItemDto;

public interface CartService {
    CartDto getCartByClientId(Long clientId);
    CartDto addProductToCart(Long clientId, Long productId, int quantity);
    CartDto updateProductQuantityInCart(Long clientId, Long productId, int quantity);
    CartDto removeProductFromCart(Long clientId, Long productId);
    void clearCart(Long clientId);
}
