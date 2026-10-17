
package com.example.student_topic_management.registration.service;

import java.time.Instant;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class RoundAccessImpl implements RoundAccess {

    private final JdbcTemplate jdbc;

    public RoundAccessImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Khoa dot dang ky trong transaction
    @Override
    public void lockRound(Long roundId) {

        requirePositive(roundId);

        requireTransaction();

        var rounds = jdbc.queryForList(
            """
            SELECT id
            FROM registration_rounds
            WHERE id = ?
            FOR UPDATE
            """,
            Long.class,
            roundId
        );

        if (rounds.isEmpty()) {
            throw new IllegalArgumentException(
                "Dot dang ky khong ton tai"
            );
        }
    }

    // Kiem tra trang thai va thoi gian dang ky
    @Override
    public void requireStudentWindow(
            Long roundId,
            Instant now) {

        requirePositive(roundId);

        if (now == null) {
            throw new IllegalArgumentException(
                "Thoi gian khong hop le"
            );
        }

        var rounds = jdbc.query(
            """
            SELECT status,
                   student_starts_at,
                   student_ends_at
            FROM registration_rounds
            WHERE id = ?
            """,
            (rs, rowNum) -> new RoundWindow(
                rs.getString("status"),
                rs.getTimestamp("student_starts_at")
                    .toLocalDateTime()
                    .toInstant(java.time.ZoneOffset.UTC),
                rs.getTimestamp("student_ends_at")
                    .toLocalDateTime()
                    .toInstant(java.time.ZoneOffset.UTC)
            ),
            roundId
        );

        if (rounds.isEmpty()) {
            throw new IllegalArgumentException(
                "Dot dang ky khong ton tai"
            );
        }

        RoundWindow round = rounds.get(0);

        if (!"OPEN".equals(round.status())) {
            throw new IllegalStateException(
                "Dot dang ky chua mo hoac da dong"
            );
        }

        if (now.isBefore(round.start())
                || !now.isBefore(round.end())) {

            throw new IllegalStateException(
                "Ngoai thoi gian dang ky sinh vien"
            );
        }
    }

    // Lay so thanh vien toi da cua nhom
    @Override
    public int getGroupMaxSize(Long roundId) {

        requirePositive(roundId);

        Integer maxSize = jdbc.query(
            """
            SELECT group_max_size
            FROM registration_rounds
            WHERE id = ?
            """,
            rs -> rs.next()
                ? rs.getInt("group_max_size")
                : null,
            roundId
        );

        if (maxSize == null) {
            throw new IllegalArgumentException(
                "Dot dang ky khong ton tai"
            );
        }

        if (maxSize < 1 || maxSize > 5) {
            throw new IllegalStateException(
                "So thanh vien toi da khong hop le"
            );
        }

        return maxSize;
    }

    private void requirePositive(Long roundId) {
        if (roundId == null || roundId <= 0) {
            throw new IllegalArgumentException(
                "Ma dot dang ky khong hop le"
            );
        }
    }

    private void requireTransaction() {

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()) {

            throw new IllegalStateException(
                "Phai thuc hien trong transaction"
            );
        }
    }

    private record RoundWindow(
            String status,
            Instant start,
            Instant end
    ) {}
}
