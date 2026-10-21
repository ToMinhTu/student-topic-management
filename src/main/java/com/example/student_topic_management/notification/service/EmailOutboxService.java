package com.example.student_topic_management.notification.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.audit.service.AuditLogService;
import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.notification.dto.MailJob;
import com.example.student_topic_management.notification.entity.EmailOutbox;
import com.example.student_topic_management.notification.repository.EmailOutboxRepository;

@Service
public class EmailOutboxService {
    private final EmailOutboxRepository outboxRepository;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public EmailOutboxService(EmailOutboxRepository outboxRepository, AuditLogService auditLogService, Clock clock) {
        this.outboxRepository = outboxRepository;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional
    public void enqueueEmail(Long resultId, Long studentId, String email, String subject, String body) {
        outboxRepository.save(new EmailOutbox(resultId, studentId, email, subject, body));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<MailJob> claimOneJob(LocalDateTime now) {
        return outboxRepository.claimOneJob(now).map(outbox -> {
            outbox.setStatus("PROCESSING");
            outbox.setAttempts(outbox.getAttempts() + 1);
            outbox.setLockedAt(now);
            outboxRepository.save(outbox);
            return new MailJob(outbox.getId(), outbox.getAttempts(), outbox.getRecipientEmail(), outbox.getSubject(),
                    outbox.getBody());
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeSuccess(Long id, int claimedAttempt, LocalDateTime now) {
        outboxRepository.findById(id).ifPresent(outbox -> {
            if ("PROCESSING".equals(outbox.getStatus()) && outbox.getAttempts() == claimedAttempt) {
                outbox.setStatus("SENT");
                outbox.setSentAt(now);
                outbox.setLockedAt(null);
                outbox.setNextAttemptAt(null);
                outbox.setLastError(null);
                outboxRepository.save(outbox);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeFailure(Long id, int claimedAttempt, String errorMsg, int maxAttempts, LocalDateTime now) {
        outboxRepository.findById(id).ifPresent(outbox -> {
            if ("PROCESSING".equals(outbox.getStatus()) && outbox.getAttempts() == claimedAttempt) {
                outbox.setLastError(errorMsg);
                outbox.setLockedAt(null);
                if (outbox.getAttempts() >= maxAttempts) {
                    outbox.setStatus("FAILED");
                } else {
                    outbox.setStatus("PENDING");
                    long delayMinutes = switch (outbox.getAttempts()) {
                        case 1 -> 1;
                        case 2 -> 5;
                        case 3 -> 15;
                        default -> 60;
                    };
                    outbox.setNextAttemptAt(now.plusMinutes(delayMinutes));
                }
                outboxRepository.save(outbox);
            }
        });
    }

    @Transactional
    public void recoverStuckJobs(LocalDateTime now, long leaseSeconds, int maxAttempts) {
        List<EmailOutbox> stuckJobs = outboxRepository.findStuckJobs(now.minusSeconds(leaseSeconds));
        for (EmailOutbox job : stuckJobs) {
            job.setLockedAt(null);
            if (job.getAttempts() >= maxAttempts) {
                job.setStatus("FAILED");
            } else {
                job.setStatus("PENDING");
                job.setNextAttemptAt(now);
            }
            outboxRepository.save(job);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void retryFailedJob(Long id) {
        EmailOutbox outbox = outboxRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy yêu cầu"));
        if (!"FAILED".equals(outbox.getStatus()))
            throw new BusinessException("Chỉ thử lại được job FAILED");
        outbox.setStatus("PENDING");
        outbox.setNextAttemptAt(LocalDateTime.now(clock));
        outbox.setLockedAt(null);
        outboxRepository.save(outbox);
        auditLogService.log("MAIL_RETRY_REQUESTED", "EMAIL_OUTBOX", id,
                "Thử lại email tới: " + outbox.getRecipientEmail());
    }

}
