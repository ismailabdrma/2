package com.example.stage2025.service.impl;

import com.example.stage2025.dto.CategoryDto;
import com.example.stage2025.entity.Category;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.CategoryMapper;
import com.example.stage2025.repository.CategoryRepository;
import com.example.stage2025.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryDto> getCategories(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categories = categoryRepository.findByFilters(search, pageable);
        return categories.map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists.");
        }
        Category category = categoryMapper.toEntity(categoryDto);
        category.setActive(true); // New categories are active by default
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!existingCategory.getName().equals(categoryDto.getName()) &&
                categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Category with name '" + categoryDto.getName() + "' already exists.");
        }

        existingCategory.setName(categoryDto.getName());
        existingCategory.setDescription(categoryDto.getDescription());
        existingCategory.setActive(categoryDto.isActive()); // Allow updating active status
        return categoryMapper.toDto(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
    public void updateCategoryStatus(Long id, boolean active) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setActive(active);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        // TODO: Add logic to handle products associated with this category (e.g., set category to null, or prevent deletion)
        categoryRepository.deleteById(id);
    }
}
