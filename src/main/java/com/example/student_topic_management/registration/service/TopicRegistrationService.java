
package com.example.student_topic_management.registration.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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

    private record ExistingRegistration(
            Long id,
            Long topicId,
            Long roundId,
            String status
    ) {}

    // =====================================================
    // C03 + C04.3 - DANG KY / DANG KY LAI DE TAI
    // =====================================================

    @Transactional
    public void registerTopic(Long groupId, Long topicId) {

        // 1. Kiem tra tham so
        if (groupId == null || groupId <= 0
                || topicId == null || topicId <= 0) {

            throw new IllegalArgumentException(
                    "Ma nhom hoac ma de tai khong hop le"
            );
        }

        // 2. Lay sinh vien dang dang nhap
        Long studentId =
                studentAccess.requireCurrentStudentId();

        // 3. Xac dinh dot dang ky cua nhom
        List<Long> groups = jdbc.queryForList(
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
                    "Nhom khong ton tai"
            );
        }

        Long roundId = groups.get(0);

        // 4. Khoa dot dang ky
        roundAccess.lockRound(roundId);

        // 5. Kiem tra thoi gian dang ky
        roundAccess.requireStudentWindow(
                roundId,
                clock.instant()
        );

        // 6. Khoa sinh vien
        studentAccess.lockEnabledStudent(studentId);

        // 7. Khoa nhom
        List<String> lockedGroups = jdbc.queryForList(
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
                    "Nhom khong o trang thai cho phep"
            );
        }

        // 8. Chi truong nhom moi duoc dang ky
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
                    "Chi truong nhom moi duoc dang ky de tai"
            );
        }

        // 9. Kiem tra so luong thanh vien
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

        List<int[]> limits = jdbc.query(
                """
                SELECT group_min_size, group_max_size
                FROM registration_rounds
                WHERE id = ?
                """,
                (rs, rowNum) -> new int[]{
                        rs.getInt("group_min_size"),
                        rs.getInt("group_max_size")
                },
                roundId
        );

        if (limits.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay dot dang ky"
            );
        }

        int[] limit = limits.get(0);

        if (memberCount == null
                || memberCount < limit[0]
                || memberCount > limit[1]) {

            throw new IllegalStateException(
                    "So thanh vien nhom khong hop le"
            );
        }

        // 10. Kiem tra de tai da cong bo
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
                    "De tai khong hop le hoac chua cong bo"
            );
        }

        // 11. Khoa va tim ho so cu cua nhom
        List<ExistingRegistration> existingRows = jdbc.query(
                """
                SELECT id, topic_id, round_id, status
                FROM topic_registrations
                WHERE group_id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new ExistingRegistration(
                        rs.getLong("id"),
                        rs.getLong("topic_id"),
                        rs.getLong("round_id"),
                        rs.getString("status")
                ),
                groupId
        );

        if (existingRows.size() > 1) {
            throw new IllegalStateException(
                    "Du lieu dang ky cua nhom khong hop le"
            );
        }

        ExistingRegistration existing =
                existingRows.isEmpty()
                        ? null
                        : existingRows.get(0);

        boolean reRegister = existing != null;

        if (reRegister) {

            if (!roundId.equals(existing.roundId())) {
                throw new IllegalStateException(
                        "Ho so cu khong thuoc dot dang ky cua nhom"
                );
            }

            if (!"REJECTED".equals(existing.status())) {
                throw new IllegalStateException(
                        "Nhom da tung dang ky de tai"
                );
            }

            // Ho so cu khong duoc co du lieu nghiep vu
            Integer dependentCount = jdbc.queryForObject(
                    """
                    SELECT
                        (SELECT COUNT(*)
                         FROM reports
                         WHERE registration_id = ?)
                      + (SELECT COUNT(*)
                         FROM defenses
                         WHERE registration_id = ?)
                      + (SELECT COUNT(*)
                         FROM reviewer_assignments
                         WHERE registration_id = ?)
                      + (SELECT COUNT(*)
                         FROM results
                         WHERE registration_id = ?)
                    """,
                    Integer.class,
                    existing.id(),
                    existing.id(),
                    existing.id(),
                    existing.id()
            );

            if (dependentCount == null || dependentCount > 0) {
                throw new IllegalStateException(
                        "Ho so da co du lieu xu ly, khong the dang ky lai"
                );
            }
        }

        // 12. Kiem tra de tai co bi giu cho khong
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
                    "De tai da duoc nhom khac dang ky"
            );
        }

        // 13. Tao moi hoac cap nhat ho so
        Long registrationId;

        if (reRegister) {

            registrationId = existing.id();

            LocalDateTime now = LocalDateTime.ofInstant(
                    clock.instant(),
                    ZoneOffset.UTC
            );

            int updated;

            try {
                updated = jdbc.update(
                        """
                        UPDATE topic_registrations
                        SET topic_id = ?,
                            status = 'PENDING',
                            submitted_by = ?,
                            submitted_at = ?,
                            decided_by = NULL,
                            decided_at = NULL,
                            decision_note = NULL
                        WHERE id = ?
                          AND group_id = ?
                          AND round_id = ?
                          AND status = 'REJECTED'
                        """,
                        topicId,
                        studentId,
                        now,
                        registrationId,
                        groupId,
                        roundId
                );

            } catch (DataIntegrityViolationException ex) {
                throw new IllegalStateException(
                        "De tai da duoc nhom khac dang ky",
                        ex
                );
            }

            if (updated != 1) {
                throw new IllegalStateException(
                        "Dang ky lai de tai that bai"
                );
            }

            // Lam moi snapshot thanh vien
            jdbc.update(
                    """
                    DELETE FROM registration_members
                    WHERE registration_id = ?
                    """,
                    registrationId
            );

        } else {

            KeyHolder keyHolder = new GeneratedKeyHolder();

            try {

                jdbc.update(connection -> {

                    PreparedStatement ps =
                            connection.prepareStatement(
                                    """
                                    INSERT INTO topic_registrations
                                        (
                                            group_id,
                                            topic_id,
                                            round_id,
                                            status,
                                            submitted_by
                                        )
                                    VALUES (?, ?, ?, 'PENDING', ?)
                                    """,
                                    Statement.RETURN_GENERATED_KEYS
                            );

                    ps.setLong(1, groupId);
                    ps.setLong(2, topicId);
                    ps.setLong(3, roundId);
                    ps.setLong(4, studentId);

                    return ps;

                }, keyHolder);

            } catch (DataIntegrityViolationException ex) {
                throw new IllegalStateException(
                        "Nhom hoac de tai da duoc dang ky",
                        ex
                );
            }

            Number generatedId = keyHolder.getKey();

            if (generatedId == null) {
                throw new IllegalStateException(
                        "Khong lay duoc ma ho so dang ky"
                );
            }

            registrationId = generatedId.longValue();
        }

        // 14. Luu snapshot thanh vien moi
        int snapshotCount = jdbc.update(
                """
                INSERT INTO registration_members
                    (
                        registration_id,
                        student_id,
                        slot_no
                    )
                SELECT
                    ?,
                    gm.student_id,
                    gm.slot_no
                FROM group_members gm
                WHERE gm.group_id = ?
                  AND gm.left_at IS NULL
                """,
                registrationId,
                groupId
        );

        if (snapshotCount != memberCount) {
            throw new IllegalStateException(
                    "Luu danh sach thanh vien dang ky that bai"
            );
        }

        // 15. Ghi audit
        String action = reRegister
                ? "REGISTRATION_RESUBMITTED"
                : "REGISTRATION_SUBMITTED";

        int auditCount = jdbc.update(
                """
                INSERT INTO audit_logs
                    (
                        actor_id,
                        action,
                        entity_type,
                        entity_id,
                        details
                    )
                VALUES (?, ?, ?, ?, ?)
                """,
                studentId,
                action,
                "TOPIC_REGISTRATION",
                registrationId,
                "Group ID: " + groupId
                        + ", Topic ID: " + topicId
                        + (reRegister
                            ? ", Previous Topic ID: " + existing.topicId()
                            : "")
        );

        if (auditCount != 1) {
            throw new IllegalStateException(
                    "Ghi nhat ky dang ky that bai"
            );
        }
    }
}
