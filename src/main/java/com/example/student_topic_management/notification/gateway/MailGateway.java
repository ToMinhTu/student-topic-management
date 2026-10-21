package com.example.student_topic_management.notification.gateway;

import com.example.student_topic_management.notification.dto.MailJob;

public interface MailGateway {
    void send(MailJob job);
}
