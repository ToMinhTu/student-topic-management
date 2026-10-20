
package com.example.student_topic_management.registration.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.student_topic_management.registration.dto.TopicView;

@Repository("registrationTopicRepository")
public class TopicRepository {

    private final JdbcTemplate jdbc;

    public TopicRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // C03.1 - Lay danh sach de tai da cong bo.
    public List<TopicView> findPublishedTopics(Long roundId) {

        String sql = """
            SELECT
                t.id,
                t.round_id,
                t.code,
                t.title,
                t.description,
                t.requirements,
                t.status,
                d.name AS department_name,
                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM topic_registrations tr
                        WHERE tr.topic_id = t.id
                          AND tr.status IN ('PENDING', 'APPROVED')
                    ) THEN 0
                    ELSE 1
                END AS available
            FROM topics t
            JOIN departments d
                ON d.id = t.department_id
            WHERE t.status = 'PUBLISHED'
              AND t.round_id = ?
            ORDER BY t.code ASC
            """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> new TopicView(
                        rs.getLong("id"),
                        rs.getLong("round_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("requirements"),
                        rs.getString("status"),
                        rs.getString("department_name"),
                        rs.getBoolean("available")
                ),
                roundId
        );
    }
}
