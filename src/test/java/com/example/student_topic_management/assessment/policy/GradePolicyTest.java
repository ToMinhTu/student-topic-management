package com.example.student_topic_management.assessment.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class GradePolicyTest {

    private final GradePolicy policy = new GradePolicy();

    @Test
    void calculatesAverageWithTwoDecimalHalfUpRounding() {
        assertEquals(new BigDecimal("8.00"), policy.average(List.of(
                new BigDecimal("8"), new BigDecimal("7"), new BigDecimal("9"))));
        assertEquals(new BigDecimal("0.33"), policy.average(List.of(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE)));
    }

    @Test
    void rejectsEmptyOrOutOfRangeScores() {
        assertThrows(IllegalArgumentException.class, () -> policy.average(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> policy.average(List.of(new BigDecimal("10.01"))));
    }
}
