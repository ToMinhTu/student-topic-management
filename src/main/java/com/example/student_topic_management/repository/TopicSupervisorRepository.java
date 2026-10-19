package com.example.student_topic_management.repository;

import com.example.student_topic_management.entity.TopicSupervisor;
import com.example.student_topic_management.entity.TopicSupervisorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TopicSupervisorRepository
        extends JpaRepository<TopicSupervisor, TopicSupervisorId> {

    List<TopicSupervisor> findByIdTopicIdOrderBySlotNoAsc(Long topicId);

    boolean existsByIdTopicIdAndIdLecturerId(
            Long topicId,
            Long lecturerId
    );

    boolean existsByIdTopicIdAndSlotNo(
            Long topicId,
            Byte slotNo
    );

    long countByIdTopicId(Long topicId);

    // No registration entity exists yet; query the existing table without changing module C.
    @Query(value = "SELECT COUNT(*) FROM topic_registrations WHERE topic_id = :topicId AND status = 'APPROVED'",
            nativeQuery = true)
    long countApprovedRegistrationsByTopicId(@Param("topicId") Long topicId);
}
