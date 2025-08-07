package com.example.stage2025.controller;

import com.example.stage2025.dto.ProductDto;
import com.example.stage2025.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Public endpoints
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        // For public view, only active products are returned (active = true)
        Page<ProductDto> products = productService.getProducts(page, size, sortBy, sortDir, search, categoryId, minPrice, maxPrice, true);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        // Ensure product is active for public view
        if (!product.isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/products/featured")
    public ResponseEntity<List<ProductDto>> getFeaturedProducts() {
        List<ProductDto> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{productId}/suggested")
    public ResponseEntity<List<ProductDto>> getSuggestedProducts(@PathVariable Long productId) {
        List<ProductDto> products = productService.getSuggestedProducts(productId);
        return ResponseEntity.ok(products);
    }

    // Admin endpoints
    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductDto>> getAdminProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean active) { // Admin can filter by active status
        Page<ProductDto> products = productService.getProducts(page, size, sortBy, sortDir, search, categoryId, minPrice, maxPrice, active);
        return ResponseEntity.ok(products);
    }

    @PostMapping(value = "/admin/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProduct(
            @RequestPart("product") @Valid ProductDto productDto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        ProductDto newProduct = productService.createProduct(productDto, imageFile);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @PutMapping(value = "/admin/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") @Valid ProductDto productDto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto, imageFile);
        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/admin/products/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateProductStatus(@PathVariable Long id, @RequestParam boolean active) {
        productService.updateProductStatus(id, active);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
