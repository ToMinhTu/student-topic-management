package com.example.student_topic_management.service;

import com.example.student_topic_management.dto.TopicProposalForm;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.repository.TopicRepository;
import com.example.student_topic_management.repository.RegistrationRoundRepository;
import com.example.student_topic_management.policy.RoundPolicy;

import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.common.exception.BusinessException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

@Service
public class TopicProposalService {

    private final TopicRepository topicRepository;
    private final RegistrationRoundRepository roundRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserService currentUserService;
    private final RoundPolicy roundPolicy;
    private final Clock clock;
    private final TopicAuditService audit;

    public TopicProposalService(
            TopicRepository topicRepository,
            RegistrationRoundRepository roundRepository,
            DepartmentRepository departmentRepository,
            CurrentUserService currentUserService,
            RoundPolicy roundPolicy,
            Clock clock, TopicAuditService audit) {

        this.topicRepository = topicRepository;
        this.roundRepository = roundRepository;
        this.departmentRepository = departmentRepository;
        this.currentUserService = currentUserService;
        this.roundPolicy = roundPolicy;
        this.clock = clock;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public java.util.List<RegistrationRound> getProposalRounds(Long selectedRoundId) {
        requireProposer();
        Instant now = Instant.now(clock);
        return roundRepository.findAll(org.springframework.data.domain.Sort.by("id").descending())
                .stream()
                .filter(round -> roundPolicy.isProposalOpen(round, now)
                        || round.getId().equals(selectedRoundId))
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<com.example.student_topic_management.identity.entity.Department> getDepartments() {
        requireProposer();
        return departmentRepository.findAll(org.springframework.data.domain.Sort.by("name"));
    }

    private void validateTopicId(Long topicId) {
        if (topicId == null || topicId <= 0) throw new BusinessException("ID đề tài không hợp lệ");
    }

    private String normalizeCode(String code) {
        if (code == null) throw new BusinessException("Mã đề tài không được để trống");
        String normalized = code.strip().toUpperCase(java.util.Locale.ROOT);
        requireText(normalized, "Mã đề tài", 40);
        if (normalized.codePoints().anyMatch(ch -> Character.isWhitespace(ch) || Character.isSpaceChar(ch)
                || Character.isISOControl(ch))) {
            throw new BusinessException("Mã đề tài không được chứa khoảng trắng hoặc ký tự điều khiển");
        }
        return normalized;
    }

    private void reopenRejected(Topic topic, Long actorId) {
        if (topic.getStatus() != TopicStatus.REJECTED) return;
        audit.record(topic, actorId, "TOPIC_REOPEN");
        audit.snapshotReview(topic, actorId);
        topic.setDepartmentDecision(null);
        topic.setDepartmentReviewedBy(null);
        topic.setDepartmentReviewedAt(null);
        topic.setDepartmentComment(null);
        topic.setApprovedBy(null);
        topic.setApprovedAt(null);
        topic.setRejectionReason(null);
        topic.setPublishedAt(null);
        topic.setStatus(TopicStatus.DRAFT);
    }

    @Transactional
    public void deleteDraft(Long topicId) {
        AppUser user = requireProposer();
        validateTopicId(topicId);
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException("Bạn không phải người đề xuất đề tài");
        }
        if (topic.getStatus() != TopicStatus.DRAFT) {
            throw new BusinessException("Chỉ được xóa đề tài nháp");
        }
        requireProposalRound(topic.getRoundId());
        audit.record(topic, user.getId(), "TOPIC_DELETE");
        topicRepository.delete(topic);
        topicRepository.flush();
    }

    private AppUser requireProposer() {
        AppUser user = currentUserService.getCurrentUser();

        boolean allowed = Boolean.TRUE.equals(user.getEnabled()) && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role ->
                    "LECTURER".equals(role.getCode())
                    || "STUDENT".equals(role.getCode())
                );

        if (!allowed) {
            throw new BusinessException(
                "Bạn không có quyền đề xuất đề tài"
            );
        }

        return user;
    }

