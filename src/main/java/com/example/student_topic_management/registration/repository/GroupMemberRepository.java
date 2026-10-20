package com.example.student_topic_management.registration.repository;

import com.example.student_topic_management.registration.entity.GroupMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    // Kiểm tra sinh viên đã có nhóm active chưa
    boolean existsByStudentIdAndLeftAtIsNull(
            Long studentId
    );

    // Lấy thành viên đang tham gia nhóm
    @Query("""
           SELECT m FROM GroupMember m
           WHERE m.group.id = :groupId
           AND m.leftAt IS NULL
           """)
    List<GroupMember> findActiveMembers(
            @Param("groupId") Long groupId
    );

    // Đếm số thành viên active
    long countByGroup_IdAndLeftAtIsNull(
            Long groupId
    );

    // Kiểm tra sinh viên đã từng tham gia nhóm
    boolean existsByGroup_IdAndStudentId(
            Long groupId,
            Long studentId
    );
}