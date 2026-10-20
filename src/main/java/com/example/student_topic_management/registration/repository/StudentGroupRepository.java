package com.example.student_topic_management.registration.repository;

import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.entity.GroupStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface StudentGroupRepository
        extends JpaRepository<StudentGroup, Long> {

    // Tìm danh sách nhóm của một đợt
    List<StudentGroup> findByRoundId(Long roundId);

    // Tìm nhóm theo đợt và trạng thái
    List<StudentGroup> findByRoundIdAndStatus(
            Long roundId,
            GroupStatus status
    );

    // Khóa nhóm khi cập nhật để tránh xung đột
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM StudentGroup g WHERE g.id = :id")
    Optional<StudentGroup> findGroupForUpdate(
            @Param("id") Long id
    );
}