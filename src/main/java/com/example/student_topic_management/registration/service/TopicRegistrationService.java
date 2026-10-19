
package com.example.student_topic_management.registration.service;

import java.time.Clock;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicRegistrationService {

    private final JdbcTemplate jdbc;
    private final StudentAccess studentAccess;
    private final RoundAccess roundAccess;
    private final Clock clock;

    public TopicRegistrationService(
            JdbcTemplate jdbc,
            StudentAccess studentAccess,
            RoundAccess roundAccess,
            Clock clock) {

        this.jdbc = jdbc;
        this.studentAccess = studentAccess;
        this.roundAccess = roundAccess;
        this.clock = clock;
    }

    @Transactional
    public void registerTopic(Long groupId, Long topicId) {

        if (groupId == null || groupId <= 0
                || topicId == null || topicId <= 0) {
            throw new IllegalArgumentException(
                    "Ma nhom hoac ma de tai khong hop le");
        }

        // 1. Xac dinh sinh vien dang dang nhap
        Long studentId = studentAccess.requireCurrentStudentId();

        // 2. Xac dinh dot dang ky cua nhom
        var groups = jdbc.queryForList(
                """
                SELECT round_id
                FROM student_groups
                WHERE id = ?
                """,
                Long.class,
                groupId
        );

        if (groups.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nhom khong ton tai");
        }

        Long roundId = groups.get(0);

        // 3. Khoa dot dang ky truoc
        roundAccess.lockRound(roundId);

        // 4. Kiem tra thoi gian dang ky
        roundAccess.requireStudentWindow(
                roundId,
                clock.instant()
        );

        // 5. Khoa sinh vien dang thao tac
        studentAccess.lockEnabledStudent(studentId);

        // 6. Khoa nhom va kiem tra trang thai
        var lockedGroups = jdbc.queryForList(
                """
                SELECT status
                FROM student_groups
                WHERE id = ?
                  AND round_id = ?
                FOR UPDATE
                """,
                String.class,
                groupId,
                roundId
        );

        if (lockedGroups.isEmpty()
                || !"FORMING".equals(lockedGroups.get(0))) {
            throw new IllegalStateException(
                    "Nhom khong o trang thai cho phep");
        }

        // 7. Chi truong nhom moi duoc dang ky
        Integer leaderCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                  AND student_id = ?
                  AND slot_no = 1
                  AND left_at IS NULL
                """,
                Integer.class,
                groupId,
                studentId
        );

        if (leaderCount == null || leaderCount != 1) {
            throw new IllegalStateException(
                    "Chi truong nhom moi duoc dang ky de tai");
        }

        // 8. Kiem tra so luong thanh vien
        Integer memberCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                  AND left_at IS NULL
                """,
                Integer.class,
                groupId
        );

        var limits = jdbc.query(
                """
                SELECT group_min_size, group_max_size
                FROM registration_rounds
                WHERE id = ?
                """,
                (rs, rowNum) -> new int[] {
                        rs.getInt("group_min_size"),
                        rs.getInt("group_max_size")
                },
                roundId
        );

        if (limits.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay dot dang ky");
        }

        int[] limit = limits.get(0);

        if (memberCount == null
                || memberCount < limit[0]
                || memberCount > limit[1]) {
            throw new IllegalStateException(
                    "So thanh vien nhom khong hop le");
        }

        // 9. Kiem tra de tai da cong bo, dung dot
        Integer validTopic = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM topics
                WHERE id = ?
                  AND round_id = ?
                  AND status = 'PUBLISHED'
                """,
                Integer.class,
                topicId,
                roundId
        );

        if (validTopic == null || validTopic != 1) {
            throw new IllegalStateException(
                    "De tai khong hop le hoac chua cong bo");
        }

        // 10. DB quy dinh UNIQUE(group_id), nen nhom
        // khong duoc co bat ky dang ky nao truoc do.
        Integer groupRegistrations = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM topic_registrations
                WHERE group_id = ?
                """,
                Integer.class,
                groupId
        );

        if (groupRegistrations != null
                && groupRegistrations > 0) {
            throw new IllegalStateException(
                    "Nhom da tung dang ky de tai");
        }

        // 11. Kiem tra de tai chua bi giu cho
        Integer topicRegistrations = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM topic_registrations
                WHERE topic_id = ?
                  AND status IN ('PENDING', 'APPROVED')
                """,
                Integer.class,
                topicId
        );

        if (topicRegistrations != null
                && topicRegistrations > 0) {
            throw new IllegalStateException(
                    "De tai da duoc nhom khac dang ky");
        }

        // 12. Luu dang ky cho duyet
        try {
            jdbc.update(
                    """
                    INSERT INTO topic_registrations
                        (group_id, topic_id, round_id,
                         status, submitted_by)
                    VALUES (?, ?, ?, 'PENDING', ?)
                    """,
                    groupId,
                    topicId,
                    roundId,
                    studentId
            );
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Nhom hoac de tai da duoc dang ky", ex);
        }
    }
}
