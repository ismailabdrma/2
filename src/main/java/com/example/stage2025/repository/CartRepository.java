package com.example.stage2025.repository;

import com.example.stage2025.entity.Cart;
import com.example.stage2025.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByClient(Client client);
}