    private RegistrationRound requireProposalRound(Long roundId) {
        if (roundId == null || roundId <= 0) {
            throw new BusinessException(
                "Đợt đăng ký không hợp lệ"
            );
        }

        RegistrationRound round = roundRepository.findByIdForUpdate(roundId)
                .orElseThrow(() -> new BusinessException(
                    "Không tìm thấy đợt đăng ký"
                ));

        if (!roundPolicy.isProposalOpen(
                round, Instant.now(clock))) {
            throw new BusinessException(
                "Đợt đăng ký không trong thời gian đề xuất"
            );
        }

        return round;
    }

    private String requireText(
            String value, String field, int maxLength) {

        if (value == null || value.isBlank()) {
            throw new BusinessException(
                field + " không được để trống"
            );
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength || normalized.getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new BusinessException(
                field + " vượt quá độ dài cho phép"
            );
        }

        return normalized;
    }

    private void validateForm(TopicProposalForm form) {
        if (form == null) {
            throw new BusinessException(
                "Dữ liệu đề tài không hợp lệ"
            );
        }

        if (form.getRoundId() == null
                || form.getRoundId() <= 0) {
            throw new BusinessException(
                "Vui lòng chọn đợt đăng ký"
            );
        }

        if (form.getDepartmentId() == null
                || form.getDepartmentId() <= 0) {
            throw new BusinessException(
                "Vui lòng chọn bộ môn"
            );
        }

        if (form.getRequirements() != null
                && form.getRequirements().getBytes(StandardCharsets.UTF_8).length > 65535) {
            throw new BusinessException("Yêu cầu đề tài vượt quá dung lượng cho phép");
        }

        normalizeCode(form.getCode());
        requireText(form.getTitle(), "Tên đề tài", 255);
        requireText(
                form.getDescription(),
                "Mô tả đề tài",
                Integer.MAX_VALUE
        );

        if (!departmentRepository.existsById(
                form.getDepartmentId())) {
            throw new BusinessException(
                "Bộ môn không tồn tại"
            );
        }
    }

    private void copyFormToTopic(
            TopicProposalForm form, Topic topic) {

        topic.setRoundId(form.getRoundId());
        topic.setDepartmentId(form.getDepartmentId());

        topic.setCode(normalizeCode(form.getCode()));

        topic.setTitle(requireText(
                form.getTitle(), "Tên đề tài", 255));

        topic.setDescription(requireText(
                form.getDescription(),
                "Mô tả đề tài",
                Integer.MAX_VALUE
        ));

        topic.setRequirements(
                form.getRequirements() == null
                    ? null
                    : form.getRequirements().trim()
        );
    }

    @Transactional
    public Topic createDraft(TopicProposalForm form) {
        AppUser user = requireProposer();

        validateForm(form);
        requireProposalRound(form.getRoundId());

        String code = normalizeCode(form.getCode());

        if (topicRepository.existsByRoundIdAndCode(
                form.getRoundId(), code)) {
            throw new BusinessException(
                "Mã đề tài đã tồn tại trong đợt này"
            );
        }

        Topic topic = new Topic();

        copyFormToTopic(form, topic);

        topic.setProposedBy(user.getId());
        topic.setStatus(TopicStatus.DRAFT);

        Topic saved = topicRepository.saveAndFlush(topic);
        audit.record(saved, user.getId(), "TOPIC_CREATE");
        return saved;
    }

