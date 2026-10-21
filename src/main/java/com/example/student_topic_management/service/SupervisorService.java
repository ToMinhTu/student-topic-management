package com.example.student_topic_management.service;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.entity.TopicSupervisor;
import com.example.student_topic_management.entity.TopicSupervisorId;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.LecturerProfile;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.repository.TopicRepository;
import com.example.student_topic_management.repository.TopicSupervisorRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupervisorService {

    private final TopicRepository topicRepository;
    private final TopicSupervisorRepository supervisorRepository;
    private final LecturerProfileRepository lecturerRepository;
    private final CurrentUserService currentUserService;

    public SupervisorService(TopicRepository topicRepository,
                             TopicSupervisorRepository supervisorRepository,
                             LecturerProfileRepository lecturerRepository,
                             CurrentUserService currentUserService) {
        this.topicRepository = topicRepository;
        this.supervisorRepository = supervisorRepository;
        this.lecturerRepository = lecturerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    @PreAuthorize("hasRole('DEAN')")
    public TopicSupervisor addSupervisor(Long topicId, Long lecturerId, Integer slotNo) {
        requireActiveDean();
        requirePositiveId(topicId, "ID đề tài");
        requirePositiveId(lecturerId, "ID giảng viên");
        if (slotNo == null || slotNo < 1 || slotNo > 2) {
            throw new BusinessException("Vị trí giảng viên hướng dẫn phải là 1 hoặc 2");
        }

        Topic topic = lockedTopic(topicId);
        requireEditable(topic);
        requireEligibleLecturer(lecturerId);
        if (supervisorRepository.existsByIdTopicIdAndIdLecturerId(topicId, lecturerId)) {
            throw new BusinessException("Giảng viên đã được phân công hướng dẫn đề tài này");
        }
        if (supervisorRepository.existsByIdTopicIdAndSlotNo(topicId, slotNo.byteValue())) {
            throw new BusinessException("Vị trí giảng viên hướng dẫn đã được sử dụng");
        }
        if (supervisorRepository.countByIdTopicId(topicId) >= 2) {
            throw new BusinessException("Một đề tài chỉ có tối đa 2 giảng viên hướng dẫn");
        }

        try {
            return supervisorRepository.saveAndFlush(
                    new TopicSupervisor(topicId, lecturerId, slotNo.byteValue()));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Không thể phân công giảng viên: dữ liệu đã thay đổi hoặc bị trùng. Vui lòng tải lại và thử lại.");
        }
    }

    @Transactional
    @PreAuthorize("hasRole('DEAN')")
    public void removeSupervisor(Long topicId, Long lecturerId) {
        requireActiveDean();
        requirePositiveId(topicId, "ID đề tài");
        requirePositiveId(lecturerId, "ID giảng viên");
        Topic topic = lockedTopic(topicId);
        requireEditable(topic);

        TopicSupervisor supervisor = supervisorRepository.findById(new TopicSupervisorId(topicId, lecturerId))
                .orElseThrow(() -> new BusinessException("Không tìm thấy phân công giảng viên hướng dẫn"));
        try {
            supervisorRepository.delete(supervisor);
            supervisorRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Không thể xóa phân công vì dữ liệu đang được sử dụng hoặc đã thay đổi");
        }
    }

    @Transactional(readOnly = true)
    public List<TopicSupervisor> getSupervisors(Long topicId) {
        requirePositiveId(topicId, "ID đề tài");
        if (!topicRepository.existsById(topicId)) {
            throw new BusinessException("Không tìm thấy đề tài");
        }
        return supervisorRepository.findByIdTopicIdOrderBySlotNoAsc(topicId);
    }

    // lecturerId is users.id / lecturers.user_id, not the lecturer code.
    // Keep historical assignments visible even when the lecturer account is disabled.
    @Transactional(readOnly = true)
    public boolean isSupervisor(Long topicId, Long lecturerId) {
        requirePositiveId(topicId, "ID đề tài");
        requirePositiveId(lecturerId, "ID giảng viên");
        return supervisorRepository.existsByIdTopicIdAndIdLecturerId(topicId, lecturerId);
    }

    // REQUIRED joins B03's transaction. B03 must approve/publish in that same transaction.
    // Do not require DRAFT here: approval can be checking a SUBMITTED topic.
    @Transactional
    public void requireSupervisorCountForApproval(Long topicId) {
        requirePositiveId(topicId, "ID đề tài");
        lockedTopic(topicId);
        long count = supervisorRepository.countByIdTopicId(topicId);
        if (count < 1 || count > 2) {
            throw new BusinessException("Đề tài phải có từ 1 đến 2 giảng viên hướng dẫn trước khi duyệt hoặc công bố");
        }
    }

    private Topic lockedTopic(Long topicId) {
        return topicRepository.findByIdForUpdate(topicId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đề tài"));
    }

    private void requireEditable(Topic topic) {
        if (topic.getStatus() != TopicStatus.DRAFT) {
            throw new BusinessException("Chỉ được thay đổi giảng viên hướng dẫn khi đề tài ở trạng thái DRAFT");
        }
        if (supervisorRepository.countApprovedRegistrationsByTopicId(topic.getId()) > 0) {
            throw new BusinessException("Không được thay đổi giảng viên hướng dẫn khi đề tài đã có đăng ký APPROVED");
        }
    }

    private void requireEligibleLecturer(Long lecturerId) {
        LecturerProfile profile = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ giảng viên"));
        AppUser user = profile.getUser();
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException("Tài khoản giảng viên không hoạt động");
        }
        if (!hasRole(user, "LECTURER")) {
            throw new BusinessException("Tài khoản được chọn không có vai trò LECTURER");
        }
    }

    private void requireActiveDean() {
        AppUser user = currentUserService.getCurrentUser();
        if (!Boolean.TRUE.equals(user.getEnabled()) || !hasRole(user, "DEAN")) {
            throw new BusinessException("Chỉ trưởng khoa có tài khoản đang hoạt động được thay đổi giảng viên hướng dẫn");
        }
    }

    private boolean hasRole(AppUser user, String code) {
        return user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> role != null && code.equals(role.getCode()));
    }

    private void requirePositiveId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new BusinessException(field + " không hợp lệ");
        }
    }
}
