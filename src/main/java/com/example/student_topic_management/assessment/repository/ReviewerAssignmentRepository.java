// toan-D3
package com.example.student_topic_management.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.ReviewerAssignment;

public interface ReviewerAssignmentRepository
        extends JpaRepository<ReviewerAssignment, Long> {

    List<ReviewerAssignment> findByRegistrationId(Long registrationId);

    List<ReviewerAssignment> findByLecturerId(Long lecturerId);

    boolean existsByRegistrationIdAndLecturerId(
            Long registrationId,
            Long lecturerId);
}