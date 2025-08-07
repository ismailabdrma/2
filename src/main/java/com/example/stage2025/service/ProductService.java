package com.example.stage2025.service;

import com.example.stage2025.dto.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<ProductDto> getProducts(int page, int size, String sortBy, String sortDir, String search, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean active);
    ProductDto getProductById(Long id);
    List<ProductDto> getFeaturedProducts();
    List<ProductDto> getSuggestedProducts(Long productId);
    ProductDto createProduct(ProductDto productDto, MultipartFile imageFile);
    ProductDto updateProduct(Long id, ProductDto productDto, MultipartFile imageFile);
    void updateProductStatus(Long id, boolean active);
    void deleteProduct(Long id);
}
