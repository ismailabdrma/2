package com.example.stage2025.mapper;

import com.example.stage2025.dto.ProductDto;
import com.example.stage2025.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "supplier.name", target = "supplierName")
    @Mapping(source = "supplier.id", target = "supplierId")
    ProductDto toDto(Product product);

    @Mapping(target = "category", ignore = true) // Category will be set by service
    @Mapping(target = "supplier", ignore = true) // Supplier will be set by service
    Product toEntity(ProductDto productDto);
}
