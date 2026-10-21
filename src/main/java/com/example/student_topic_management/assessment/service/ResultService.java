package com.example.student_topic_management.assessment.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.assessment.dto.ResultForm;
import com.example.student_topic_management.assessment.entity.Result;
import com.example.student_topic_management.assessment.policy.GradePolicy;
import com.example.student_topic_management.assessment.policy.ResultRequirementPolicy;
import com.example.student_topic_management.assessment.policy.ResultRequirementPolicy.Assignment;
import com.example.student_topic_management.assessment.policy.ResultRequirementPolicy.SubmittedEvaluation;
import com.example.student_topic_management.assessment.repository.ResultRepository;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;

@Service
public class ResultService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final String RESULT_VIEW_SELECT = """
            SELECT r.id, r.registration_id, tr.round_id, sg.name AS group_name,
                   t.code AS topic_code, t.title AS topic_title,
                   r.final_score, r.component_count, r.conclusion,
                   r.confirmed_by, confirmer.full_name AS confirmer_name,
                   r.confirmed_at, r.published_at
            FROM results r
            JOIN topic_registrations tr ON tr.id = r.registration_id
            JOIN student_groups sg ON sg.id = tr.group_id
            JOIN topics t ON t.id = tr.topic_id
            JOIN users confirmer ON confirmer.id = r.confirmed_by
            """;

    private final ResultRepository resultRepository;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbc;
    private final GradePolicy gradePolicy;
    private final ResultRequirementPolicy requirementPolicy;
    private final Clock clock;
    private final String publicBaseUrl;

    public ResultService(
            ResultRepository resultRepository,
            CurrentUserService currentUserService,
            JdbcTemplate jdbc,
            GradePolicy gradePolicy,
            ResultRequirementPolicy requirementPolicy,
            Clock clock,
            @Value("${app.public-base-url:http://localhost:8080}") String publicBaseUrl) {
        this.resultRepository = resultRepository;
        this.currentUserService = currentUserService;
        this.jdbc = jdbc;
        this.gradePolicy = gradePolicy;
        this.requirementPolicy = requirementPolicy;
        this.clock = clock;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public record ResultView(
            Long id,
            Long registrationId,
            Long roundId,
            String groupName,
            String topicCode,
            String topicTitle,
            BigDecimal finalScore,
            Integer componentCount,
            String conclusion,
            Long confirmedBy,
            String confirmerName,
            LocalDateTime confirmedAt,
            LocalDateTime publishedAt) {
    }

    public record ConfirmationCandidate(
            Long registrationId,
            Long roundId,
            String groupName,
            String topicCode,
            String topicTitle,
            int assignmentCount,
            int submittedCount) {
        public boolean ready() {
            return assignmentCount > 0 && assignmentCount == submittedCount;
        }
    }

    private record RegistrationContext(
            Long registrationId,
            Long roundId,
            Long topicId,
            String status,
            String roundType,
            LocalDateTime reviewerDueAt) {
    }

    private record DefenseContext(Long id, Long councilId, LocalDateTime scheduledAt, String status) {
    }

    private record Recipient(Long studentId, String email, String fullName) {
    }

    @Transactional(readOnly = true)
    public List<ResultView> findVisibleResults() {
        Long actorId = currentUserService.getCurrentUserId();
        // STUDENT luôn bị giới hạn vào kết quả đã công bố của chính snapshot nhóm,
        // kể cả khi tài khoản vô tình được gán thêm vai trò khác.
        if (hasRole("STUDENT")) {
            return queryResultViews(RESULT_VIEW_SELECT + """
                    WHERE r.published_at IS NOT NULL
                      AND EXISTS (
                        SELECT 1 FROM registration_members rm
                        WHERE rm.registration_id = r.registration_id
                          AND rm.student_id = ?)
                    ORDER BY r.published_at DESC
                    """, actorId);
        }
        if (hasRole("DEAN")) {
            return queryResultViews(RESULT_VIEW_SELECT + " ORDER BY r.confirmed_at DESC");
        }
        if (hasRole("LECTURER")) {
            return queryResultViews(RESULT_VIEW_SELECT + """
                    WHERE EXISTS (
                        SELECT 1 FROM reviewer_assignments ra
                        WHERE ra.registration_id = r.registration_id
                          AND ra.lecturer_id = ?)
                    ORDER BY r.confirmed_at DESC
                    """, actorId);
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public ResultView findVisibleById(Long id) {
        Long actorId = currentUserService.getCurrentUserId();
        String permission;
        Object[] parameters;
        if (hasRole("STUDENT")) {
            permission = """
                    AND r.published_at IS NOT NULL
                    AND EXISTS (SELECT 1 FROM registration_members rm
                                WHERE rm.registration_id = r.registration_id
                                  AND rm.student_id = ?)
                    """;
            parameters = new Object[] { id, actorId };
        } else if (hasRole("DEAN")) {
            permission = "";
            parameters = new Object[] { id };
        } else if (hasRole("LECTURER")) {
            permission = """
                    AND EXISTS (SELECT 1 FROM reviewer_assignments ra
                                WHERE ra.registration_id = r.registration_id
                                  AND ra.lecturer_id = ?)
                    """;
            parameters = new Object[] { id, actorId };
        } else {
            throw new IllegalStateException("Bạn không có quyền xem kết quả này");
        }

        List<ResultView> rows = queryResultViews(
                RESULT_VIEW_SELECT + " WHERE r.id = ? " + permission,
                parameters);
        if (rows.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy kết quả hoặc bạn không có quyền xem");
        }
        return rows.get(0);
    }

    @Transactional(readOnly = true)
    public List<ConfirmationCandidate> findConfirmationCandidates() {
        Long chairId = currentUserService.getCurrentUserId();
        return jdbc.query("""
                SELECT tr.id AS registration_id, tr.round_id,
                       sg.name AS group_name, t.code AS topic_code, t.title AS topic_title,
                       COUNT(DISTINCT ra.id) AS assignment_count,
                       COUNT(DISTINCT CASE WHEN e.submitted_at IS NOT NULL THEN e.id END) AS submitted_count
                FROM topic_registrations tr
                JOIN student_groups sg ON sg.id = tr.group_id
                JOIN topics t ON t.id = tr.topic_id
                JOIN defenses d ON d.registration_id = tr.id AND d.status = 'DONE'
                JOIN council_members cm ON cm.council_id = d.council_id
                                            AND cm.slot_no = 1
                                            AND cm.lecturer_id = ?
                LEFT JOIN reviewer_assignments ra ON ra.registration_id = tr.id
                LEFT JOIN evaluations e ON e.assignment_id = ra.id
                LEFT JOIN results r ON r.registration_id = tr.id
                WHERE tr.status = 'APPROVED' AND r.id IS NULL
                GROUP BY tr.id, tr.round_id, sg.name, t.code, t.title
                ORDER BY tr.id
                """, (rs, rowNum) -> new ConfirmationCandidate(
                        rs.getLong("registration_id"),
                        rs.getLong("round_id"),
                        rs.getString("group_name"),
                        rs.getString("topic_code"),
                        rs.getString("topic_title"),
                        rs.getInt("assignment_count"),
                        rs.getInt("submitted_count")), chairId);
    }

    @Transactional
    public Result consolidateResult(ResultForm form) {
        AppUser actor = requireEnabledCurrentUser();
        if (!hasRole("LECTURER") || hasRole("STUDENT")) {
            throw new IllegalStateException("Chỉ giảng viên là chủ tịch hội đồng được xác nhận kết quả");
        }

        RegistrationContext context = findRegistrationContext(form.getRegistrationId());
        lockOne("SELECT id FROM registration_rounds WHERE id = ? FOR UPDATE", context.roundId(),
                "Không tìm thấy đợt đăng ký");
        lockOne("SELECT id FROM topic_registrations WHERE id = ? FOR UPDATE", context.registrationId(),
                "Không tìm thấy hồ sơ đăng ký");

        DefenseContext defense = findDefenseContext(context.registrationId());
        lockOne("SELECT id FROM councils WHERE id = ? FOR UPDATE", defense.councilId(),
                "Không tìm thấy hội đồng");

        if (!"APPROVED".equals(context.status())) {
            throw new IllegalStateException("Chỉ hồ sơ đã được duyệt mới được xác nhận kết quả");
        }
        if (!"DONE".equals(defense.status())) {
            throw new IllegalStateException("Phải kết thúc buổi bảo vệ trước khi xác nhận kết quả");
        }
        if (defense.scheduledAt().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalStateException("Chưa đến thời gian bảo vệ");
        }
        requireChair(defense.councilId(), actor.getId());
        if (resultRepository.existsByRegistrationId(context.registrationId())) {
            throw new IllegalStateException("Hồ sơ này đã được xác nhận kết quả");
        }

        Integer snapshotCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registration_members WHERE registration_id = ?",
                Integer.class, context.registrationId());
        if (snapshotCount == null || snapshotCount == 0) {
            throw new IllegalStateException("Hồ sơ chưa có danh sách thành viên được chụp tại lúc đăng ký");
        }

        Set<Long> councilMembers = new HashSet<>(jdbc.queryForList(
                "SELECT lecturer_id FROM council_members WHERE council_id = ?",
                Long.class, defense.councilId()));
        List<Assignment> assignments = jdbc.query("""
                SELECT id, lecturer_id, assignment_type
                FROM reviewer_assignments
                WHERE registration_id = ?
                ORDER BY id
                """, (rs, rowNum) -> new Assignment(
                        rs.getLong("id"), rs.getLong("lecturer_id"), rs.getString("assignment_type")),
                context.registrationId());

        boolean reviewerRequired = "SPECIALIZATION".equals(context.roundType())
                || "THESIS".equals(context.roundType());
        requirementPolicy.requireValidAssignments(councilMembers, assignments, reviewerRequired);

        Integer supervisorGraders = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM reviewer_assignments ra
                JOIN topic_supervisors ts ON ts.topic_id = ? AND ts.lecturer_id = ra.lecturer_id
                WHERE ra.registration_id = ?
                """, Integer.class, context.topicId(), context.registrationId());
        if (supervisorGraders != null && supervisorGraders > 0) {
            throw new IllegalStateException("Giảng viên hướng dẫn không được chấm hồ sơ mình hướng dẫn");
        }

        List<SubmittedEvaluation> evaluations = jdbc.query("""
                SELECT ra.id AS assignment_id, ra.assignment_type,
                       e.score, e.comments, e.submitted_at
                FROM reviewer_assignments ra
                LEFT JOIN evaluations e ON e.assignment_id = ra.id
                WHERE ra.registration_id = ?
                ORDER BY ra.id
                """, (rs, rowNum) -> new SubmittedEvaluation(
                        rs.getLong("assignment_id"),
                        rs.getString("assignment_type"),
                        rs.getBigDecimal("score"),
                        rs.getString("comments"),
                        rs.getTimestamp("submitted_at") == null
                                ? null : rs.getTimestamp("submitted_at").toLocalDateTime()),
                context.registrationId());

        List<BigDecimal> scores = requirementPolicy.requireCompleteEvaluations(
                assignments, evaluations, context.reviewerDueAt());
        BigDecimal average = gradePolicy.average(scores);

        Result result = new Result();
        result.setRegistrationId(context.registrationId());
        result.setFinalScore(average);
        result.setComponentCount(scores.size());
        result.setConclusion(form.getConclusion().trim());
        result.setConfirmedBy(actor.getId());
        result.setConfirmedAt(LocalDateTime.now(clock));

        try {
            resultRepository.saveAndFlush(result);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Hồ sơ này đã được xác nhận kết quả", ex);
        }

        insertAudit(actor.getId(), "RESULT_CONFIRMED", "RESULT", result.getId(),
                "Xác nhận kết quả hồ sơ #" + context.registrationId()
                        + ", điểm trung bình " + average.toPlainString());
        return result;
    }

    @Transactional
    public boolean publishResult(Long resultId) {
        if (!hasRole("DEAN") || hasRole("STUDENT")) {
            throw new IllegalStateException("Chỉ Trưởng khoa được công bố kết quả");
        }
        AppUser dean = requireEnabledCurrentUser();

        List<long[]> contextRows = jdbc.query("""
                SELECT tr.round_id, r.registration_id
                FROM results r
                JOIN topic_registrations tr ON tr.id = r.registration_id
                WHERE r.id = ?
                """, (rs, rowNum) -> new long[] {
                        rs.getLong("round_id"), rs.getLong("registration_id") }, resultId);
        if (contextRows.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy kết quả");
        }
        long[] context = contextRows.get(0);
        lockOne("SELECT id FROM registration_rounds WHERE id = ? FOR UPDATE", context[0],
                "Không tìm thấy đợt đăng ký");
        lockOne("SELECT id FROM topic_registrations WHERE id = ? FOR UPDATE", context[1],
                "Không tìm thấy hồ sơ đăng ký");
        lockOne("SELECT id FROM results WHERE id = ? FOR UPDATE", resultId,
                "Không tìm thấy kết quả");

        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy kết quả"));
        if (result.getPublishedAt() != null) {
            return false;
        }

        List<Recipient> recipients = jdbc.query("""
                SELECT rm.student_id, u.email, u.full_name
                FROM registration_members rm
                JOIN users u ON u.id = rm.student_id
                WHERE rm.registration_id = ?
                ORDER BY rm.slot_no
                """, (rs, rowNum) -> new Recipient(
                        rs.getLong("student_id"), rs.getString("email"), rs.getString("full_name")),
                result.getRegistrationId());
        if (recipients.isEmpty()) {
            throw new IllegalStateException("Không thể công bố vì hồ sơ chưa có danh sách thành viên");
        }
        for (Recipient recipient : recipients) {
            if (!isValidEmail(recipient.email())) {
                throw new IllegalStateException(
                        "Email của sinh viên #" + recipient.studentId() + " không hợp lệ");
            }
        }

        LocalDateTime publishedAt = LocalDateTime.now(clock);
        if (publishedAt.isBefore(result.getConfirmedAt())) {
            publishedAt = result.getConfirmedAt();
        }
        result.setPublishedBy(dean.getId());
        result.setPublishedAt(publishedAt);
        resultRepository.saveAndFlush(result);

        String subject = "Kết quả bảo vệ đã được công bố";
        String link = publicBaseUrl + "/results/" + result.getId();
        for (Recipient recipient : recipients) {
            String body = "Xin chào " + recipient.fullName()
                    + ",\n\nKết quả bảo vệ của nhóm đã được công bố. Vui lòng đăng nhập để xem tại:\n"
                    + link + "\n";
            int inserted = jdbc.update("""
                    INSERT INTO email_outbox
                        (result_id, student_id, recipient_email, subject, body,
                         status, attempts, created_at)
                    VALUES (?, ?, ?, ?, ?, 'PENDING', 0, ?)
                    """, result.getId(), recipient.studentId(), recipient.email(), subject, body, publishedAt);
            if (inserted != 1) {
                throw new IllegalStateException("Không thể tạo thông báo kết quả cho sinh viên");
            }
        }

        insertAudit(dean.getId(), "RESULT_PUBLISHED", "RESULT", result.getId(),
                "Công bố kết quả hồ sơ #" + result.getRegistrationId()
                        + " cho " + recipients.size() + " sinh viên");
        return true;
    }

    private RegistrationContext findRegistrationContext(Long registrationId) {
        List<RegistrationContext> rows = jdbc.query("""
                SELECT tr.id, tr.round_id, tr.topic_id, tr.status,
                       rr.round_type, rr.reviewer_due_at
                FROM topic_registrations tr
                JOIN registration_rounds rr ON rr.id = tr.round_id
                WHERE tr.id = ?
                """, (rs, rowNum) -> new RegistrationContext(
                        rs.getLong("id"),
                        rs.getLong("round_id"),
                        rs.getLong("topic_id"),
                        rs.getString("status"),
                        rs.getString("round_type"),
                        rs.getTimestamp("reviewer_due_at") == null
                                ? null : rs.getTimestamp("reviewer_due_at").toLocalDateTime()),
                registrationId);
        if (rows.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy hồ sơ đăng ký #" + registrationId);
        }
        return rows.get(0);
    }

    private DefenseContext findDefenseContext(Long registrationId) {
        List<DefenseContext> rows = jdbc.query("""
                SELECT id, council_id, scheduled_at, status
                FROM defenses WHERE registration_id = ? FOR UPDATE
                """, (rs, rowNum) -> new DefenseContext(
                        rs.getLong("id"),
                        rs.getLong("council_id"),
                        rs.getTimestamp("scheduled_at").toLocalDateTime(),
                        rs.getString("status")), registrationId);
        if (rows.isEmpty()) {
            throw new IllegalStateException("Hồ sơ chưa có lịch bảo vệ");
        }
        return rows.get(0);
    }

    private void requireChair(Long councilId, Long actorId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM council_members
                WHERE council_id = ? AND lecturer_id = ? AND slot_no = 1
                """, Integer.class, councilId, actorId);
        if (count == null || count != 1) {
            throw new IllegalStateException("Chỉ Chủ tịch hội đồng được xác nhận kết quả");
        }
    }

    private void lockOne(String sql, Object id, String notFoundMessage) {
        List<Long> rows = jdbc.query(sql, (rs, rowNum) -> rs.getLong(1), id);
        if (rows.isEmpty()) {
            throw new IllegalStateException(notFoundMessage);
        }
    }

    private void insertAudit(Long actorId, String action, String entityType, Long entityId, String details) {
        int inserted = jdbc.update("""
                INSERT INTO audit_logs (actor_id, action, entity_type, entity_id, details, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """, actorId, action, entityType, entityId, details, LocalDateTime.now(clock));
        if (inserted != 1) {
            throw new IllegalStateException("Không thể ghi nhật ký hệ thống");
        }
    }

    private AppUser requireEnabledCurrentUser() {
        AppUser actor = currentUserService.getCurrentUser();
        if (!Boolean.TRUE.equals(actor.getEnabled())) {
            throw new IllegalStateException("Tài khoản hiện tại đã bị vô hiệu hóa");
        }
        return actor;
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> ("ROLE_" + role.toUpperCase(Locale.ROOT))
                        .equals(authority.getAuthority()));
    }

    private boolean isValidEmail(String email) {
        return email != null && email.length() <= 254 && EMAIL_PATTERN.matcher(email).matches();
    }

    private List<ResultView> queryResultViews(String sql, Object... parameters) {
        return jdbc.query(sql, (rs, rowNum) -> new ResultView(
                rs.getLong("id"),
                rs.getLong("registration_id"),
                rs.getLong("round_id"),
                rs.getString("group_name"),
                rs.getString("topic_code"),
                rs.getString("topic_title"),
                rs.getBigDecimal("final_score"),
                rs.getInt("component_count"),
                rs.getString("conclusion"),
                rs.getLong("confirmed_by"),
                rs.getString("confirmer_name"),
                rs.getTimestamp("confirmed_at").toLocalDateTime(),
                rs.getTimestamp("published_at") == null
                        ? null : rs.getTimestamp("published_at").toLocalDateTime()), parameters);
    }
}
