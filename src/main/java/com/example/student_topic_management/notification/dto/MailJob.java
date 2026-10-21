package com.example.student_topic_management.notification.dto;

public record MailJob(
        Long id,
        int attempt,
        String recipient,
        String subject,
        String body) {
}
