package com.example.student_topic_management.repository;

import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    Page<Topic> findByRoundId(Long roundId, Pageable pageable);

    Page<Topic> findByProposedBy(
            Long proposedBy,
            Pageable pageable
    );

    Page<Topic> findByRoundIdAndStatus(
            Long roundId,
            TopicStatus status,
            Pageable pageable
    );

    Optional<Topic> findByIdAndProposedBy(
            Long id,
            Long proposedBy
    );

    boolean existsByRoundIdAndCode(
            Long roundId,
            String code
    );

    boolean existsByRoundIdAndCodeAndIdNot(
            Long roundId,
            String code,
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Topic t WHERE t.id = :id")
    Optional<Topic> findByIdForUpdate(@Param("id") Long id);
}
