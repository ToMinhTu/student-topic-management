package com.example.student_topic_management.policy;

import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.RoundStatus;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class RoundPolicy {

    // Kiem tra thoi gian theo khoang [start, end)
    private boolean isWithinWindow(
            LocalDateTime startUtc,
            LocalDateTime endUtc,
            Instant now) {

        if (startUtc == null || endUtc == null || now == null) {
            return false;
        }

        Instant start = startUtc.toInstant(ZoneOffset.UTC);
        Instant end = endUtc.toInstant(ZoneOffset.UTC);

        if (!start.isBefore(end)) {
            return false;
        }

        return !now.isBefore(start) && now.isBefore(end);
    }

    // Kiem tra dot co dang OPEN hay khong
    public boolean isRoundOpen(RegistrationRound round) {

        return round != null
                && round.getStatus() == RoundStatus.OPEN;
    }

    // Kiem tra giai doan de xuat de tai
    public boolean isProposalOpen(
            RegistrationRound round,
            Instant now) {

        if (!isRoundOpen(round)) {
            return false;
        }

        return isWithinWindow(
                round.getProposalStartsAt(),
                round.getProposalEndsAt(),
                now);
    }

    // Kiem tra giai doan sinh vien dang ky
    public boolean isStudentRegistrationOpen(
            RegistrationRound round,
            Instant now) {

        if (!isRoundOpen(round)) {
            return false;
        }

        return isWithinWindow(
                round.getStudentStartsAt(),
                round.getStudentEndsAt(),
                now);
    }

    // Chan thao tac de xuat neu khong hop le
    public void requireProposalOpen(
            RegistrationRound round,
            Instant now) {

        if (!isProposalOpen(round, now)) {
            throw new IllegalStateException(
                    "Hien tai khong trong thoi gian de xuat de tai");
        }
    }

    // Chan thao tac dang ky neu khong hop le
    public void requireStudentRegistrationOpen(
            RegistrationRound round,
            Instant now) {

        if (!isStudentRegistrationOpen(round, now)) {
            throw new IllegalStateException(
                    "Hien tai khong trong thoi gian dang ky de tai");
        }
    }
}
