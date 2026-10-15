// toan-D1
package com.example.student_topic_management.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.Council;
import com.example.student_topic_management.assessment.entity.CouncilStatus;

public interface CouncilRepository extends JpaRepository<Council, Long> {

    List<Council> findByRoundId(Long roundId);

    List<Council> findByStatus(CouncilStatus status);

    List<Council> findByRoundIdAndStatus(
            Long roundId,
            CouncilStatus status);
}