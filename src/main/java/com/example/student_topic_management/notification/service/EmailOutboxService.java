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

    private static final int LAST_ERROR_MAX_LENGTH = 500;

    private final EmailOutboxRepository outboxRepository;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public EmailOutboxService(
            EmailOutboxRepository outboxRepository,
            AuditLogService auditLogService,
            Clock clock) {
        this.outboxRepository = outboxRepository;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<MailJob> claimOneJob(LocalDateTime now) {
        return outboxRepository.claimOneJob(now).map(outbox -> {
            outbox.setStatus("PROCESSING");
            outbox.setAttempts(outbox.getAttempts() + 1);
            outbox.setLockedAt(now);
            outbox.setLastError(null);
            return toMailJob(outbox);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeSuccess(Long id, int claimedAttempt, LocalDateTime now) {
        outboxRepository.findById(id).ifPresent(outbox -> {
            if (isCurrentClaim(outbox, claimedAttempt)) {
                outbox.setStatus("SENT");
                outbox.setSentAt(now);
                outbox.setLockedAt(null);
                outbox.setNextAttemptAt(null);
                outbox.setLastError(null);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeFailure(
            Long id,
            int claimedAttempt,
            String errorMessage,
            int maxAttempts,
            LocalDateTime now) {
        outboxRepository.findById(id).ifPresent(outbox -> {
            if (!isCurrentClaim(outbox, claimedAttempt)) {
                return;
            }
            outbox.setLastError(truncateError(errorMessage));
            outbox.setLockedAt(null);
            if (outbox.getAttempts() >= maxAttempts) {
                outbox.setStatus("FAILED");
                outbox.setNextAttemptAt(null);
            } else {
                outbox.setStatus("PENDING");
                outbox.setNextAttemptAt(now.plusMinutes(retryDelayMinutes(outbox.getAttempts())));
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverStuckJobs(LocalDateTime now, long leaseSeconds, int maxAttempts) {
        if (leaseSeconds <= 0 || maxAttempts <= 0) {
            throw new IllegalArgumentException("Cấu hình retry email không hợp lệ");
        }
        List<EmailOutbox> stuckJobs = outboxRepository.findStuckJobs(now.minusSeconds(leaseSeconds));
        for (EmailOutbox job : stuckJobs) {
            job.setLockedAt(null);
            job.setLastError("Job hết thời gian giữ khóa và đã được hệ thống thu hồi");
            if (job.getAttempts() >= maxAttempts) {
                job.setStatus("FAILED");
                job.setNextAttemptAt(null);
            } else {
                job.setStatus("PENDING");
                job.setNextAttemptAt(now);
            }
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void retryFailedJob(Long id) {
        EmailOutbox outbox = outboxRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy email job #" + id));
        if (!"FAILED".equals(outbox.getStatus())) {
            throw new BusinessException("Chỉ có thể gửi lại job ở trạng thái FAILED");
        }

        outbox.setStatus("PENDING");
        outbox.setAttempts(0);
        outbox.setNextAttemptAt(LocalDateTime.now(clock));
        outbox.setLockedAt(null);
        outbox.setSentAt(null);
        outbox.setLastError(null);

        auditLogService.logCurrentUser(
                "MAIL_RETRY_REQUESTED",
                "EMAIL_OUTBOX",
                id,
                "Yêu cầu gửi lại email job #" + id);
    }

    private boolean isCurrentClaim(EmailOutbox outbox, int claimedAttempt) {
        return "PROCESSING".equals(outbox.getStatus())
                && outbox.getAttempts() == claimedAttempt;
    }

    private MailJob toMailJob(EmailOutbox outbox) {
        return new MailJob(
                outbox.getId(),
                outbox.getAttempts(),
                outbox.getRecipientEmail(),
                outbox.getSubject(),
                outbox.getBody());
    }

    private long retryDelayMinutes(int attempt) {
        return switch (attempt) {
            case 1 -> 1;
            case 2 -> 5;
            case 3 -> 15;
            default -> 60;
        };
    }

    private String truncateError(String errorMessage) {
        String value = errorMessage == null || errorMessage.isBlank()
                ? "Không xác định được lỗi gửi email"
                : errorMessage;
        return value.substring(0, Math.min(value.length(), LAST_ERROR_MAX_LENGTH));
    }
}
