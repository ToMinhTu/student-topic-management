package com.example.student_topic_management.registration.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.registration.repository.DeanRegistrationRepository.RegistrationRow;

@Service
@PreAuthorize("hasRole('LECTURER') and !hasRole('STUDENT')")
@Transactional(readOnly = true)
public class LecturerRegistrationService {
    private static final int PAGE_SIZE = 10;
    private static final String FILTER = """
            FROM topic_registrations tr
            JOIN student_groups sg ON sg.id = tr.group_id AND sg.round_id = tr.round_id
            JOIN topics t ON t.id = tr.topic_id AND t.round_id = tr.round_id
            JOIN registration_rounds rr ON rr.id = tr.round_id
            WHERE EXISTS (SELECT 1 FROM topic_supervisors ts
                          WHERE ts.topic_id = tr.topic_id AND ts.lecturer_id = ?)
              AND (? IS NULL OR tr.round_id = ?)
              AND (? IS NULL OR tr.status = ?)
            """;
    private final JdbcTemplate jdbc;
    private final CurrentUserService currentUser;

    public LecturerRegistrationService(JdbcTemplate jdbc, CurrentUserService currentUser) {
        this.jdbc = jdbc;
        this.currentUser = currentUser;
    }

    public record RoundOption(Long id, String name) {}

    public static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return null;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return Set.of("PENDING", "APPROVED", "REJECTED", "CANCELLED").contains(normalized) ? normalized : null;
    }

    public Page<RegistrationRow> findRegistrations(Long roundId, String status, int page) {
        Long lecturerId = requireLecturer();
        String selectedStatus = normalizeStatus(status);
        Long count = jdbc.queryForObject("SELECT COUNT(*) " + FILTER, Long.class,
                lecturerId, roundId, roundId, selectedStatus, selectedStatus);
        long total = count == null ? 0 : count;
        int lastPage = total == 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, (total - 1) / PAGE_SIZE);
        int currentPage = Math.min(Math.max(0, page), lastPage);
        PageRequest pageable = PageRequest.of(currentPage, PAGE_SIZE);
        if (total == 0) return new PageImpl<>(List.of(), pageable, 0);
        List<RegistrationRow> rows = jdbc.query("""
                SELECT tr.id, tr.group_id, sg.name AS group_name,
                       tr.topic_id, t.code AS topic_code, t.title AS topic_title,
                       tr.round_id, rr.name AS round_name, tr.status, tr.submitted_at
                """ + FILTER + " ORDER BY tr.submitted_at DESC, tr.id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> new RegistrationRow(rs.getLong("id"), rs.getLong("group_id"),
                        rs.getString("group_name"), rs.getLong("topic_id"), rs.getString("topic_code"),
                        rs.getString("topic_title"), rs.getLong("round_id"), rs.getString("round_name"),
                        rs.getString("status"), rs.getTimestamp("submitted_at").toLocalDateTime()),
                lecturerId, roundId, roundId, selectedStatus, selectedStatus, PAGE_SIZE, pageable.getOffset());
        return new PageImpl<>(rows, pageable, total);
    }

    public List<RoundOption> findRounds() {
        Long lecturerId = requireLecturer();
        return jdbc.query("""
                SELECT DISTINCT rr.id, rr.name FROM registration_rounds rr
                JOIN topics t ON t.round_id = rr.id
                JOIN topic_supervisors ts ON ts.topic_id = t.id
                WHERE ts.lecturer_id = ? ORDER BY rr.id DESC
                """, (rs, rowNum) -> new RoundOption(rs.getLong("id"), rs.getString("name")), lecturerId);
    }

    private Long requireLecturer() {
        AppUser actor = currentUser.getCurrentUser();
        if (actor == null || actor.getId() == null || !Boolean.TRUE.equals(actor.getEnabled())
                || actor.getRoles() == null
                || actor.getRoles().stream().noneMatch(role -> "LECTURER".equals(role.getCode()))
                || actor.getRoles().stream().anyMatch(role -> "STUDENT".equals(role.getCode()))) {
            throw new AccessDeniedException("Tài khoản không có quyền xem hồ sơ của giảng viên");
        }
        return actor.getId();
    }
}
