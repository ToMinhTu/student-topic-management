package com.example.student_topic_management.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.student_topic_management.audit.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query(value = "SELECT a FROM AuditLog a LEFT JOIN FETCH a.actor ORDER BY a.createdAt DESC",
            countQuery = "SELECT COUNT(a) FROM AuditLog a")
    Page<AuditLog> findAllWithActor(Pageable pageable);
}
