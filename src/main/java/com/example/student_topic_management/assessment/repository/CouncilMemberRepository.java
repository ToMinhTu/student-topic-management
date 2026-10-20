// toan-D1
// toan-D1 - thêm chức năng quản lý hội đồng
package com.example.student_topic_management.assessment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.assessment.entity.CouncilMember;
import com.example.student_topic_management.assessment.entity.CouncilMemberId;

public interface CouncilMemberRepository
        extends JpaRepository<CouncilMember, CouncilMemberId> {

    List<CouncilMember> findByIdCouncilId(Long councilId);

    void deleteByIdCouncilId(Long councilId);
}