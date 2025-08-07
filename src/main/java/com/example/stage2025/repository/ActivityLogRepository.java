package com.example.stage2025.repository;

import com.example.stage2025.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT al FROM ActivityLog al WHERE " +
            "(:username IS NULL OR LOWER(al.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:actionType IS NULL OR LOWER(al.actionType) LIKE LOWER(CONCAT('%', :actionType, '%'))) AND " +
            "(:startDate IS NULL OR al.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR al.timestamp <= :endDate)")
    Page<ActivityLog> findByFilters(
            @Param("username") String username,
            @Param("actionType") String actionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
