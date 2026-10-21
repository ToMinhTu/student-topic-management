
package com.example.student_topic_management.registration.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Thong tin ho so dang ky va nhom
    public record RegistrationInfo(
            Long registrationId,
            Long groupId,
            Long roundId,
            String registrationStatus,
            String groupStatus,
            LocalDateTime reportDueAt
    ) {}

    // Thong tin mot phien ban bao cao
    public record ReportInfo(
            Long id,
            Long registrationId,
            int versionNo,
            String storageKey,
            String originalName,
            String contentType,
            long sizeBytes,
            String sha256,
            Long submittedBy,
            LocalDateTime createdAt
    ) {}

    // C05.1 - Tim ho so dang ky va trang thai nhom
    public Optional<RegistrationInfo> findRegistration(
            Long registrationId) {

        String sql = """
                SELECT
                    tr.id AS registration_id,
                    tr.group_id,
                    tr.round_id,
                    tr.status AS registration_status,
                    sg.status AS group_status,
                    rr.report_due_at
                FROM topic_registrations tr
                JOIN student_groups sg
                    ON sg.id = tr.group_id
                   AND sg.round_id = tr.round_id
                JOIN registration_rounds rr
                    ON rr.id = tr.round_id
                WHERE tr.id = ?
                """;

        List<RegistrationInfo> results = jdbc.query(
                sql,
                (rs, rowNum) -> new RegistrationInfo(
                        rs.getLong("registration_id"),
                        rs.getLong("group_id"),
                        rs.getLong("round_id"),
                        rs.getString("registration_status"),
                        rs.getString("group_status"),
                        rs.getTimestamp("report_due_at") == null
                                ? null
                                : rs.getTimestamp("report_due_at")
                                    .toLocalDateTime()
                ),
                registrationId
        );

        return results.stream().findFirst();
    }

    // C05.2 - Kiem tra sinh vien la truong nhom
    public boolean isActiveLeader(
            Long groupId,
            Long studentId) {

        String sql = """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                  AND student_id = ?
                  AND slot_no = 1
                  AND left_at IS NULL
                """;

        Long count = jdbc.queryForObject(
                sql,
                Long.class,
                groupId,
                studentId
        );

        return count != null && count > 0;
    }

        // C05 - Kiem tra sinh vien la thanh vien dang hoat dong
    public boolean isActiveMember(
            Long groupId,
            Long studentId) {

        String sql = """
                SELECT COUNT(*)
                FROM group_members
                WHERE group_id = ?
                AND student_id = ?
                AND left_at IS NULL
                """;

        Long count = jdbc.queryForObject(
                sql,
                Long.class,
                groupId,
                studentId
        );

        return count != null && count > 0;
    }

    // C05.3 - Tim so phien ban cao nhat
    public int findLatestVersion(Long registrationId) {

        String sql = """
                SELECT COALESCE(MAX(version_no), 0)
                FROM reports
                WHERE registration_id = ?
                """;

        Integer version = jdbc.queryForObject(
                sql,
                Integer.class,
                registrationId
        );

        return version == null ? 0 : version;
    }

    // C05.4 - Luu metadata bao cao
    public int insertReport(
            Long registrationId,
            int versionNo,
            String storageKey,
            String originalName,
            String contentType,
            long sizeBytes,
            String sha256,
            Long submittedBy) {

        String sql = """
                INSERT INTO reports (
                    registration_id,
                    version_no,
                    storage_key,
                    original_name,
                    content_type,
                    size_bytes,
                    sha256,
                    submitted_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbc.update(
                sql,
                registrationId,
                versionNo,
                storageKey,
                originalName,
                contentType,
                sizeBytes,
                sha256,
                submittedBy
        );
    }

    // C05.5 - Danh sach phien ban bao cao
    public List<ReportInfo> findByRegistrationId(
            Long registrationId) {

        String sql = """
                SELECT
                    id,
                    registration_id,
                    version_no,
                    storage_key,
                    original_name,
                    content_type,
                    size_bytes,
                    sha256,
                    submitted_by,
                    created_at
                FROM reports
                WHERE registration_id = ?
                ORDER BY version_no DESC
                """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> mapReport(rs),
                registrationId
        );
    }

    // C05.6 - Tim bao cao theo ID
    public Optional<ReportInfo> findById(Long reportId) {

        String sql = """
                SELECT
                    id,
                    registration_id,
                    version_no,
                    storage_key,
                    original_name,
                    content_type,
                    size_bytes,
                    sha256,
                    submitted_by,
                    created_at
                FROM reports
                WHERE id = ?
                """;

        List<ReportInfo> reports = jdbc.query(
                sql,
                (rs, rowNum) -> mapReport(rs),
                reportId
        );

        return reports.stream().findFirst();
    }

    private ReportInfo mapReport(
            java.sql.ResultSet rs)
            throws java.sql.SQLException {

        return new ReportInfo(
                rs.getLong("id"),
                rs.getLong("registration_id"),
                rs.getInt("version_no"),
                rs.getString("storage_key"),
                rs.getString("original_name"),
                rs.getString("content_type"),
                rs.getLong("size_bytes"),
                rs.getString("sha256"),
                rs.getLong("submitted_by"),
                rs.getTimestamp("created_at")
                        .toLocalDateTime()
        );
    }
}
