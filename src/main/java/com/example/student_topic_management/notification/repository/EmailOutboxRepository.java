package com.example.student_topic_management.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.student_topic_management.notification.entity.EmailOutbox;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

    @Query(value = """
            SELECT * FROM email_outbox
            WHERE status = 'PENDING'
              AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
            ORDER BY created_at, id
            LIMIT 1 FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<EmailOutbox> claimOneJob(@Param("now") LocalDateTime now);

    @Query(value = """
            SELECT * FROM email_outbox
            WHERE status = 'PROCESSING' AND locked_at < :threshold
            ORDER BY locked_at, id
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<EmailOutbox> findStuckJobs(@Param("threshold") LocalDateTime threshold);

    @Query(value = "SELECT e FROM EmailOutbox e ORDER BY e.createdAt DESC",
            countQuery = "SELECT COUNT(e) FROM EmailOutbox e")
    Page<EmailOutbox> findAllJobs(Pageable pageable);
}
