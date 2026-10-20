// toan-D3
// toan-D3 - thêm chức năng tổng hợp kết quả
package com.example.student_topic_management.assessment.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.Result;

public interface ResultRepository
        extends JpaRepository<Result, Long> {

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT r.* FROM results r
            JOIN topic_registrations tr ON tr.id = r.registration_id
            JOIN group_members gm ON gm.group_id = tr.group_id
            WHERE gm.student_id = :studentId AND gm.left_at IS NULL
            """, nativeQuery = true)
    List<Result> findVisibleToStudent(@org.springframework.data.repository.query.Param("studentId") Long studentId);

    Optional<Result> findByRegistrationId(Long registrationId);

    boolean existsByRegistrationId(Long registrationId);
}
