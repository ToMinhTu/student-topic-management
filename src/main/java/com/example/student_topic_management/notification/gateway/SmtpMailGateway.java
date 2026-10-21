package com.example.student_topic_management.notification.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.student_topic_management.notification.dto.MailJob;

@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpMailGateway implements MailGateway {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public SmtpMailGateway(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(MailJob job) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(job.recipient());
        message.setSubject(job.subject());
        message.setText(job.body());
        mailSender.send(message);
    }
}
