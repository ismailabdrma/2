package com.example.stage2025.service;

import com.example.stage2025.dto.ActivityLogDto;
import com.example.stage2025.entity.User;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ActivityLogService {
    void logActivity(User user, String actionType, String description);
    Page<ActivityLogDto> getAllActivityLogs(int page, int size, String sortBy, String sortDir, String username, String actionType, LocalDate startDate, LocalDate endDate);
}
