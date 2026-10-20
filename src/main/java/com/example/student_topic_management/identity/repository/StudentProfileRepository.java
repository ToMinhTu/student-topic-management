package com.example.student_topic_management.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.StudentProfile;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    boolean existsByStudentCode(String studentCode);

    @Override
    @EntityGraph(attributePaths = { "department" }) // join luon Department
    Optional<StudentProfile> findById(Long id);
}
