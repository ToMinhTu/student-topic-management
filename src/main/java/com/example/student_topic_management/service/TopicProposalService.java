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

    private AppUser requireProposer() {
        AppUser user = currentUserService.getCurrentUser();

        boolean allowed = user.getRoles() != null
                && user.getRoles().stream().anyMatch(role ->
                    "LECTURER".equals(role.getCode())
                    || "STUDENT".equals(role.getCode())
                );

        if (!allowed) {
            throw new BusinessException(
                "Ban khong co quyen de xuat de tai"
            );
        }

        return user;
    }

    private RegistrationRound requireProposalRound(Long roundId) {
        if (roundId == null || roundId <= 0) {
            throw new BusinessException(
                "Dot dang ky khong hop le"
            );
        }

        RegistrationRound round = roundRepository.findById(roundId)
                .orElseThrow(() -> new BusinessException(
                    "Khong tim thay dot dang ky"
                ));

        if (!roundPolicy.isProposalOpen(
                round, Instant.now(clock))) {
            throw new BusinessException(
                "Dot dang ky khong trong thoi gian de xuat"
            );
        }

        return round;
    }

    private String requireText(
            String value, String field, int maxLength) {

        if (value == null || value.isBlank()) {
            throw new BusinessException(
                field + " khong duoc de trong"
            );
        }

        String normalized = value.trim();

        if (normalized.length() > maxLength) {
            throw new BusinessException(
                field + " vuot qua do dai cho phep"
            );
        }

        return normalized;
    }

    private void validateForm(TopicProposalForm form) {
        if (form == null) {
            throw new BusinessException(
                "Du lieu de tai khong hop le"
            );
        }

        if (form.getRoundId() == null
                || form.getRoundId() <= 0) {
            throw new BusinessException(
                "Vui long chon dot dang ky"
            );
        }

        if (form.getDepartmentId() == null
                || form.getDepartmentId() <= 0) {
            throw new BusinessException(
                "Vui long chon bo mon"
            );
        }

        requireText(form.getCode(), "Ma de tai", 40);
        requireText(form.getTitle(), "Ten de tai", 255);
        requireText(
                form.getDescription(),
                "Mo ta de tai",
                Integer.MAX_VALUE
        );

        if (!departmentRepository.existsById(
                form.getDepartmentId())) {
            throw new BusinessException(
                "Bo mon khong ton tai"
            );
        }
    }

    private void copyFormToTopic(
            TopicProposalForm form, Topic topic) {

        topic.setRoundId(form.getRoundId());
        topic.setDepartmentId(form.getDepartmentId());

        topic.setCode(requireText(
                form.getCode(), "Ma de tai", 40));

        topic.setTitle(requireText(
                form.getTitle(), "Ten de tai", 255));

        topic.setDescription(requireText(
                form.getDescription(),
                "Mo ta de tai",
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
                "Ma de tai da ton tai trong dot nay"
            );
        }

        Topic topic = new Topic();

        copyFormToTopic(form, topic);

        topic.setProposedBy(user.getId());
        topic.setStatus(TopicStatus.DRAFT);

        return topicRepository.save(topic);
    }

    @Transactional
    public Topic updateDraft(
            Long topicId, TopicProposalForm form) {

        AppUser user = requireProposer();

        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException(
                    "Khong tim thay de tai"
                ));

        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException(
                "Ban khong phai nguoi de xuat de tai"
            );
        }

        if (topic.getStatus() != TopicStatus.DRAFT) {
            throw new BusinessException(
                "Chi duoc sua de tai o trang thai DRAFT"
            );
        }

        validateForm(form);

        // Khong cho chuyen de tai sang dot khac.
        if (!topic.getRoundId().equals(form.getRoundId())) {
            throw new BusinessException(
                "Khong duoc thay doi dot dang ky"
            );
        }

        requireProposalRound(topic.getRoundId());

        String code = form.getCode().trim();

        if (topicRepository.existsByRoundIdAndCodeAndIdNot(
                topic.getRoundId(), code, topicId)) {
            throw new BusinessException(
                "Ma de tai da ton tai trong dot nay"
            );
        }

        copyFormToTopic(form, topic);

        return topicRepository.save(topic);
    }

    @Transactional
    public Topic submitProposal(Long topicId) {
        AppUser user = requireProposer();

        Topic topic = topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException(
                    "Khong tim thay de tai"
                ));

        if (!user.getId().equals(topic.getProposedBy())) {
            throw new BusinessException(
                "Ban khong phai nguoi de xuat de tai"
            );
        }

        if (topic.getStatus() != TopicStatus.DRAFT) {
            throw new BusinessException(
                "Chi de tai DRAFT moi duoc gui"
            );
        }

        requireProposalRound(topic.getRoundId());

        topic.setStatus(TopicStatus.SUBMITTED);

        return topicRepository.save(topic);
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
                "Khong tim thay de tai hoac ban khong co quyen xem"
        ));
    }
}
