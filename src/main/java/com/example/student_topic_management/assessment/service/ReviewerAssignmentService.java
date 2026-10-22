package com.example.student_topic_management.assessment.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.assessment.dto.ReviewerAssignmentForm;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.assessment.repository.EvaluationRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;

@Service
public class ReviewerAssignmentService {

    public record AssignmentRow(
            Long id,
            Long registrationId,
            String groupName,
            String topicCode,
            String topicTitle,
            String roundName,
            Long lecturerId,
            String lecturerCode,
            String lecturerName,
            String assignmentType,
            LocalDateTime createdAt,
            boolean evaluated) {
    }

    public record RegistrationOption(
            Long id,
            String groupName,
            String topicCode,
            String topicTitle,
            String roundName,
            String councilName) {
    }

    public record LecturerOption(
            Long id,
            String lecturerCode,
            String fullName) {
    }

    private record RegistrationContext(
            Long topicId,
            String registrationStatus,
            String roundType,
            Long councilId,
            String councilStatus,
            String defenseStatus,
            long reportCount) {
    }

    private static final String REVIEWER = "REVIEWER";
    private static final String COUNCIL = "COUNCIL";

    private final ReviewerAssignmentRepository assignments;
    private final EvaluationRepository evaluations;
    private final JdbcTemplate jdbc;
    private final CurrentUserService currentUser;

