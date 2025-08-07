package com.example.stage2025.mapper;

import com.example.stage2025.dto.ActivityLogDto;
import com.example.stage2025.entity.ActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {
    ActivityLogMapper INSTANCE = Mappers.getMapper(ActivityLogMapper.class);

    @Mapping(source = "user.username", target = "username")
    ActivityLogDto toDto(ActivityLog activityLog);

    @Mapping(target = "user", ignore = true) // User will be set by service
    ActivityLog toEntity(ActivityLogDto activityLogDto);
}
