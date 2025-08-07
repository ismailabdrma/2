package com.example.stage2025.repository;

import com.example.stage2025.entity.Cart;
import com.example.stage2025.entity.CartItem;
import com.example.stage2025.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCart(Cart cart);
}
