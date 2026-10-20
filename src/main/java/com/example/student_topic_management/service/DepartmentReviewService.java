package com.example.student_topic_management.service;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.DepartmentReviewForm;
import com.example.student_topic_management.entity.*;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.*;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.policy.RoundPolicy;
import com.example.student_topic_management.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('DEPARTMENT_HEAD')")
public class DepartmentReviewService {
    private final TopicRepository topics;
    private final RegistrationRoundRepository rounds;
    private final LecturerProfileRepository lecturers;
    private final CurrentUserService users;
    private final RoundPolicy policy;
    private final Clock clock;
    private final TopicAuditService audit;

    public DepartmentReviewService(TopicRepository topics, RegistrationRoundRepository rounds,
                                   LecturerProfileRepository lecturers, CurrentUserService users,
                                   RoundPolicy policy, Clock clock, TopicAuditService audit) {
        this.topics = topics;
        this.rounds = rounds;
        this.lecturers = lecturers;
        this.users = users;
        this.policy = policy;
        this.clock = clock;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page<Topic> getPendingTopics(int page) {
        Reviewer reviewer = requireReviewer();
        if (page < 0) throw new BusinessException("Số trang không được âm");
        return topics.findByDepartmentIdAndStatusAndDepartmentDecisionIsNull(
                reviewer.departmentId(), TopicStatus.SUBMITTED,
                PageRequest.of(page, 10, Sort.by("id").ascending()));
    }

    @Transactional(readOnly = true)
    public ReviewPage getReviewPage(Long topicId) {
        Reviewer reviewer = requireReviewer();
        Topic topic = requireTopic(topicId);
        requireSameDepartment(topic, reviewer);
        RegistrationRound round = rounds.findById(topic.getRoundId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đợt đăng ký"));
        return new ReviewPage(topic, topic.getStatus() == TopicStatus.SUBMITTED
                && topic.getDepartmentDecision() == null
                && policy.isProposalOpen(round, clock.instant()));
    }

    @Transactional
    public void review(Long topicId, DepartmentReviewForm form) {
        Reviewer reviewer = requireReviewer();
        validateForm(form);
        if (topicId == null || topicId <= 0) throw new BusinessException("ID đề tài không hợp lệ");
        // Match B02's existing topic -> round lock order; read state under lock.
        Topic topic = topics.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        requireSameDepartment(topic, reviewer);
        RegistrationRound round = rounds.findByIdForUpdate(topic.getRoundId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đợt đăng ký"));
        if (topic.getStatus() != TopicStatus.SUBMITTED || topic.getDepartmentDecision() != null) {
            throw new BusinessException("Chỉ sơ duyệt đề tài SUBMITTED chưa có quyết định bộ môn");
        }
        if (!policy.isProposalOpen(round, clock.instant())) {
            throw new BusinessException("Đợt không trong thời gian đề xuất");
        }

        String comment = form.getComment() == null ? null : form.getComment().strip();
        topic.setDepartmentDecision(form.getDecision().name());
        topic.setDepartmentReviewedBy(reviewer.userId());
        topic.setDepartmentReviewedAt(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        topic.setDepartmentComment(comment == null || comment.isEmpty() ? null : comment);
        if (form.getDecision() == DepartmentReviewForm.Decision.REJECTED) {
            topic.setStatus(TopicStatus.REJECTED);
            topic.setRejectionReason(comment);
        }
        topics.saveAndFlush(topic);
        audit.record(topic, reviewer.userId(), "TOPIC_DEPARTMENT_" + form.getDecision().name());
        audit.snapshotReview(topic, reviewer.userId());
    }

    private Reviewer requireReviewer() {
        AppUser user = users.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getEnabled()) || user.getRoles() == null
                || user.getRoles().stream().noneMatch(role -> "DEPARTMENT_HEAD".equals(role.getCode()))) {
            throw new AccessDeniedException("Bạn không có quyền trưởng bộ môn");
        }
        LecturerProfile profile = lecturers.findById(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Không có hồ sơ giảng viên"));
        if (profile.getDepartment() == null || profile.getDepartment().getId() == null) {
            throw new AccessDeniedException("Hồ sơ chưa có bộ môn");
        }
        return new Reviewer(user.getId(), profile.getDepartment().getId());
    }

    private Topic requireTopic(Long id) {
        if (id == null || id <= 0) throw new BusinessException("ID đề tài không hợp lệ");
        return topics.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
    }

    private void requireSameDepartment(Topic topic, Reviewer reviewer) {
        if (!reviewer.departmentId().equals(topic.getDepartmentId())) {
            throw new AccessDeniedException("Không được sơ duyệt đề tài của bộ môn khác");
        }
    }

    private void validateForm(DepartmentReviewForm form) {
        if (form == null || form.getDecision() == null) {
            throw new BusinessException("Vui lòng chọn kết quả sơ duyệt");
        }
        String comment = form.getComment() == null ? "" : form.getComment().strip();
        if (comment.length() > 4000) throw new BusinessException("Nhận xét không được vượt quá 4000 ký tự");
        if (form.getDecision() == DepartmentReviewForm.Decision.REJECTED && comment.isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do từ chối");
        }
    }

    private record Reviewer(Long userId, Long departmentId) {}
    public record ReviewPage(Topic topic, boolean canReview) {}
}