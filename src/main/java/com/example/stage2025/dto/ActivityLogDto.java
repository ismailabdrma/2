package com.example.stage2025.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogDto {
    private Long id;
    private LocalDateTime timestamp;
    private String username;
    private String actionType;
    private String description;
}
