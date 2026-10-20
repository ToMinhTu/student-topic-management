package com.example.student_topic_management.dto;

/**
 * Optional filters; page is zero-based. Null page/size use service defaults.
 */
public record PublishedTopicFilter(
        String keyword,
        Long roundId,
        Long departmentId,
        Integer page,
        Integer size
) {
}