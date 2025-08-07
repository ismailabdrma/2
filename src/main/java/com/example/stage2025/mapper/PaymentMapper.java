package com.example.stage2025.mapper;

import com.example.stage2025.dto.PaymentDto;
import com.example.stage2025.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(source = "order.id", target = "orderId")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "order", ignore = true) // Order will be set by service
    Payment toEntity(PaymentDto paymentDto);
}
