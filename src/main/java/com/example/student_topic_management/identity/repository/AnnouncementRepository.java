package com.example.student_topic_management.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.student_topic_management.identity.entity.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // Lay ds ban nhap (publishedAt IS NULL) - cho admin
    @EntityGraph(attributePaths = "author")
    List<Announcement> findByPublishedAtIsNullOrderByCreatedAtDesc();

    // Lay ds ds cong bo (publishedAt <= now) - cho sinh vien/giang vien doc
    @EntityGraph(attributePaths = "author")
    List<Announcement> findByPublishedAtIsNotNullAndPublishedAtLessThanEqualOrderByPublishedAtDesc(LocalDateTime now);

    @EntityGraph(attributePaths = "author")
    Optional<Announcement> findById(Long id);
}
