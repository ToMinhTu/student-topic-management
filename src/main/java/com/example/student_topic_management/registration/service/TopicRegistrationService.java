
package com.example.student_topic_management.registration.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Clock;

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

    // =====================================================
    // C03 - DANG KY DE TAI SINH VIEN
    // =====================================================

    @Transactional
    public void registerTopic(Long groupId, Long topicId) {

        // 1. KIEM TRA THAM SO
        if (groupId == null || groupId <= 0
                || topicId == null || topicId <= 0) {

            throw new IllegalArgumentException(
                    "Ma nhom hoac ma de tai khong hop le"
            );
        }

        // 2. LAY SINH VIEN DANG DANG NHAP
        Long studentId =
                studentAccess.requireCurrentStudentId();

        // 3. XAC DINH DOT DANG KY CUA NHOM
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
                    "Nhom khong ton tai"
            );
        }

        Long roundId = groups.get(0);

        // 4. KHOA DOT DANG KY
        roundAccess.lockRound(roundId);

        // 5. KIEM TRA THOI GIAN DANG KY
        roundAccess.requireStudentWindow(
                roundId,
                clock.instant()
        );

        // 6. KHOA SINH VIEN DANG THAO TAC
        studentAccess.lockEnabledStudent(studentId);

        // 7. KHOA NHOM VA KIEM TRA TRANG THAI
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
                    "Nhom khong o trang thai cho phep"
            );
        }

        // 8. KIEM TRA QUYEN TRUONG NHOM
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

        // 9. KIEM TRA SO LUONG THANH VIEN
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
                SELECT
                    group_min_size,
                    group_max_size
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

        // 10. KIEM TRA DE TAI DA CONG BO
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

        // 11. KIEM TRA NHOM DA TUNG DANG KY CHUA
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
                    "Nhom da tung dang ky de tai"
            );
        }

        // 12. KIEM TRA DE TAI CO BI GIU CHO KHONG
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

        // =====================================================
        // 13. TAO HO SO DANG KY VA LAY ID
        // =====================================================

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

        Long registrationId = generatedId.longValue();

        // =====================================================
        // 14. LUU SNAPSHOT THANH VIEN
        // =====================================================

        // Ghi lai danh sach sinh vien dang hoat dong
        // tai thoi diem nop dang ky.

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

        // Neu snapshot khong du so thanh vien,
        // rollback toan bo giao dich.

        if (snapshotCount != memberCount) {

            throw new IllegalStateException(
                    "Luu danh sach thanh vien dang ky that bai"
            );
        }

        // =====================================================
        // 15. GHI NHAT KY DANG KY
        // =====================================================

        jdbc.update(
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
                "REGISTRATION_SUBMITTED",
                "TOPIC_REGISTRATION",
                registrationId,
                "Group ID: " + groupId
                        + ", Topic ID: " + topicId
        );

    }
}
