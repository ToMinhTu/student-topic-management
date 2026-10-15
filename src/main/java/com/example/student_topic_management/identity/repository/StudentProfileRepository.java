package com.example.student_topic_management.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.StudentProfile;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    boolean existsByStudentCode(String studentCode);
}
