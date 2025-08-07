package com.example.stage2025.service.impl;

import com.example.stage2025.dto.OrderDto;
import com.example.stage2025.enums.DeliveryStatus;
import com.example.stage2025.enums.OrderStatus;
import com.example.stage2025.entity.Address;
import com.example.stage2025.entity.Cart;
import com.example.stage2025.entity.CartItem;
import com.example.stage2025.entity.Client;
import com.example.stage2025.entity.Order;
import com.example.stage2025.entity.OrderItem;
import com.example.stage2025.entity.Product;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.OrderMapper;
import com.example.stage2025.repository.AddressRepository;
import com.example.stage2025.repository.CartRepository;
import com.example.stage2025.repository.ClientRepository;
import com.example.stage2025.repository.OrderRepository;
import com.example.stage2025.repository.ProductRepository;
import com.example.stage2025.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ClientRepository clientRepository,
                            CartRepository cartRepository, ProductRepository productRepository,
                            AddressRepository addressRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderDto createOrder(Long clientId, Long shippingAddressId, BigDecimal shippingFee) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        Cart cart = cartRepository.findByClient(client)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for client: " + clientId));
        Address shippingAddress = addressRepository.findById(shippingAddressId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with id: " + shippingAddressId));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order from an empty cart.");
        }

        // Validate stock and deduct from product stock
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getSyncedStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }
            product.setSyncedStock(product.getSyncedStock() - cartItem.getQuantity());
            productRepository.save(product); // Update product stock
        }

        Order order = Order.builder()
                .client(client)
                .shippingAddress(shippingAddress)
                .shippingFee(shippingFee)
                .totalAmount(cart.getTotalAmount().add(shippingFee))
                .status(OrderStatus.PENDING)
                .deliveryStatus(DeliveryStatus.PENDING)
                .paymentStatus(com.example.stage2025.enums.PaymentStatus.PENDING) // Initial payment status
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(savedOrder)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getUnitPrice())
                        .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        savedOrder.setOrderItems(orderItems); // Associate order items with the order
        orderRepository.save(savedOrder); // Save again to persist order items

        // Clear the cart after order creation
        cart.getItems().clear();
        cartRepository.save(cart); // Save the cleared cart

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public List<OrderDto> getOrdersByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return orderRepository.findByClientIdOrderByOrderDateDesc(clientId).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toDto(order);
    }

    @Override
    public Page<OrderDto> getAllOrders(int page, int size, String sortBy, String sortDir, OrderStatus status, DeliveryStatus deliveryStatus, Long clientId) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepository.findByFilters(status, deliveryStatus, clientId, pageable);
        return orders.map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(newStatus);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto updateOrderDeliveryStatus(Long orderId, DeliveryStatus newDeliveryStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setDeliveryStatus(newDeliveryStatus);
        return orderMapper.toDto(orderRepository.save(order));
    }
}
