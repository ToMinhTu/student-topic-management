// toan-D3
// toan-D3 - thêm chức năng nhập điểm
package com.example.student_topic_management.assessment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.Evaluation;

public interface EvaluationRepository
        extends JpaRepository<Evaluation, Long> {

    Optional<Evaluation> findByAssignmentId(Long assignmentId);

    boolean existsByAssignmentId(Long assignmentId);
}