    public ReviewerAssignmentService(
            ReviewerAssignmentRepository assignments,
            EvaluationRepository evaluations,
            JdbcTemplate jdbc,
            CurrentUserService currentUser) {

        this.assignments = assignments;
        this.evaluations = evaluations;
        this.jdbc = jdbc;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasRole('DEAN')")
    @Transactional(readOnly = true)
    public List<AssignmentRow> findAll() {
        return jdbc.query(
                """
                SELECT ra.id, ra.registration_id, sg.name AS group_name,
                       t.code AS topic_code, t.title AS topic_title,
                       rr.name AS round_name, ra.lecturer_id,
                       l.lecturer_code, u.full_name AS lecturer_name,
                       ra.assignment_type, ra.created_at,
                       CASE WHEN e.id IS NULL THEN FALSE ELSE TRUE END AS evaluated
                FROM reviewer_assignments ra
                JOIN topic_registrations tr ON tr.id = ra.registration_id
                JOIN student_groups sg ON sg.id = tr.group_id
                JOIN topics t ON t.id = tr.topic_id
                JOIN registration_rounds rr ON rr.id = tr.round_id
                JOIN lecturers l ON l.user_id = ra.lecturer_id
                JOIN users u ON u.id = ra.lecturer_id
                LEFT JOIN evaluations e ON e.assignment_id = ra.id
                ORDER BY ra.created_at DESC, ra.id DESC
                """,
                (rs, rowNum) -> new AssignmentRow(
                        rs.getLong("id"),
                        rs.getLong("registration_id"),
                        rs.getString("group_name"),
                        rs.getString("topic_code"),
                        rs.getString("topic_title"),
                        rs.getString("round_name"),
                        rs.getLong("lecturer_id"),
                        rs.getString("lecturer_code"),
                        rs.getString("lecturer_name"),
                        rs.getString("assignment_type"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getBoolean("evaluated")
                )
        );
    }

    @PreAuthorize("hasRole('DEAN')")
    @Transactional(readOnly = true)
    public List<RegistrationOption> findEligibleRegistrations() {
        return jdbc.query(
                """
                SELECT tr.id, sg.name AS group_name, t.code AS topic_code,
                       t.title AS topic_title, rr.name AS round_name,
                       c.name AS council_name
                FROM topic_registrations tr
                JOIN student_groups sg ON sg.id = tr.group_id
                JOIN topics t ON t.id = tr.topic_id
                JOIN registration_rounds rr ON rr.id = tr.round_id
                JOIN defenses d ON d.registration_id = tr.id
                JOIN councils c ON c.id = d.council_id
                WHERE tr.status = 'APPROVED'
                  AND d.status <> 'CANCELLED'
                  AND c.status = 'ACTIVE'
                  AND EXISTS (
                      SELECT 1 FROM reports r WHERE r.registration_id = tr.id
                  )
                ORDER BY rr.created_at DESC, tr.id DESC
                """,
                (rs, rowNum) -> new RegistrationOption(
                        rs.getLong("id"),
                        rs.getString("group_name"),
                        rs.getString("topic_code"),
                        rs.getString("topic_title"),
                        rs.getString("round_name"),
                        rs.getString("council_name")
                )
        );
    }

    @PreAuthorize("hasRole('DEAN')")
    @Transactional(readOnly = true)
    public List<LecturerOption> findActiveLecturers() {
        return jdbc.query(
                """
                SELECT l.user_id, l.lecturer_code, u.full_name
                FROM lecturers l
                JOIN users u ON u.id = l.user_id
                WHERE u.enabled = TRUE
                ORDER BY u.full_name, l.lecturer_code
                """,
                (rs, rowNum) -> new LecturerOption(
                        rs.getLong("user_id"),
                        rs.getString("lecturer_code"),
                        rs.getString("full_name")
                )
        );
    }

    @PreAuthorize("hasRole('DEAN')")
    @Transactional
    public ReviewerAssignment create(ReviewerAssignmentForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Thông tin phân công không hợp lệ");
        }

        Long registrationId = requirePositiveId(
                form.getRegistrationId(), "Mã hồ sơ đăng ký");
        Long lecturerId = requirePositiveId(
                form.getLecturerId(), "Mã giảng viên");
        String assignmentType = normalizeType(form.getAssignmentType());

        lockRegistration(registrationId);
        RegistrationContext context = requireEligibleRegistration(registrationId);
        requireActiveLecturer(lecturerId);
        requireNotSupervisor(context.topicId(), lecturerId);
        requireCorrectAssignmentRole(context, lecturerId, assignmentType);

        if (REVIEWER.equals(assignmentType)
                && assignments.existsByRegistrationIdAndAssignmentType(
                        registrationId, REVIEWER)) {
            throw new IllegalStateException(
                    "Hồ sơ đã có giảng viên phản biện");
        }

        if (assignments.existsByRegistrationIdAndLecturerId(
                registrationId, lecturerId)) {
            throw new IllegalStateException(
                    "Giảng viên đã được phân công chấm hồ sơ này");
        }

        ReviewerAssignment assignment = new ReviewerAssignment();
        assignment.setRegistrationId(registrationId);
        assignment.setLecturerId(lecturerId);
        assignment.setAssignmentType(assignmentType);
        assignment.setAssignedBy(currentUser.getCurrentUserId());
        assignment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        try {
            return assignments.saveAndFlush(assignment);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Không thể lưu phân công do dữ liệu đã thay đổi hoặc bị trùng", ex);
        }
    }

    @PreAuthorize("hasRole('DEAN')")
    @Transactional
    public int assignCouncilMembers(Long registrationId) {
        registrationId = requirePositiveId(registrationId, "Mã hồ sơ đăng ký");
        lockRegistration(registrationId);
        RegistrationContext context = requireEligibleRegistration(registrationId);

        List<Long> councilMembers = jdbc.queryForList(
                """
                SELECT lecturer_id
                FROM council_members
                WHERE council_id = ?
                ORDER BY slot_no
                """,
                Long.class,
                context.councilId()
        );

        if (councilMembers.isEmpty()) {
            throw new IllegalStateException("Hội đồng chưa có thành viên");
        }

        Map<Long, ReviewerAssignment> existingByLecturer = new HashMap<>();
        for (ReviewerAssignment assignment
                : assignments.findByRegistrationId(registrationId)) {
            existingByLecturer.put(assignment.getLecturerId(), assignment);
        }

        for (Long lecturerId : councilMembers) {
            requireActiveLecturer(lecturerId);
            requireNotSupervisor(context.topicId(), lecturerId);

            ReviewerAssignment existing = existingByLecturer.get(lecturerId);
            if (existing != null && !COUNCIL.equals(existing.getAssignmentType())) {
                throw new IllegalStateException(
                        "Một thành viên hội đồng đang được phân công với vai trò phản biện");
            }
        }

        Long actorId = currentUser.getCurrentUserId();
        int created = 0;

        for (Long lecturerId : councilMembers) {
            if (existingByLecturer.containsKey(lecturerId)) {
                continue;
            }

            ReviewerAssignment assignment = new ReviewerAssignment();
            assignment.setRegistrationId(registrationId);
            assignment.setLecturerId(lecturerId);
            assignment.setAssignmentType(COUNCIL);
            assignment.setAssignedBy(actorId);
            assignment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            assignments.save(assignment);
            created++;
        }

        if (created == 0) {
            throw new IllegalStateException(
                    "Tất cả thành viên hội đồng đã được phân công chấm");
        }

        try {
            assignments.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Không thể đồng bộ phân công do dữ liệu đã thay đổi hoặc bị trùng", ex);
        }

        return created;
    }

    @PreAuthorize("hasRole('DEAN')")
    @Transactional
    public void delete(Long assignmentId) {
        assignmentId = requirePositiveId(assignmentId, "Mã phân công");

        ReviewerAssignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy phân công chấm điểm"));

        if (evaluations.existsByAssignmentId(assignmentId)) {
            throw new IllegalStateException(
                    "Không thể xóa phân công đã có phiếu chấm");
        }

        assignments.delete(assignment);
        assignments.flush();
    }

    private RegistrationContext requireEligibleRegistration(Long registrationId) {
        List<RegistrationContext> rows = jdbc.query(
                """
                SELECT tr.topic_id, tr.status AS registration_status,
                       rr.round_type, d.council_id,
                       c.status AS council_status,
                       d.status AS defense_status,
                       (SELECT COUNT(*) FROM reports r
                        WHERE r.registration_id = tr.id) AS report_count
                FROM topic_registrations tr
                JOIN registration_rounds rr ON rr.id = tr.round_id
                LEFT JOIN defenses d ON d.registration_id = tr.id
                LEFT JOIN councils c ON c.id = d.council_id
                WHERE tr.id = ?
                """,
                (rs, rowNum) -> new RegistrationContext(
                        rs.getLong("topic_id"),
                        rs.getString("registration_status"),
                        rs.getString("round_type"),
                        (Long) rs.getObject("council_id"),
                        rs.getString("council_status"),
                        rs.getString("defense_status"),
                        rs.getLong("report_count")
                ),
                registrationId
        );

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Hồ sơ đăng ký không tồn tại");
        }

        RegistrationContext context = rows.get(0);
        if (!"APPROVED".equals(context.registrationStatus())) {
            throw new IllegalStateException("Hồ sơ đăng ký chưa được duyệt");
        }
        if (context.reportCount() == 0) {
            throw new IllegalStateException("Nhóm chưa nộp báo cáo");
        }
        if (context.councilId() == null
                || "CANCELLED".equals(context.defenseStatus())) {
            throw new IllegalStateException("Hồ sơ chưa có lịch bảo vệ hợp lệ");
        }
        if (!"ACTIVE".equals(context.councilStatus())) {
            throw new IllegalStateException("Hội đồng chưa được kích hoạt");
        }

        return context;
    }