    @Transactional
    public Topic updateDraft(
            Long topicId, TopicProposalForm form) {

        AppUser user = requireProposer();

        validateTopicId(topicId);
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException(
                    "Không tìm thấy đề tài"
                ));

        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException(
                "Bạn không phải người đề xuất đề tài"
            );
        }

        if (topic.getStatus() != TopicStatus.DRAFT && topic.getStatus() != TopicStatus.REJECTED) {
            throw new BusinessException(
                "Chỉ được sửa đề tài nháp hoặc bị từ chối"
            );
        }

        validateForm(form);

        // Khong cho chuyen de tai sang dot khac.
        if (!topic.getRoundId().equals(form.getRoundId())) {
            throw new BusinessException(
                "Không được thay đổi đợt đăng ký"
            );
        }

        requireProposalRound(topic.getRoundId());

        String code = normalizeCode(form.getCode());

        if (topicRepository.existsByRoundIdAndCodeAndIdNot(
                topic.getRoundId(), code, topicId)) {
            throw new BusinessException(
                "Mã đề tài đã tồn tại trong đợt này"
            );
        }

        reopenRejected(topic, user.getId());
        copyFormToTopic(form, topic);
        Topic saved = topicRepository.saveAndFlush(topic);
        audit.record(saved, user.getId(), "TOPIC_UPDATE");
        return saved;
    }

    @Transactional
    public Topic submitProposal(Long topicId) {
        AppUser user = requireProposer();

        validateTopicId(topicId);
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException(
                    "Không tìm thấy đề tài"
                ));

        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException(
                "Bạn không phải người đề xuất đề tài"
            );
        }

        if (topic.getStatus() != TopicStatus.DRAFT && topic.getStatus() != TopicStatus.REJECTED) {
            throw new BusinessException(
                "Chỉ đề tài nháp hoặc bị từ chối mới được gửi"
            );
        }

        requireProposalRound(topic.getRoundId());

        String code = normalizeCode(topic.getCode());
        if (topicRepository.existsByRoundIdAndCodeAndIdNot(topic.getRoundId(), code, topicId)) {
            throw new BusinessException("Mã đề tài đã tồn tại trong đợt này");
        }
        reopenRejected(topic, user.getId());
        topic.setCode(code);
        topic.setStatus(TopicStatus.SUBMITTED);

        Topic saved = topicRepository.saveAndFlush(topic);
        audit.record(saved, user.getId(), "TOPIC_SUBMIT");
        return saved;
    }

    @Transactional
    public Topic publishTopic(Long topicId) {
        AppUser user = currentUserService.getCurrentUser();
        boolean lecturer = Boolean.TRUE.equals(user.getEnabled()) && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> "LECTURER".equals(role.getCode()));
        if (!lecturer) throw new BusinessException("Chỉ giáo viên mới được công bố đề tài");
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        if (topic.getStatus() != TopicStatus.APPROVED)
            throw new BusinessException("Chỉ đề tài đã được duyệt mới được công bố");
        topic.setStatus(TopicStatus.PUBLISHED);
        topic.setPublishedAt(java.time.LocalDateTime.now(clock));
        Topic saved = topicRepository.saveAndFlush(topic);
        audit.record(saved, user.getId(), "TOPIC_PUBLISH");
        return saved;
    }

    @Transactional
    public Topic approveTopic(Long topicId) {
        AppUser user = requireReviewer();
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        if (topic.getStatus() != TopicStatus.SUBMITTED)
            throw new BusinessException("Chỉ đề tài đang chờ duyệt mới được duyệt");
        topic.setStatus(TopicStatus.APPROVED);
        topic.setApprovedBy(user.getId());
        topic.setApprovedAt(java.time.LocalDateTime.now(clock));
        topic.setRejectionReason(null);
        Topic saved = topicRepository.saveAndFlush(topic);
        audit.record(saved, user.getId(), "TOPIC_APPROVE");
        return saved;
    }

    @Transactional
    public Topic rejectTopic(Long topicId, String reason) {
        AppUser user = requireReviewer();
        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        if (topic.getStatus() != TopicStatus.SUBMITTED)
            throw new BusinessException("Chỉ đề tài đang chờ duyệt mới được từ chối");
        topic.setStatus(TopicStatus.REJECTED);
        topic.setApprovedBy(user.getId());
        topic.setApprovedAt(java.time.LocalDateTime.now(clock));
        topic.setRejectionReason(reason == null || reason.isBlank() ? "Không đạt yêu cầu" : reason.trim());
        Topic saved = topicRepository.saveAndFlush(topic);
        audit.record(saved, user.getId(), "TOPIC_REJECT");
        return saved;
    }

    private AppUser requireReviewer() {
        AppUser user = currentUserService.getCurrentUser();
        boolean allowed = Boolean.TRUE.equals(user.getEnabled()) && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role ->
                    "LECTURER".equals(role.getCode()) || "DEAN".equals(role.getCode()));
        if (!allowed) throw new BusinessException("Chỉ giáo viên hoặc trưởng khoa mới được duyệt đề tài");
        return user;
    }

    @Transactional(readOnly = true)
    public Page<Topic> getMyTopics(Pageable pageable) {
        requireProposer();
        return topicRepository.findByProposedBy(requireProposer().getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Topic getMyTopicDetail(Long topicId) {
        requireProposer();
        return topicRepository.findById(topicId).orElseThrow(() -> new BusinessException(
                "Không tìm thấy đề tài"
        ));
    }
}
