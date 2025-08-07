package com.example.stage2025.service.impl;

import com.example.stage2025.dto.ProductDto;
import com.example.stage2025.entity.Category;
import com.example.stage2025.entity.Product;
import com.example.stage2025.entity.Supplier;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.ProductMapper;
import com.example.stage2025.repository.CategoryRepository;
import com.example.stage2025.repository.ProductRepository;
import com.example.stage2025.repository.SupplierRepository;
import com.example.stage2025.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    // Configure your image upload directory
    @Value("${app.upload.dir:${user.home}/uploads/products}")
    private String uploadDir;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository,
                              SupplierRepository supplierRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Page<ProductDto> getProducts(int page, int size, String sortBy, String sortDir, String search, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean active) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByFilters(search, categoryId, minPrice, maxPrice, active, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findTop5ByActiveTrueOrderByCreatedAtDesc().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> getSuggestedProducts(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return productRepository.findTop5ByCategoryIdAndIdIsNotAndActiveTrue(product.getCategory().getId(), productId).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto, MultipartFile imageFile) {
        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDto.getCategoryId()));
        Supplier supplier = supplierRepository.findById(productDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + productDto.getSupplierId()));

        Product product = productMapper.toEntity(productDto);
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setActive(true); // New products are active by default

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setImageUrls(List.of(imageUrl));
        } else if (productDto.getImageUrls() != null && !productDto.getImageUrls().isEmpty()) {
            product.setImageUrls(productDto.getImageUrls());
        } else {
            product.setImageUrls(List.of("/placeholder.svg?height=200&width=200&text=No Image")); // Default placeholder
        }

        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto, MultipartFile imageFile) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(productDto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDto.getCategoryId()));
        Supplier supplier = supplierRepository.findById(productDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + productDto.getSupplierId()));

        existingProduct.setName(productDto.getName());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setDisplayedPrice(productDto.getDisplayedPrice());
        existingProduct.setSyncedStock(productDto.getSyncedStock());
        existingProduct.setCategory(category);
        existingProduct.setSupplier(supplier);
        existingProduct.setActive(productDto.isActive()); // Allow updating active status

        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists and is not a placeholder
            if (existingProduct.getImageUrls() != null && !existingProduct.getImageUrls().isEmpty() &&
                    !existingProduct.getImageUrls().get(0).contains("placeholder.svg")) {
                deleteImage(existingProduct.getImageUrls().get(0));
            }
            String imageUrl = saveImage(imageFile);
            existingProduct.setImageUrls(List.of(imageUrl));
        } else if (productDto.getImageUrls() != null && !productDto.getImageUrls().isEmpty()) {
            existingProduct.setImageUrls(productDto.getImageUrls());
        } else {
            existingProduct.setImageUrls(List.of("/placeholder.svg?height=200&width=200&text=No Image"));
        }

        return productMapper.toDto(productRepository.save(existingProduct));
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setActive(active);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Delete associated images
        if (product.getImageUrls() != null) {
            product.getImageUrls().forEach(this::deleteImage);
        }

        productRepository.delete(product);
    }

    private String saveImage(MultipartFile imageFile) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath); // Create directory if it doesn't exist

            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + fileName; // Return a relative URL
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.contains("placeholder.svg")) {
            try {
                // Assuming imageUrl is like "/uploads/products/filename.jpg"
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir).resolve(fileName).toAbsolutePath().normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but don't throw, as it shouldn't prevent the main operation
                System.err.println("Failed to delete image file: " + imageUrl + " - " + e.getMessage());
            }
        }
    }
}
