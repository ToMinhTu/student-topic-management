// toan-D2
package com.example.student_topic_management.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.Defense;

public interface DefenseRepository
        extends JpaRepository<Defense, Long> {

    List<Defense> findByRoundId(Long roundId);

    List<Defense> findByCouncilId(Long councilId);

    boolean existsByRegistrationId(Long registrationId);
}