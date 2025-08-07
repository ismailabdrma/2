package com.example.stage2025.repository;

import com.example.stage2025.enums.DeliveryStatus;
import com.example.stage2025.enums.OrderStatus;
import com.example.stage2025.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClientIdOrderByOrderDateDesc(Long clientId);

    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:deliveryStatus IS NULL OR o.deliveryStatus = :deliveryStatus) AND " +
            "(:clientId IS NULL OR o.client.id = :clientId)")
    Page<Order> findByFilters(
            @Param("status") OrderStatus status,
            @Param("deliveryStatus") DeliveryStatus deliveryStatus,
            @Param("clientId") Long clientId,
            Pageable pageable);

    long countByStatus(OrderStatus status);
    long countByDeliveryStatus(DeliveryStatus deliveryStatus);
}
