package com.example.stage2025.mapper;

import com.example.stage2025.dto.CartItemDto;
import com.example.stage2025.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.imageUrls[0]", target = "imageUrl")
    CartItemDto toDto(CartItem cartItem);

    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    CartItem toEntity(CartItemDto cartItemDto);
}
