package com.example.student_topic_management.notification.worker;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.student_topic_management.notification.dto.MailJob;
import com.example.student_topic_management.notification.gateway.MailGateway;
import com.example.student_topic_management.notification.service.EmailOutboxService;

@Component
@ConditionalOnProperty(name = "app.mail.worker-enabled", havingValue = "true")
public class MailOutboxWorker {

    private final EmailOutboxService outboxService;
    private final MailGateway mailGateway;
    private final Clock clock;
    private final int maxAttempts;
    private final long leaseSeconds;
    private final int batchSize;

    public MailOutboxWorker(
            EmailOutboxService outboxService,
            MailGateway mailGateway,
            Clock clock,
            @Value("${app.mail.max-attempts:5}") int maxAttempts,
            @Value("${app.mail.lease-seconds:300}") long leaseSeconds,
            @Value("${app.mail.batch-size:10}") int batchSize) {
        this.outboxService = outboxService;
        this.mailGateway = mailGateway;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
        this.leaseSeconds = leaseSeconds;
        this.batchSize = Math.max(1, batchSize);
    }

    @Scheduled(fixedDelayString = "${app.mail.fixed-delay-ms:10000}")
    public void processOutbox() {
        LocalDateTime now = LocalDateTime.now(clock);
        outboxService.recoverStuckJobs(now, leaseSeconds, maxAttempts);

        for (int index = 0; index < batchSize; index++) {
            Optional<MailJob> claimed = outboxService.claimOneJob(LocalDateTime.now(clock));
            if (claimed.isEmpty()) {
                return;
            }
            send(claimed.get());
        }
    }

    private void send(MailJob job) {
        try {
            mailGateway.send(job);
            outboxService.finalizeSuccess(job.id(), job.attempt(), LocalDateTime.now(clock));
        } catch (Exception ex) {
            outboxService.finalizeFailure(
                    job.id(),
                    job.attempt(),
                    ex.getMessage(),
                    maxAttempts,
                    LocalDateTime.now(clock));
        }
    }
}
