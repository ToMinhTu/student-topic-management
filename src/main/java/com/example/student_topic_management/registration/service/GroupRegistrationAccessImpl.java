
package com.example.student_topic_management.registration.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class GroupRegistrationAccessImpl
        implements GroupRegistrationAccess {

    private final JdbcTemplate jdbc;

    public GroupRegistrationAccessImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void requireNoActiveRegistration(Long groupId) {

        if (groupId == null || groupId <= 0) {
            throw new IllegalArgumentException(
                    "Ma nhom khong hop le");
        }

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM topic_registrations
                WHERE group_id = ?
                  AND status IN ('PENDING', 'APPROVED')
                """,
                Integer.class,
                groupId
        );

        if (count != null && count > 0) {
            throw new IllegalStateException(
                    "Nhom da co dang ky de tai " +
                    "dang cho duyet hoac da duoc duyet"
            );
        }
    }
}
