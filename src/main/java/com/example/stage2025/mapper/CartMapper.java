package com.example.stage2025.mapper;

import com.example.stage2025.dto.CartDto;
import com.example.stage2025.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {
    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "totalItems", expression = "java(cart.getTotalItems())")
    @Mapping(target = "totalAmount", expression = "java(cart.getTotalAmount())")
    CartDto toDto(Cart cart);

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "items", ignore = true) // Items are managed separately
    Cart toEntity(CartDto cartDto);
}
