package com.example.stage2025.mapper;

import com.example.stage2025.dto.AddressDto;
import com.example.stage2025.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    AddressDto toDto(Address address);

    @Mapping(target = "client", ignore = true) // Client will be set by service
    Address toEntity(AddressDto addressDto);
}
