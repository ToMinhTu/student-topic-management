package com.example.student_topic_management.notification.worker;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.student_topic_management.notification.dto.MailJob;
import com.example.student_topic_management.notification.gateway.MailGateway;
import com.example.student_topic_management.notification.service.EmailOutboxService;

@Component
public class MailOutboxWorker {
    private final EmailOutboxService outboxService;
    private final MailGateway mailGateway;
    private final Clock clock;

    @Value("${app.mail.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.mail.lease-seconds:300}")
    private long leaseSeconds;

    public MailOutboxWorker(EmailOutboxService outboxService, MailGateway mailGateway, Clock clock) {
        this.outboxService = outboxService;
        this.mailGateway = mailGateway;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.mail.fix-delay-ms:10000}")
    public void processOutbox() {
        LocalDateTime now = LocalDateTime.now(clock);
        outboxService.recoverStuckJobs(now, leaseSeconds, maxAttempts);

        Optional<MailJob> optJob = outboxService.claimOneJob(now);
        if (optJob.isEmpty())
            return;

        MailJob job = optJob.get();

        try {
            mailGateway.send(job);
            outboxService.finalizeSuccess(job.id(), job.attempt(), LocalDateTime.now(clock));
        } catch (Exception ex) {
            String err = ex.getMessage() != null ? ex.getMessage() : "Unknown Error";
            outboxService.finalizeFailure(job.id(), job.attempt(), err.substring(0, Math.min(err.length(), 2000)),
                    maxAttempts, LocalDateTime.now(clock));
        }
    }
}
