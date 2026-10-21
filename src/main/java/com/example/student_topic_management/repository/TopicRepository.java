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

    @Query("SELECT (count(t) > 0) FROM Topic t WHERE t.roundId = :roundId AND upper(trim(t.code)) = :code")
    boolean existsByRoundIdAndCode(
            Long roundId,
            String code
    );

    @Query("SELECT (count(t) > 0) FROM Topic t WHERE t.roundId = :roundId AND upper(trim(t.code)) = :code AND t.id <> :id")
    boolean existsByRoundIdAndCodeAndIdNot(
            Long roundId,
            String code,
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Topic t WHERE t.id = :id")
    Optional<Topic> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT new com.example.student_topic_management.dto.PublishedTopicView(t.id, t.roundId, t.departmentId, t.code, t.title, t.description, t.requirements) FROM Topic t WHERE t.status = com.example.student_topic_management.enums.TopicStatus.PUBLISHED AND (:keyword IS NULL OR lower(t.title) LIKE lower(concat('%', :keyword, '%'))) AND (:departmentId IS NULL OR t.departmentId = :departmentId) AND (:supervisorId IS NULL OR :supervisorId IS NOT NULL)")
    Page<com.example.student_topic_management.dto.PublishedTopicView> searchPublishedTopics(@Param("keyword") String keyword, @Param("departmentId") Long departmentId, @Param("supervisorId") Long supervisorId, Pageable pageable);

    Page<Topic> findByDepartmentIdAndStatusAndDepartmentDecisionIsNull(Long departmentId, TopicStatus status, Pageable pageable);

    @Query("SELECT t FROM Topic t WHERE t.status = com.example.student_topic_management.enums.TopicStatus.SUBMITTED")
    Page<Topic> findPendingDeanReview(Pageable pageable);

    Page<Topic> findByStatusAndPublishedAtIsNull(TopicStatus status, Pageable pageable);
}
