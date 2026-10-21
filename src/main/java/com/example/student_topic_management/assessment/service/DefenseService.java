// toan-D2
// toan-D2 - thêm chức năng quản lý lịch bảo vệ
package com.example.student_topic_management.assessment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.student_topic_management.assessment.dto.DefenseForm;
import com.example.student_topic_management.assessment.entity.Council;
import com.example.student_topic_management.assessment.entity.CouncilStatus;
import com.example.student_topic_management.assessment.entity.Defense;
import com.example.student_topic_management.assessment.repository.CouncilRepository;
import com.example.student_topic_management.assessment.repository.DefenseRepository;
import com.example.student_topic_management.assessment.repository.CouncilMemberRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;
import com.example.student_topic_management.assessment.entity.CouncilMember;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.identity.service.CurrentUserService;

@Service
public class DefenseService {

    private final DefenseRepository defenseRepository;
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final ReviewerAssignmentRepository assignmentRepository;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbc;

    public DefenseService(
            DefenseRepository defenseRepository,
            CouncilRepository councilRepository,
            CouncilMemberRepository councilMemberRepository,
            ReviewerAssignmentRepository assignmentRepository,
            CurrentUserService currentUserService,
            JdbcTemplate jdbc) {

        this.defenseRepository = defenseRepository;
        this.councilRepository = councilRepository;
        this.councilMemberRepository = councilMemberRepository;
        this.assignmentRepository = assignmentRepository;
        this.currentUserService = currentUserService;
        this.jdbc = jdbc;
    }

    public List<Defense> findAll() {
        return defenseRepository.findAll();
    }

    public List<Defense> findByRoundId(Long roundId) {
        return defenseRepository.findByRoundId(roundId);
    }

    public Defense findById(Long id) {
        return defenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy lịch bảo vệ"));
    }

    @Transactional
    public void saveDefense(DefenseForm form) {

        List<RegistrationInfo> registrations = jdbc.query(
                "SELECT round_id, status FROM topic_registrations WHERE id = ?",
                (rs, rowNum) -> new RegistrationInfo(
                        rs.getLong("round_id"),
                        rs.getString("status")),
                form.getRegistrationId());

        if (registrations.isEmpty()) {
            throw new RuntimeException(
                    "Không tìm thấy hồ sơ đăng ký #" + form.getRegistrationId());
        }

        RegistrationInfo registration = registrations.get(0);

        if (!registration.roundId().equals(form.getRoundId())) {
            throw new RuntimeException(
                    "Hồ sơ #" + form.getRegistrationId()
                            + " thuộc đợt #" + registration.roundId()
                            + ", không phải đợt #" + form.getRoundId());
        }

        if (!"APPROVED".equals(registration.status())) {
            throw new RuntimeException("Chỉ hồ sơ đăng ký đã được duyệt mới được lập lịch bảo vệ");
        }

        Council council = councilRepository.findById(form.getCouncilId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy hội đồng"));

        if (council.getStatus() != CouncilStatus.ACTIVE) {
            throw new RuntimeException(
                    "Hội đồng phải ở trạng thái ACTIVE");
        }

        if (defenseRepository.existsByRegistrationId(
                form.getRegistrationId())) {

            throw new RuntimeException(
                    "Hồ sơ này đã có lịch bảo vệ");
        }

        if (form.getScheduledAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Thời gian bảo vệ phải ở tương lai");
        }

        if (!council.getRoundId()
                .equals(form.getRoundId())) {

            throw new RuntimeException(
                    "Hội đồng không thuộc đợt đăng ký này");
        }

        boolean councilConflict = defenseRepository.findByCouncilId(form.getCouncilId()).stream()
                .anyMatch(d -> d.getScheduledAt().equals(form.getScheduledAt())
                        && !"CANCELLED".equals(d.getStatus()));
        if (councilConflict) {
            throw new RuntimeException("Hội đồng đã có lịch bảo vệ tại thời điểm này");
        }

        Defense defense = new Defense();

        defense.setRegistrationId(form.getRegistrationId());
        defense.setRoundId(form.getRoundId());
        defense.setCouncilId(form.getCouncilId());
        defense.setScheduledAt(form.getScheduledAt());
        defense.setLocation(form.getLocation());
        defense.setStatus("SCHEDULED");

        defenseRepository.save(defense);

        Long assignedBy = currentUserService.getCurrentUserId();
        for (CouncilMember member : councilMemberRepository.findByIdCouncilId(form.getCouncilId())) {
            saveAssignment(form.getRegistrationId(), member.getId().getLecturerId(), "COUNCIL", assignedBy);
        }
        if (form.getReviewerLecturerId() != null) {
            if (councilMemberRepository.existsById(new com.example.student_topic_management.assessment.entity.CouncilMemberId(form.getCouncilId(), form.getReviewerLecturerId())))
                throw new RuntimeException("Giảng viên phản biện không được trùng thành viên hội đồng");
            saveAssignment(form.getRegistrationId(), form.getReviewerLecturerId(), "REVIEWER", assignedBy);
        }
    }

    private void saveAssignment(Long registrationId, Long lecturerId, String type, Long assignedBy) {
        if (assignmentRepository.existsByRegistrationIdAndLecturerId(registrationId, lecturerId))
            throw new RuntimeException("Một giảng viên không được phân công trùng trong cùng hồ sơ");
        ReviewerAssignment a = new ReviewerAssignment();
        a.setRegistrationId(registrationId); a.setLecturerId(lecturerId);
        a.setAssignmentType(type); a.setAssignedBy(assignedBy);
        assignmentRepository.save(a);
    }

    private record RegistrationInfo(Long roundId, String status) {}
}
