package com.example.student_topic_management.dto;

/**
 * Public topic information only; excludes proposal ownership and review details.
 */
public record PublishedTopicView(
        Long id,
        Long roundId,
        Long departmentId,
        String code,
        String title,
        String description,
        String requirements
) {
}