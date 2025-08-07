package com.example.stage2025.service.impl;

import com.example.stage2025.dto.CartDto;
import com.example.stage2025.entity.Cart;
import com.example.stage2025.entity.CartItem;
import com.example.stage2025.entity.Client;
import com.example.stage2025.entity.Product;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.CartMapper;
import com.example.stage2025.repository.CartItemRepository;
import com.example.stage2025.repository.CartRepository;
import com.example.stage2025.repository.ClientRepository;
import com.example.stage2025.repository.ProductRepository;
import com.example.stage2025.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                           ClientRepository clientRepository, ProductRepository productRepository,
                           CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    public CartDto getCartByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        Cart cart = cartRepository.findByClient(client)
                .orElseGet(() -> createNewCart(client));
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartDto addProductToCart(Long clientId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is not active and cannot be added to cart.");
        }

        Cart cart = cartRepository.findByClient(client)
                .orElseGet(() -> createNewCart(client));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > product.getSyncedStock()) {
                throw new IllegalArgumentException("Cannot add more than available stock for product: " + product.getName());
            }
            item.setQuantity(newQuantity);
            item.setUnitPrice(product.getDisplayedPrice()); // Update price in case it changed
            cartItemRepository.save(item);
        } else {
            if (quantity > product.getSyncedStock()) {
                throw new IllegalArgumentException("Cannot add more than available stock for product: " + product.getName());
            }
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getDisplayedPrice())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedDate(LocalDateTime.now());
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartDto updateProductQuantityInCart(Long clientId, Long productId, int quantity) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        Cart cart = cartRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for client: " + clientId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart: " + productId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (quantity > product.getSyncedStock()) {
                throw new IllegalArgumentException("Cannot set quantity more than available stock for product: " + product.getName());
            }
            item.setQuantity(quantity);
            item.setUnitPrice(product.getDisplayedPrice()); // Update price in case it changed
            cartItemRepository.save(item);
        }

        cart.setUpdatedDate(LocalDateTime.now());
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartDto removeProductFromCart(Long clientId, Long productId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        Cart cart = cartRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for client: " + clientId));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart: " + productId));

        cart.getItems().remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        cart.setUpdatedDate(LocalDateTime.now());
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        Cart cart = cartRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for client: " + clientId));

        cartItemRepository.deleteByCart(cart); // Delete all items associated with the cart
        cart.getItems().clear(); // Clear the collection in the entity
        cart.setUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private Cart createNewCart(Client client) {
        Cart newCart = Cart.builder()
                .client(client)
                .build();
        return cartRepository.save(newCart);
    }
}
