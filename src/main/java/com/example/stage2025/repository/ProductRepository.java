package com.example.stage2025.repository;

import com.example.stage2025.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTop5ByActiveTrueOrderByCreatedAtDesc(); // For featured products
    List<Product> findTop5ByCategoryIdAndIdIsNotAndActiveTrue(Long categoryId, Long productId); // For suggested products

    @Query("SELECT p FROM Product p WHERE " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR p.displayedPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.displayedPrice <= :maxPrice) AND " +
            "(:active IS NULL OR p.active = :active)")
    Page<Product> findByFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("active") Boolean active, // Nullable for public view, true/false for admin
            Pageable pageable);

    long countBySyncedStock(Integer syncedStock);
    long countByActive(boolean active);
}
