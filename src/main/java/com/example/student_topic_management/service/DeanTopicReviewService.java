package com.example.student_topic_management.service;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.DeanTopicReviewForm;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.policy.RoundPolicy;
import com.example.student_topic_management.repository.RegistrationRoundRepository;
import com.example.student_topic_management.repository.TopicRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('DEAN')")
public class DeanTopicReviewService {
    private final TopicRepository topics;
    private final RegistrationRoundRepository rounds;
    private final CurrentUserService users;
    private final SupervisorService supervisors;
    private final TopicAuditService audit;
    private final RoundPolicy policy;
    private final Clock clock;

    public DeanTopicReviewService(TopicRepository topics, RegistrationRoundRepository rounds,
                                  CurrentUserService users, SupervisorService supervisors,
                                  TopicAuditService audit, RoundPolicy policy, Clock clock) {
        this.topics = topics;
        this.rounds = rounds;
        this.users = users;
        this.supervisors = supervisors;
        this.audit = audit;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<Topic> getPendingTopics(int page) {
        requireActiveDean();
        if (page < 0) throw new BusinessException("Số trang không được âm");
        return topics.findPendingDeanReview(PageRequest.of(page, 10, Sort.by("id").ascending()));
    }

    @Transactional(readOnly = true)
    public ReviewPage getReviewPage(Long topicId) {
        requireActiveDean();
        requirePositiveId(topicId);
        Topic topic = topics.findById(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        RegistrationRound round = rounds.findById(topic.getRoundId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đợt đăng ký"));
        return new ReviewPage(topic, isPendingFinalDecision(topic)
                && policy.isProposalOpen(round, clock.instant()));
    }

    @Transactional
    public void review(Long topicId, DeanTopicReviewForm form) {
        Long deanId = requireActiveDean();
        requirePositiveId(topicId);
        validateForm(form);

        // Match B02/B03.1: lock topic first, then round; never decide from an unlocked snapshot.
        Topic topic = topics.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        RegistrationRound round = rounds.findByIdForUpdate(topic.getRoundId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đợt đăng ký"));
        if (!isPendingFinalDecision(topic)) {
            throw new BusinessException("Chỉ duyệt đề tài SUBMITTED đã được bộ môn đồng ý và chưa có quyết định cuối");
        }
        Instant now = clock.instant();
        if (!policy.isProposalOpen(round, now)) {
            throw new BusinessException("Đợt không trong thời gian đề xuất");
        }

        if (form.getDecision() == DeanTopicReviewForm.Decision.APPROVED) {
            // REQUIRED propagation in SupervisorService joins this transaction and retains the topic lock.
            supervisors.requireSupervisorCountForApproval(topicId);
            topic.setApprovedBy(deanId);
            topic.setApprovedAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
            topic.setStatus(TopicStatus.APPROVED);
        } else {
            topic.setRejectionReason(form.getRejectionReason().strip());
            topic.setStatus(TopicStatus.REJECTED);
        }
        topics.saveAndFlush(topic);
        audit.record(topic, deanId, "TOPIC_DEAN_" + form.getDecision().name());
        audit.snapshotReview(topic, deanId);
    }

    private boolean isPendingFinalDecision(Topic topic) {
        return topic.getStatus() == TopicStatus.SUBMITTED
                && "APPROVED".equals(topic.getDepartmentDecision())
                && topic.getApprovedBy() == null && topic.getApprovedAt() == null
                && topic.getRejectionReason() == null && topic.getPublishedAt() == null;
    }

    private Long requireActiveDean() {
        // This application models one faculty (CNTT), containing all departments.
        // No faculty ID or per-faculty membership exists to narrow the DEAN's scope further.
        AppUser user = users.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getEnabled()) || user.getRoles() == null
                || user.getRoles().stream().noneMatch(role -> role != null && "DEAN".equals(role.getCode()))) {
            throw new AccessDeniedException("Chỉ trưởng khoa đang hoạt động được phê duyệt đề tài");
        }
        return user.getId();
    }

    private void requirePositiveId(Long id) {
        if (id == null || id <= 0) throw new BusinessException("ID đề tài không hợp lệ");
    }

    private void validateForm(DeanTopicReviewForm form) {
        if (form == null || form.getDecision() == null) {
            throw new BusinessException("Vui lòng chọn quyết định của trưởng khoa");
        }
        String reason = form.getRejectionReason() == null ? "" : form.getRejectionReason().strip();
        if (reason.length() > 4000) throw new BusinessException("Lý do từ chối không được vượt quá 4000 ký tự");
        if (form.getDecision() == DeanTopicReviewForm.Decision.REJECTED && reason.isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do từ chối");
        }
    }

    public record ReviewPage(Topic topic, boolean canReview) {}
}