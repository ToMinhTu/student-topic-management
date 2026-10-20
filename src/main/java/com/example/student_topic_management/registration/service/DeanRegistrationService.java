
package com.example.student_topic_management.registration.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Clock;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;

@Service
public class DeanRegistrationService {

    private final JdbcTemplate jdbc;
    private final RoundAccess roundAccess;
    private final CurrentUserService currentUserService;
    private final Clock clock;

    public DeanRegistrationService(
            JdbcTemplate jdbc,
            RoundAccess roundAccess,
            CurrentUserService currentUserService,
            Clock clock) {

        this.jdbc = jdbc;
        this.roundAccess = roundAccess;
        this.currentUserService = currentUserService;
        this.clock = clock;
    }

    private record RegistrationInfo(
            Long groupId,
            Long topicId,
            Long roundId
    ) {}

    private record LockedRegistration(
            String status,
            Long groupId,
            Long topicId,
            Long roundId
    ) {}

    private record GroupInfo(
            String status,
            Long roundId
    ) {}

    private record TopicInfo(
            String status,
            Long roundId
    ) {}

    private record GroupLimits(
            int minSize,
            int maxSize
    ) {}

    // ==========================================
    // C04.2 - DEAN DUYET HO SO DANG KY
    // ==========================================

    @PreAuthorize("hasAnyRole('DEAN','LECTURER')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void approveRegistration(Long registrationId) {

        if (registrationId == null || registrationId <= 0) {
            throw new IllegalArgumentException(
                    "Ma ho so dang ky khong hop le"
            );
        }

        // 1. Lay tai khoan truong khoa dang dang nhap.
        AppUser dean = currentUserService.getCurrentUser();

        if (!Boolean.TRUE.equals(dean.getEnabled())
                || dean.getRoles() == null
                || dean.getRoles().stream().noneMatch(
                        role -> "DEAN".equals(role.getCode())
                )) {

            throw new IllegalStateException(
                    "Tai khoan khong co quyen duyet dang ky"
            );
        }

        Long deanId = dean.getId();

        // 2. Doc thong tin ho so ban dau de xac dinh round.
        // Sau khi khoa se kiem tra lai.
        List<RegistrationInfo> initialRegistrations =
                jdbc.query(
                        """
                        SELECT group_id, topic_id, round_id
                        FROM topic_registrations
                        WHERE id = ?
                        """,
                        (rs, rowNum) -> new RegistrationInfo(
                                rs.getLong("group_id"),
                                rs.getLong("topic_id"),
                                rs.getLong("round_id")
                        ),
                        registrationId
                );

        if (initialRegistrations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ho so dang ky khong ton tai"
            );
        }

        RegistrationInfo initial = initialRegistrations.get(0);

        Long groupId = initial.groupId();
        Long topicId = initial.topicId();
        Long roundId = initial.roundId();

        // 3. Khoa dot dang ky truoc.
        roundAccess.lockRound(roundId);

        // 4. Khoa nhom.
        List<GroupInfo> groups = jdbc.query(
                """
                SELECT status, round_id
                FROM student_groups
                WHERE id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new GroupInfo(
                        rs.getString("status"),
                        rs.getLong("round_id")
                ),
                groupId
        );

        if (groups.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay nhom sinh vien"
            );
        }

        GroupInfo group = groups.get(0);

        if (!roundId.equals(group.roundId())) {
            throw new IllegalStateException(
                    "Nhom khong thuoc dot dang ky cua ho so"
            );
        }

        if (!"FORMING".equals(group.status())) {
            throw new IllegalStateException(
                    "Nhom khong o trang thai cho phep duyet"
            );
        }

        // 5. Khoa de tai.
        List<TopicInfo> topics = jdbc.query(
                """
                SELECT status, round_id
                FROM topics
                WHERE id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new TopicInfo(
                        rs.getString("status"),
                        rs.getLong("round_id")
                ),
                topicId
        );

