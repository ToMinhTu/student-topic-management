
package com.example.student_topic_management.registration.dto;

public record TopicView(
        Long id,
        Long roundId,
        String code,
        String title,
        String description,
        String requirements,
        String status,
        String departmentName,
        boolean available
) {
}

