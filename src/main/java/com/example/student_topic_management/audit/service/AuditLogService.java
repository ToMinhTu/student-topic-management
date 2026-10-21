package com.example.student_topic_management.audit.service;

import java.sql.Timestamp;
import java.time.Clock;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.identity.service.CurrentUserService;

@Service
public class AuditLogService {

    private final JdbcTemplate jdbc;
    private final CurrentUserService currentUserService;
    private final Clock clock;

    public AuditLogService(JdbcTemplate jdbc, CurrentUserService currentUserService, Clock clock) {
        this.jdbc = jdbc;
        this.currentUserService = currentUserService;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void logCurrentUser(String action, String entityType, Long entityId, String details) {
        Long actorId = currentUserService.getCurrentUserId();
        int inserted = jdbc.update("""
                INSERT INTO audit_logs
                    (actor_id, action, entity_type, entity_id, details, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, actorId, action, entityType, entityId, details, Timestamp.from(clock.instant()));
        if (inserted != 1) {
            throw new IllegalStateException("Không thể ghi nhật ký hệ thống");
        }
    }
}
