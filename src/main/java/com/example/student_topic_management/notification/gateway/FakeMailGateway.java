package com.example.student_topic_management.notification.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.student_topic_management.notification.dto.MailJob;

@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class FakeMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(FakeMailGateway.class);

    @Override
    public void send(MailJob job) {
        log.info("Đã giả lập gửi email cho job #{}", job.id());
    }
}
