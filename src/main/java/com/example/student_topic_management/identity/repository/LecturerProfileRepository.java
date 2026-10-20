package com.example.student_topic_management.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.LecturerProfile;

public interface LecturerProfileRepository extends JpaRepository<LecturerProfile, Long> {
    boolean existsByLecturerCode(String lecturerCode);

    @Override
    @EntityGraph(attributePaths = { "department" }) // join luon Department
    Optional<LecturerProfile> findById(Long id);
}
