package com.example.student_topic_management.notification.gateway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.student_topic_management.notification.dto.MailJob;

@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class FakeMailGateway implements MailGateway {
    @Override
    public void send(MailJob job) {
        System.out.println("FAKE MAIL: Gửi email giả lập tới " + job.recipient());
    }
}
