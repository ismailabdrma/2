package com.example.stage2025.utils;

import com.example.stage2025.entity.User;
import com.example.stage2025.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogUtils {

    private static ActivityLogService activityLogService;

    @Autowired
    public void setActivityLogService(ActivityLogService activityLogService) {
        LogUtils.activityLogService = activityLogService;
    }

    public static void log(User user, String actionType, String description) {
        if (activityLogService != null) {
            activityLogService.logActivity(user, actionType, description);
        } else {
            System.err.println("ActivityLogService not initialized. Log: " + actionType + " - " + description);
        }
    }
}