        if (topics.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay de tai"
            );
        }

        TopicInfo topic = topics.get(0);

        if (!roundId.equals(topic.roundId())) {
            throw new IllegalStateException(
                    "De tai khong thuoc dot dang ky cua ho so"
            );
        }

        if (!"PUBLISHED".equals(topic.status())) {
            throw new IllegalStateException(
                    "De tai chua duoc cong bo"
            );
        }

        // 6. Khoa ho so va kiem tra lai thong tin.
        List<LockedRegistration> registrations =
                jdbc.query(
                        """
                        SELECT status, group_id, topic_id, round_id
                        FROM topic_registrations
                        WHERE id = ?
                        FOR UPDATE
                        """,
                        (rs, rowNum) -> new LockedRegistration(
                                rs.getString("status"),
                                rs.getLong("group_id"),
                                rs.getLong("topic_id"),
                                rs.getLong("round_id")
                        ),
                        registrationId
                );

        if (registrations.isEmpty()) {
            throw new IllegalStateException(
                    "Ho so dang ky khong con ton tai"
            );
        }

        LockedRegistration registration = registrations.get(0);

        if (!groupId.equals(registration.groupId())
                || !topicId.equals(registration.topicId())
                || !roundId.equals(registration.roundId())) {

            throw new IllegalStateException(
                    "Thong tin ho so da thay doi"
            );
        }

        if (!"PENDING".equals(registration.status())) {
            throw new IllegalStateException(
                    "Chi co the duyet ho so dang cho duyet"
            );
        }

        // 7. Lay gioi han so luong thanh vien.
        List<GroupLimits> limits = jdbc.query(
                """
                SELECT group_min_size, group_max_size
                FROM registration_rounds
                WHERE id = ?
                """,
                (rs, rowNum) -> new GroupLimits(
                        rs.getInt("group_min_size"),
                        rs.getInt("group_max_size")
                ),
                roundId
        );

        if (limits.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay dot dang ky"
            );
        }

        GroupLimits limit = limits.get(0);

        // 8. Kiem tra so thanh vien dang hoat dong.
        Integer activeCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                  AND left_at IS NULL
                """,
                Integer.class,
                groupId
        );

        if (activeCount == null
                || activeCount < limit.minSize()
                || activeCount > limit.maxSize()) {

            throw new IllegalStateException(
                    "So thanh vien nhom khong hop le"
            );
        }

        // 9. Phai co dung mot truong nhom.
        Integer leaderCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                  AND slot_no = 1
                  AND left_at IS NULL
                """,
                Integer.class,
                groupId
        );

        if (leaderCount == null || leaderCount != 1) {
            throw new IllegalStateException(
                    "Nhom phai co dung mot truong nhom"
            );
        }

        // 10. Kiem tra snapshot khop danh sach hien tai.
        Integer snapshotCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM registration_members
                WHERE registration_id = ?
                """,
                Integer.class,
                registrationId
        );

        if (snapshotCount == null
                || !snapshotCount.equals(activeCount)) {

            throw new IllegalStateException(
                    "Danh sach thanh vien da thay doi so voi luc nop"
            );
        }

        // So sanh tung student_id va slot_no.
        Integer differentMembers = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM registration_members rm
                LEFT JOIN group_members gm
                    ON gm.group_id = ?
                   AND gm.student_id = rm.student_id
                   AND gm.slot_no = rm.slot_no
                   AND gm.left_at IS NULL
                WHERE rm.registration_id = ?
                  AND gm.id IS NULL
                """,
                Integer.class,
                groupId,
                registrationId
        );

        if (differentMembers == null || differentMembers > 0) {
            throw new IllegalStateException(
                    "Thanh vien nhom khong khop snapshot"
            );
        }

        // 11. Duyet ho so.
        LocalDateTime decidedAt = LocalDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );

        int approved = jdbc.update(
                """
                UPDATE topic_registrations
                SET status = 'APPROVED',
                    decided_by = ?,
                    decided_at = ?,
                    decision_note = NULL
                WHERE id = ?
                  AND status = 'PENDING'
                """,
                deanId,
                decidedAt,
                registrationId
        );

        if (approved != 1) {
            throw new IllegalStateException(
                    "Khong the cap nhat trang thai ho so"
            );
        }

        // 12. Khoa nhom trong cung transaction.
        int locked = jdbc.update(
                """
                UPDATE student_groups
                SET status = 'LOCKED'
                WHERE id = ?
                  AND status = 'FORMING'
                """,
                groupId
        );

        if (locked != 1) {
            throw new IllegalStateException(
                    "Khong the khoa nhom sinh vien"
            );
        }

        // 13. Ghi nhat ky duyet ho so.
        int audited = jdbc.update(
                """
                INSERT INTO audit_logs
                    (actor_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """,
                deanId,
                "REGISTRATION_APPROVED",
                "TOPIC_REGISTRATION",
                registrationId,
                "Approved registration " + registrationId
                        + ", group " + groupId
                        + ", topic " + topicId
        );

        if (audited != 1) {
            throw new IllegalStateException(
                    "Khong the ghi nhat ky duyet ho so"
            );
        }
    }

    // =====================================================
    // C04.3 - DEAN TU CHOI HO SO DANG KY DE TAI
    // =====================================================

    @PreAuthorize("hasAnyRole('DEAN','LECTURER')")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void rejectRegistration(Long registrationId, String reason) {

        // 1. Kiem tra du lieu dau vao
        if (registrationId == null || registrationId <= 0) {
            throw new IllegalArgumentException(
                    "Ma ho so dang ky khong hop le"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui long nhap ly do tu choi"
            );
        }

        String normalizedReason = reason.trim();

        if (normalizedReason.length() > 1000) {
            throw new IllegalArgumentException(
                    "Ly do tu choi khong duoc vuot qua 1000 ky tu"
            );
        }

        // 2. Kiem tra tai khoan DEAN
        AppUser dean = currentUserService.getCurrentUser();

        if (dean == null
                || !Boolean.TRUE.equals(dean.getEnabled())
                || dean.getRoles() == null
                || dean.getRoles().stream().noneMatch(
                        role -> "DEAN".equals(role.getCode())
                )) {
            throw new IllegalStateException(
                    "Tai khoan khong co quyen tu choi dang ky"
            );
        }

        Long deanId = dean.getId();

        // 3. Doc thong tin ho so ban dau
        List<RegistrationInfo> initialRegistrations = jdbc.query(
                """
                SELECT group_id, topic_id, round_id
                FROM topic_registrations
                WHERE id = ?
                """,
                (rs, rowNum) -> new RegistrationInfo(
                        rs.getLong("group_id"),
                        rs.getLong("topic_id"),
                        rs.getLong("round_id")
                ),
                registrationId
        );

        if (initialRegistrations.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ho so dang ky khong ton tai"
            );
        }

        RegistrationInfo initial = initialRegistrations.get(0);

        Long groupId = initial.groupId();
        Long topicId = initial.topicId();
        Long roundId = initial.roundId();

        // 4. Khoa dot dang ky
        roundAccess.lockRound(roundId);

        // 5. Khoa nhom
        List<GroupInfo> groups = jdbc.query(
                """
                SELECT status, round_id
                FROM student_groups
                WHERE id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new GroupInfo(
                        rs.getString("status"),
                        rs.getLong("round_id")
                ),
                groupId
        );

        if (groups.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay nhom sinh vien"
            );
        }

        GroupInfo group = groups.get(0);

        if (!roundId.equals(group.roundId())) {
            throw new IllegalStateException(
                    "Nhom khong thuoc dot dang ky cua ho so"
            );
        }

        if (!"FORMING".equals(group.status())) {
            throw new IllegalStateException(
                    "Nhom khong o trang thai cho phep tu choi"
            );
        }

        // 6. Khoa de tai
        List<TopicInfo> topics = jdbc.query(
                """
                SELECT status, round_id
                FROM topics
                WHERE id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new TopicInfo(
                        rs.getString("status"),
                        rs.getLong("round_id")
                ),
                topicId
        );

        if (topics.isEmpty()) {
            throw new IllegalStateException(
                    "Khong tim thay de tai"
            );
        }

        TopicInfo topic = topics.get(0);

        if (!roundId.equals(topic.roundId())) {
            throw new IllegalStateException(
                    "De tai khong thuoc dot dang ky cua ho so"
            );
        }

        // Khong bat buoc topic phai PUBLISHED khi tu choi.
        // DEAN can co the tu choi ho so PENDING khong con hop le.

        // 7. Khoa ho so va kiem tra lai trang thai
        List<LockedRegistration> registrations = jdbc.query(
                """
                SELECT status, group_id, topic_id, round_id
                FROM topic_registrations
                WHERE id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new LockedRegistration(
                        rs.getString("status"),
                        rs.getLong("group_id"),
                        rs.getLong("topic_id"),
                        rs.getLong("round_id")
                ),
                registrationId
        );

        if (registrations.isEmpty()) {
            throw new IllegalStateException(
                    "Ho so dang ky khong con ton tai"
            );
        }

        LockedRegistration registration = registrations.get(0);

        if (!groupId.equals(registration.groupId())
                || !topicId.equals(registration.topicId())
                || !roundId.equals(registration.roundId())) {
            throw new IllegalStateException(
                    "Thong tin ho so da thay doi"
            );
        }

        if (!"PENDING".equals(registration.status())) {
            throw new IllegalStateException(
                    "Chi co the tu choi ho so dang cho duyet"
            );
        }

        // 8. Lay thoi gian xu ly theo UTC
        LocalDateTime decidedAt = LocalDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );

        // 9. Chuyen PENDING -> REJECTED
        int rejected = jdbc.update(
                """
                UPDATE topic_registrations
                SET status = 'REJECTED',
                    decided_by = ?,
                    decided_at = ?,
                    decision_note = ?
                WHERE id = ?
                  AND status = 'PENDING'
                """,
                deanId,
                decidedAt,
                normalizedReason,
                registrationId
        );

        if (rejected != 1) {
            throw new IllegalStateException(
                    "Khong the cap nhat trang thai tu choi"
            );
        }

        // reserved_topic_id duoc MySQL tu dong tinh lai:
        // status = REJECTED -> reserved_topic_id = NULL.
        // Nhom van giu trang thai FORMING.

        // 10. Ghi audit log trong cung transaction
        int audited = jdbc.update(
                """
                INSERT INTO audit_logs
                    (actor_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """,
                deanId,
                "REGISTRATION_REJECTED",
                "TOPIC_REGISTRATION",
                registrationId,
                "Rejected registration " + registrationId
                        + ", group " + groupId
                        + ", topic " + topicId
        );

        if (audited != 1) {
            throw new IllegalStateException(
                    "Khong the ghi nhat ky tu choi ho so"
            );
        }
    }
}

