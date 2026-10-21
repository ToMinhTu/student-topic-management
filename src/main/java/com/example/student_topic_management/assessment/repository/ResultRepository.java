// toan-D3
// toan-D3 - thêm chức năng tổng hợp kết quả
package com.example.student_topic_management.assessment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.Result;

public interface ResultRepository
        extends JpaRepository<Result, Long> {

    Optional<Result> findByRegistrationId(Long registrationId);

    boolean existsByRegistrationId(Long registrationId);
}
