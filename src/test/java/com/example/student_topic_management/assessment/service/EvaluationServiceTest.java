package com.example.student_topic_management.assessment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.student_topic_management.assessment.dto.EvaluationForm;
import com.example.student_topic_management.assessment.entity.Evaluation;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.assessment.repository.EvaluationRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;

class EvaluationServiceTest {

    private EvaluationRepository evaluationRepository;
    private ReviewerAssignmentRepository assignmentRepository;
    private CurrentUserService currentUserService;
    private EvaluationService service;

    @BeforeEach
    void setup() {
        evaluationRepository = mock(EvaluationRepository.class);
        assignmentRepository = mock(ReviewerAssignmentRepository.class);
        currentUserService = mock(CurrentUserService.class);
        service = new EvaluationService(
                evaluationRepository,
                assignmentRepository,
                currentUserService);
    }

    @Test
    void lecturerCannotEvaluateTheSameAssignmentTwice() {
        ReviewerAssignment assignment = new ReviewerAssignment();
        assignment.setLecturerId(56L);

        when(assignmentRepository.findById(7L))
                .thenReturn(Optional.of(assignment));
        when(currentUserService.getCurrentUserId()).thenReturn(56L);
        when(evaluationRepository.existsByAssignmentId(7L)).thenReturn(true);

        EvaluationForm form = new EvaluationForm();
        form.setAssignmentId(7L);
        form.setScore(new BigDecimal("8.50"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.saveEvaluation(form));

        assertEquals(
                "Bạn đã chấm điểm cho phân công này rồi",
                error.getMessage());
        verify(evaluationRepository, never()).saveAndFlush(
                org.mockito.ArgumentMatchers.any(Evaluation.class));
    }
}
