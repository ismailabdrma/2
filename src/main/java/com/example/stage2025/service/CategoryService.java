package com.example.stage2025.service;

import com.example.stage2025.dto.CategoryDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();
    Page<CategoryDto> getCategories(int page, int size, String sortBy, String sortDir, String search);
    CategoryDto getCategoryById(Long id);
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(Long id, CategoryDto categoryDto);
    void updateCategoryStatus(Long id, boolean active);
    void deleteCategory(Long id);
}
