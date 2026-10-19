package com.example.student_topic_management.service;

import com.example.student_topic_management.entity.Topic;
import java.sql.Timestamp;
import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicAuditService {
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public TopicAuditService(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(Topic topic, Long actorId, String action) {
        write(topic, actorId, action, "status=" + topic.getStatus() + "; code=" + topic.getCode()
                + "; roundId=" + topic.getRoundId() + "; departmentId=" + topic.getDepartmentId());
    }

    // Keep the old review before clearing it; chunks fit MySQL TEXT even with UTF-8.
    @Transactional(propagation = Propagation.MANDATORY)
    public void snapshotReview(Topic topic, Long actorId) {
        snapshot(topic, actorId, "departmentDecision", topic.getDepartmentDecision());
        snapshot(topic, actorId, "departmentReviewedBy", topic.getDepartmentReviewedBy());
        snapshot(topic, actorId, "departmentReviewedAt", topic.getDepartmentReviewedAt());
        snapshot(topic, actorId, "departmentComment", topic.getDepartmentComment());
        snapshot(topic, actorId, "approvedBy", topic.getApprovedBy());
        snapshot(topic, actorId, "approvedAt", topic.getApprovedAt());
        snapshot(topic, actorId, "rejectionReason", topic.getRejectionReason());
        snapshot(topic, actorId, "publishedAt", topic.getPublishedAt());
    }

    private void snapshot(Topic topic, Long actorId, String field, Object value) {
        if (value == null) return;
        String text = value.toString();
        int offset = 0;
        int part = 1;
        do {
            int end = Math.min(offset + 12000, text.length());
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) end--;
            write(topic, actorId, "TOPIC_REVIEW_SNAPSHOT",
                    field + " part=" + part++ + "\n" + text.substring(offset, end));
            offset = end;
        } while (offset < text.length());
    }

    private void write(Topic topic, Long actorId, String action, String details) {
        jdbc.update("INSERT INTO audit_logs(actor_id,action,entity_type,entity_id,details,created_at) "
                        + "VALUES (?,?,'TOPIC',?,?,?)",
                actorId, action, topic.getId(), details, Timestamp.from(clock.instant()));
    }
}