    private void lockRegistration(Long registrationId) {
        List<Long> lockedIds = jdbc.queryForList(
                """
                SELECT id
                FROM topic_registrations
                WHERE id = ?
                FOR UPDATE
                """,
                Long.class,
                registrationId
        );

        if (lockedIds.isEmpty()) {
            throw new IllegalArgumentException("Hồ sơ đăng ký không tồn tại");
        }
    }

    private void requireActiveLecturer(Long lecturerId) {
        Long count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM lecturers l
                JOIN users u ON u.id = l.user_id
                WHERE l.user_id = ? AND u.enabled = TRUE
                """,
                Long.class,
                lecturerId
        );

        if (count == null || count == 0) {
            throw new IllegalArgumentException(
                    "Giảng viên không tồn tại hoặc đã bị khóa");
        }
    }

    private void requireNotSupervisor(Long topicId, Long lecturerId) {
        Long count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM topic_supervisors
                WHERE topic_id = ? AND lecturer_id = ?
                """,
                Long.class,
                topicId,
                lecturerId
        );

        if (count != null && count > 0) {
            throw new IllegalStateException(
                    "Giảng viên hướng dẫn không được phân công chấm đề tài này");
        }
    }

    private void requireCorrectAssignmentRole(
            RegistrationContext context,
            Long lecturerId,
            String assignmentType) {

        Long membershipCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM council_members
                WHERE council_id = ? AND lecturer_id = ?
                """,
                Long.class,
                context.councilId(),
                lecturerId
        );
        boolean isCouncilMember = membershipCount != null && membershipCount > 0;

        if (COUNCIL.equals(assignmentType) && !isCouncilMember) {
            throw new IllegalStateException(
                    "Phân công hội đồng chỉ áp dụng cho thành viên của hội đồng đã gắn với lịch bảo vệ");
        }

        if (REVIEWER.equals(assignmentType)) {
            if (isCouncilMember) {
                throw new IllegalStateException(
                        "Giảng viên phản biện phải độc lập với hội đồng");
            }

            if (!"SPECIALIZATION".equals(context.roundType())
                    && !"THESIS".equals(context.roundType())) {
                throw new IllegalStateException(
                        "Chỉ đợt đồ án chuyên ngành hoặc khóa luận mới phân công phản biện");
            }
        }
    }

    private String normalizeType(String assignmentType) {
        if (assignmentType == null) {
            throw new IllegalArgumentException("Loại phân công không hợp lệ");
        }

        String normalized = assignmentType.trim().toUpperCase(Locale.ROOT);
        if (!REVIEWER.equals(normalized) && !COUNCIL.equals(normalized)) {
            throw new IllegalArgumentException("Loại phân công không hợp lệ");
        }
        return normalized;
    }

    private Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ");
        }
        return id;
    }
}
