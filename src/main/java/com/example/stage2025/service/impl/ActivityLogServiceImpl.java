package com.example.stage2025.service.impl;

import com.example.stage2025.dto.ActivityLogDto;
import com.example.stage2025.entity.ActivityLog;
import com.example.stage2025.entity.User;
import com.example.stage2025.mapper.ActivityLogMapper;
import com.example.stage2025.repository.ActivityLogRepository;
import com.example.stage2025.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @Autowired
    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository, ActivityLogMapper activityLogMapper) {
        this.activityLogRepository = activityLogRepository;
        this.activityLogMapper = activityLogMapper;
    }

    @Override
    public void logActivity(User user, String actionType, String description) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .username(user.getUsername()) // Store username directly for easier querying
                .actionType(actionType)
                .description(description)
                .build();
        activityLogRepository.save(log);
    }

    @Override
    public Page<ActivityLogDto> getAllActivityLogs(int page, int size, String sortBy, String sortDir, String username, String actionType, LocalDate startDate, LocalDate endDate) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<ActivityLog> logs = activityLogRepository.findByFilters(username, actionType, startDateTime, endDateTime, pageable);
        return logs.map(activityLogMapper::toDto);
    }
}
