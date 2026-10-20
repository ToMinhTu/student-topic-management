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

    /**
     * Fixed PUBLISHED scope for both data and count queries.
     * keywordPattern is a lowercase LIKE pattern escaped with '!'.
     */
    @Query(value = """
            SELECT new com.example.student_topic_management.dto.PublishedTopicView(
                t.id, t.roundId, t.departmentId, t.code, t.title,
                t.description, t.requirements)
            FROM Topic t
            WHERE t.status = com.example.student_topic_management.enums.TopicStatus.PUBLISHED
              AND (:roundId IS NULL OR t.roundId = :roundId)
              AND (:departmentId IS NULL OR t.departmentId = :departmentId)
              AND (:keywordPattern IS NULL
                   OR lower(t.code) LIKE :keywordPattern ESCAPE '!'
                   OR lower(t.title) LIKE :keywordPattern ESCAPE '!')
            ORDER BY t.code ASC, t.id ASC
            """,
            countQuery = """
            SELECT count(t)
            FROM Topic t
            WHERE t.status = com.example.student_topic_management.enums.TopicStatus.PUBLISHED
              AND (:roundId IS NULL OR t.roundId = :roundId)
              AND (:departmentId IS NULL OR t.departmentId = :departmentId)
              AND (:keywordPattern IS NULL
                   OR lower(t.code) LIKE :keywordPattern ESCAPE '!'
                   OR lower(t.title) LIKE :keywordPattern ESCAPE '!')
            """)
    Page<com.example.student_topic_management.dto.PublishedTopicView> searchPublishedTopics(
            @Param("keywordPattern") String keywordPattern,
            @Param("roundId") Long roundId,
            @Param("departmentId") Long departmentId,
            Pageable pageable);

    Page<Topic> findByDepartmentIdAndStatusAndDepartmentDecisionIsNull(
            Long departmentId, TopicStatus status, Pageable pageable);

    @Query("""
            SELECT t FROM Topic t
            WHERE t.status = com.example.student_topic_management.enums.TopicStatus.SUBMITTED
              AND t.departmentDecision = 'APPROVED'
              AND t.approvedBy IS NULL AND t.approvedAt IS NULL
              AND t.rejectionReason IS NULL AND t.publishedAt IS NULL
            """)
    Page<Topic> findPendingDeanReview(Pageable pageable);

    Page<Topic> findByStatusAndPublishedAtIsNull(TopicStatus status, Pageable pageable);
}
