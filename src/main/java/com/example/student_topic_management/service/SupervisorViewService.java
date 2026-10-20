package com.example.student_topic_management.service;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.entity.TopicSupervisor;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.LecturerProfile;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.repository.TopicRepository;
import com.example.student_topic_management.repository.TopicSupervisorRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('DEAN')")
@Transactional(readOnly = true)
public class SupervisorViewService {
    private final TopicRepository topicRepository;
    private final TopicSupervisorRepository supervisorRepository;
    private final LecturerProfileRepository lecturerRepository;
    private final SupervisorService supervisorService;

    public SupervisorViewService(TopicRepository topicRepository,
                                 TopicSupervisorRepository supervisorRepository,
                                 LecturerProfileRepository lecturerRepository,
                                 SupervisorService supervisorService) {
        this.topicRepository = topicRepository;
        this.supervisorRepository = supervisorRepository;
        this.lecturerRepository = lecturerRepository;
        this.supervisorService = supervisorService;
    }

    public Page<Topic> getTopics(int page) {
        return topicRepository.findAll(PageRequest.of(Math.max(page, 0), 10, Sort.by("id").descending()));
    }

    public PageData getPage(Long topicId) {
        if (topicId == null || topicId <= 0) throw new BusinessException("ID đề tài không hợp lệ");
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
        List<TopicSupervisor> assignments = supervisorService.getSupervisors(topicId);
        Set<Long> assignedIds = assignments.stream()
                .map(assignment -> assignment.getId().getLecturerId()).collect(Collectors.toSet());
        Map<Long, LecturerOption> assignedLecturers = new HashMap<>();
        for (LecturerProfile profile : lecturerRepository.findAllById(assignedIds)) {
            assignedLecturers.put(profile.getUserid(), toOption(profile));
        }
        Map<Integer, TopicSupervisor> bySlot = new HashMap<>();
        for (TopicSupervisor assignment : assignments) {
            bySlot.put(assignment.getSlotNo().intValue(), assignment);
        }
        List<SupervisorSlot> slots = List.of(1, 2).stream().map(slotNo -> {
            TopicSupervisor assignment = bySlot.get(slotNo);
            if (assignment == null) return new SupervisorSlot(slotNo, null, "", "Chưa phân công");
            Long lecturerId = assignment.getId().getLecturerId();
            LecturerOption lecturer = assignedLecturers.get(lecturerId);
            return new SupervisorSlot(slotNo, lecturerId,
                    lecturer == null ? "" : lecturer.lecturerCode(),
                    lecturer == null ? "Không tìm thấy hồ sơ" : lecturer.fullName());
        }).toList();
        boolean approvedRegistration = supervisorRepository.countApprovedRegistrationsByTopicId(topicId) > 0;
        boolean canEdit = topic.getStatus() == TopicStatus.DRAFT && !approvedRegistration;
        // Resolve lazy user/roles here; the template receives display values only.
        List<LecturerOption> candidates = canEdit ? lecturerRepository.findAll().stream()
                .filter(profile -> !assignedIds.contains(profile.getUserid()))
                .filter(this::isEligible)
                .map(this::toOption)
                .sorted(Comparator.comparing(LecturerOption::fullName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(LecturerOption::id))
                .toList() : List.of();
        List<Integer> availableSlots = List.of(1, 2).stream()
                .filter(slot -> !bySlot.containsKey(slot)).toList();
        return new PageData(topic, slots, candidates, availableSlots, canEdit, approvedRegistration);
    }

    private boolean isEligible(LecturerProfile profile) {
        AppUser user = profile.getUser();
        return user != null && Boolean.TRUE.equals(user.getEnabled()) && user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> "LECTURER".equals(role.getCode()));
    }

    private LecturerOption toOption(LecturerProfile profile) {
        return new LecturerOption(profile.getUserid(), profile.getLecturerCode(), profile.getUser().getFullname());
    }

    public record LecturerOption(Long id, String lecturerCode, String fullName) {}
    public record SupervisorSlot(Integer slotNo, Long lecturerId, String lecturerCode, String fullName) {}
    public record PageData(Topic topic, List<SupervisorSlot> slots, List<LecturerOption> candidates,
                           List<Integer> availableSlots, boolean canEdit, boolean approvedRegistration) {}
}
