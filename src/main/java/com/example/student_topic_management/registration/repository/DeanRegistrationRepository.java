
package com.example.student_topic_management.registration.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DeanRegistrationRepository {

    private final JdbcTemplate jdbc;

    public DeanRegistrationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // C04.1 - Thong tin mot ho so dang ky
    public record RegistrationRow(
            Long id,
            Long groupId,
            String groupName,
            Long topicId,
            String topicCode,
            String topicTitle,
            Long roundId,
            String roundName,
            String status,
            LocalDateTime submittedAt
    ) {}

    // C04.1 - Danh sach dang ky co loc va phan trang
    public List<RegistrationRow> findRegistrations(
            Long roundId,
            String status,
            int page,
            int size) {

        String sql = """
                SELECT
                    tr.id,
                    tr.group_id,
                    sg.name AS group_name,
                    tr.topic_id,
                    t.code AS topic_code,
                    t.title AS topic_title,
                    tr.round_id,
                    rr.name AS round_name,
                    tr.status,
                    tr.submitted_at
                FROM topic_registrations tr
                JOIN student_groups sg
                    ON sg.id = tr.group_id
                   AND sg.round_id = tr.round_id
                JOIN topics t
                    ON t.id = tr.topic_id
                   AND t.round_id = tr.round_id
                JOIN registration_rounds rr
                    ON rr.id = tr.round_id
                WHERE (? IS NULL OR tr.round_id = ?)
                  AND (? IS NULL OR tr.status = ?)
                ORDER BY tr.submitted_at DESC, tr.id DESC
                LIMIT ? OFFSET ?
                """;

        int safeSize = Math.max(1, Math.min(size, 50));
        int safePage = Math.max(0, page);
        int offset = Math.toIntExact(
                (long) safePage * safeSize
        );

        return jdbc.query(
                sql,
                (rs, rowNum) -> new RegistrationRow(
                        rs.getLong("id"),
                        rs.getLong("group_id"),
                        rs.getString("group_name"),
                        rs.getLong("topic_id"),
                        rs.getString("topic_code"),
                        rs.getString("topic_title"),
                        rs.getLong("round_id"),
                        rs.getString("round_name"),
                        rs.getString("status"),
                        rs.getTimestamp("submitted_at").toLocalDateTime()
                ),
                roundId,
                roundId,
                status,
                status,
                safeSize,
                offset
        );
    }

    // C04.1 - Dem tong so ho so theo bo loc
    public long countRegistrations(Long roundId, String status) {

        String sql = """
                SELECT COUNT(*)
                FROM topic_registrations tr
                WHERE (? IS NULL OR tr.round_id = ?)
                  AND (? IS NULL OR tr.status = ?)
                """;

        Long total = jdbc.queryForObject(
                sql,
                Long.class,
                roundId,
                roundId,
                status,
                status
        );

        return total == null ? 0L : total;
    }
}

