package com.example.student_topic_management.service;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.repository.TopicRepository;
import java.time.Clock;
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
public class TopicPublicationService {
    private final TopicRepository topics;
    private final CurrentUserService users;
    private final TopicAuditService audit;
    private final Clock clock;

    public TopicPublicationService(TopicRepository topics, CurrentUserService users,
                                   TopicAuditService audit, Clock clock) {
        this.topics = topics;
        this.users = users;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<Topic> getApprovedTopics(int page) {
        requireActiveDean();
        if (page < 0) throw new BusinessException("Số trang không được âm");
        return topics.findByStatusAndPublishedAtIsNull(TopicStatus.APPROVED,
                PageRequest.of(page, 10, Sort.by("code").ascending().and(Sort.by("id").ascending())));
    }

    @Transactional
    public void publish(Long topicId) {
        Long deanId = requireActiveDean();
        if (topicId == null || topicId <= 0) throw new BusinessException("ID đề tài không hợp lệ");
        Topic topic = topics.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        if (topic.getStatus() != TopicStatus.APPROVED || topic.getPublishedAt() != null) {
            throw new BusinessException("Chỉ được công bố đề tài APPROVED chưa được công bố");
        }
        topic.setStatus(TopicStatus.PUBLISHED);
        topic.setPublishedAt(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        topics.saveAndFlush(topic);
        audit.record(topic, deanId, "TOPIC_PUBLISH");
        audit.snapshotReview(topic, deanId);
    }

    private Long requireActiveDean() {
        AppUser user = users.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getEnabled()) || user.getRoles() == null
                || user.getRoles().stream().noneMatch(role -> role != null && "DEAN".equals(role.getCode()))) {
            throw new AccessDeniedException("Chỉ trưởng khoa đang hoạt động được công bố đề tài");
        }
        return user.getId();
    }
}