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

    public TopicProposalService(
            TopicRepository topicRepository,
            RegistrationRoundRepository roundRepository,
            DepartmentRepository departmentRepository,
            CurrentUserService currentUserService,
            RoundPolicy roundPolicy,
            Clock clock) {

        this.topicRepository = topicRepository;
        this.roundRepository = roundRepository;
        this.departmentRepository = departmentRepository;
        this.currentUserService = currentUserService;
        this.roundPolicy = roundPolicy;
        this.clock = clock;
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

        requireText(form.getCode(), "Mã đề tài", 40);
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

        topic.setCode(requireText(
                form.getCode(), "Mã đề tài", 40));

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

        String code = form.getCode().trim();

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

        return topicRepository.saveAndFlush(topic);
    }

    @Transactional
    public Topic updateDraft(
            Long topicId, TopicProposalForm form) {

        AppUser user = requireProposer();

        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException(
                    "Không tìm thấy đề tài"
                ));

        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException(
                "Bạn không phải người đề xuất đề tài"
            );
        }

        if (topic.getStatus() != TopicStatus.DRAFT) {
            throw new BusinessException(
                "Chỉ được sửa đề tài ở trạng thái nháp"
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

        String code = form.getCode().trim();

        if (topicRepository.existsByRoundIdAndCodeAndIdNot(
                topic.getRoundId(), code, topicId)) {
            throw new BusinessException(
                "Mã đề tài đã tồn tại trong đợt này"
            );
        }

        copyFormToTopic(form, topic);

        return topicRepository.saveAndFlush(topic);
    }

    @Transactional
    public Topic submitProposal(Long topicId) {
        AppUser user = requireProposer();

        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException(
                    "Không tìm thấy đề tài"
                ));

        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException(
                "Bạn không phải người đề xuất đề tài"
            );
        }

        if (topic.getStatus() != TopicStatus.DRAFT) {
            throw new BusinessException(
                "Chỉ đề tài nháp mới được gửi"
            );
        }

        requireProposalRound(topic.getRoundId());

        topic.setStatus(TopicStatus.SUBMITTED);

        return topicRepository.saveAndFlush(topic);
    }

    @Transactional(readOnly = true)
    public Page<Topic> getMyTopics(Pageable pageable) {
        AppUser user = requireProposer();

        return topicRepository.findByProposedBy(
                user.getId(), pageable
        );
    }

    @Transactional(readOnly = true)
    public Topic getMyTopicDetail(Long topicId) {
        AppUser user = requireProposer();

        return topicRepository.findByIdAndProposedBy(
                topicId, user.getId()
        ).orElseThrow(() -> new BusinessException(
                "Không tìm thấy đề tài hoặc bạn không có quyền xem"
        ));
    }
}
