package com.example.student_topic_management.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.LecturerProfile;

public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Long> {
    boolean existsByLecturerCode(String lecturerCode);
}
