package com.example.stage2025.mapper;

import com.example.stage2025.dto.OrderDto;
import com.example.stage2025.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, AddressMapper.class})
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    OrderDto toDto(Order order);

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "payment", ignore = true)
    Order toEntity(OrderDto orderDto);
}
