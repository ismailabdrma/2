package com.example.stage2025.mapper;

import com.example.stage2025.dto.ImportLogDto;
import com.example.stage2025.entity.ImportLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ImportLogMapper {
    ImportLogMapper INSTANCE = Mappers.getMapper(ImportLogMapper.class);

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    ImportLogDto toDto(ImportLog importLog);

    @Mapping(target = "supplier", ignore = true) // Supplier will be set by service
    ImportLog toEntity(ImportLogDto importLogDto);
}
