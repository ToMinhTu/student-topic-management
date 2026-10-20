package com.example.student_topic_management.policy;

import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.RoundStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RoundPolicyTest {

    private RoundPolicy policy;
    private RegistrationRound round;

    @BeforeEach
    void setUp() {

        policy = new RoundPolicy();
        round = new RegistrationRound();

        round.setStatus(RoundStatus.OPEN);

        // Gia tri trong Entity duoc hieu la UTC
        round.setProposalStartsAt(
                LocalDateTime.parse("2026-10-20T01:00:00"));

        round.setProposalEndsAt(
                LocalDateTime.parse("2026-10-25T10:00:00"));

        round.setStudentStartsAt(
                LocalDateTime.parse("2026-10-26T01:00:00"));

        round.setStudentEndsAt(
                LocalDateTime.parse("2026-10-31T10:00:00"));
    }

    @Test
    void beforeProposalStart_shouldReturnFalse() {

        Instant now = Instant.parse("2026-10-20T00:59:59Z");

        assertFalse(policy.isProposalOpen(round, now));
    }

    @Test
    void atProposalStart_shouldReturnTrue() {

        Instant now = Instant.parse("2026-10-20T01:00:00Z");

        assertTrue(policy.isProposalOpen(round, now));
    }

    @Test
    void duringProposal_shouldReturnTrue() {

        Instant now = Instant.parse("2026-10-22T05:00:00Z");

        assertTrue(policy.isProposalOpen(round, now));
    }

    @Test
    void atProposalEnd_shouldReturnFalse() {

        Instant now = Instant.parse("2026-10-25T10:00:00Z");

        assertFalse(policy.isProposalOpen(round, now));
    }

    @Test
    void duringRegistration_shouldReturnTrue() {

        Instant now = Instant.parse("2026-10-28T05:00:00Z");

        assertTrue(policy.isStudentRegistrationOpen(round, now));
    }

    @Test
    void atRegistrationStart_shouldReturnTrue() {

        Instant now = Instant.parse("2026-10-26T01:00:00Z");

        assertTrue(policy.isStudentRegistrationOpen(round, now));
    }

    @Test
    void atRegistrationEnd_shouldReturnFalse() {

        Instant now = Instant.parse("2026-10-31T10:00:00Z");

        assertFalse(policy.isStudentRegistrationOpen(round, now));
    }

    @Test
    void draftRound_shouldReturnFalse() {

        round.setStatus(RoundStatus.DRAFT);

        Instant now = Instant.parse("2026-10-28T05:00:00Z");

        assertFalse(policy.isStudentRegistrationOpen(round, now));
    }

    @Test
    void closedRound_shouldReturnFalse() {

        round.setStatus(RoundStatus.CLOSED);

        Instant now = Instant.parse("2026-10-28T05:00:00Z");

        assertFalse(policy.isStudentRegistrationOpen(round, now));
    }

    @Test
    void invalidRegistration_shouldThrowException() {

        Instant now = Instant.parse("2026-11-01T00:00:00Z");

        assertThrows(
                IllegalStateException.class,
                () -> policy.requireStudentRegistrationOpen(round, now));
    }

    @Test
    void nullRound_shouldReturnFalse() {

        assertFalse(policy.isStudentRegistrationOpen(
                null, Instant.now()));
    }

    @Test
    void invalidWindow_shouldReturnFalse() {

        round.setStudentEndsAt(
                round.getStudentStartsAt());

        assertFalse(policy.isStudentRegistrationOpen(
                round,
                Instant.parse("2026-10-26T01:00:00Z")));
    }
}
