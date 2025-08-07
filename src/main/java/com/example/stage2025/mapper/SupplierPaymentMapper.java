package com.example.stage2025.mapper;

import com.example.stage2025.dto.SupplierPaymentDto;
import com.example.stage2025.entity.SupplierPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SupplierPaymentMapper {
    SupplierPaymentMapper INSTANCE = Mappers.getMapper(SupplierPaymentMapper.class);

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    SupplierPaymentDto toDto(SupplierPayment supplierPayment);

    @Mapping(target = "supplier", ignore = true) // Supplier will be set by service
    SupplierPayment toEntity(SupplierPaymentDto supplierPaymentDto);
}
