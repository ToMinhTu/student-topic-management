package com.example.student_topic_management.assessment.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.student_topic_management.assessment.policy.ResultRequirementPolicy.Assignment;
import com.example.student_topic_management.assessment.policy.ResultRequirementPolicy.SubmittedEvaluation;

class ResultRequirementPolicyTest {

    private final ResultRequirementPolicy policy = new ResultRequirementPolicy();

    @Test
    void acceptsExactCouncilAndOneRequiredReviewer() {
        policy.requireValidAssignments(
                Set.of(10L, 11L, 12L),
                List.of(
                        new Assignment(1L, 10L, "COUNCIL"),
                        new Assignment(2L, 11L, "COUNCIL"),
                        new Assignment(3L, 12L, "COUNCIL"),
                        new Assignment(4L, 20L, "REVIEWER")),
                true);
    }

    @Test
    void acceptsOneOptionalReviewerForExistingCourseData() {
        policy.requireValidAssignments(
                Set.of(10L, 11L),
                List.of(
                        new Assignment(1L, 10L, "COUNCIL"),
                        new Assignment(2L, 11L, "COUNCIL"),
                        new Assignment(3L, 20L, "REVIEWER")),
                false);
    }

    @Test
    void rejectsMissingCouncilAssignment() {
        assertThrows(IllegalStateException.class, () -> policy.requireValidAssignments(
                Set.of(10L, 11L),
                List.of(new Assignment(1L, 10L, "COUNCIL")),
                false));
    }

    @Test
    void rejectsMissingOrLateEvaluation() {
        LocalDateTime deadline = LocalDateTime.of(2026, 10, 20, 17, 0);
        List<Assignment> assignments = List.of(new Assignment(1L, 20L, "REVIEWER"));

        assertThrows(IllegalStateException.class,
                () -> policy.requireCompleteEvaluations(assignments, List.of(), deadline));
        assertThrows(IllegalStateException.class, () -> policy.requireCompleteEvaluations(
                assignments,
                List.of(new SubmittedEvaluation(1L, "REVIEWER", new BigDecimal("8"), "Đạt",
                        deadline.plusMinutes(1))),
                deadline));
    }

    @Test
    void returnsScoresWhenEveryEvaluationIsValid() {
        LocalDateTime submittedAt = LocalDateTime.of(2026, 10, 20, 16, 0);
        List<Assignment> assignments = List.of(
                new Assignment(1L, 10L, "COUNCIL"),
                new Assignment(2L, 11L, "COUNCIL"));

        List<BigDecimal> scores = policy.requireCompleteEvaluations(
                assignments,
                List.of(
                        new SubmittedEvaluation(1L, "COUNCIL", new BigDecimal("8"), "Tốt", submittedAt),
                        new SubmittedEvaluation(2L, "COUNCIL", new BigDecimal("9"), "Tốt", submittedAt)),
                null);

        assertEquals(List.of(new BigDecimal("8"), new BigDecimal("9")), scores);
    }
}
