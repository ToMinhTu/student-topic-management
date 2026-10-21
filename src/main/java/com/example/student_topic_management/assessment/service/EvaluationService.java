// toan-D3
// toan-D3 - thêm chức năng nhập điểm
package com.example.student_topic_management.assessment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.assessment.dto.EvaluationForm;
import com.example.student_topic_management.assessment.entity.Evaluation;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.assessment.repository.EvaluationRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ReviewerAssignmentRepository assignmentRepository;
    private final CurrentUserService currentUserService;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            ReviewerAssignmentRepository assignmentRepository,
            CurrentUserService currentUserService) {
        this.evaluationRepository = evaluationRepository;
        this.assignmentRepository = assignmentRepository;
        this.currentUserService = currentUserService;
    }

    public List<Evaluation> findAll() {
        return evaluationRepository.findAll();
    }

    public List<ReviewerAssignment> findAssignmentsForCurrentUser() {
        return assignmentRepository
                .findByLecturerId(currentUserService.getCurrentUserId())
                .stream()
                .filter(assignment -> !evaluationRepository
                        .existsByAssignmentId(assignment.getId()))
                .toList();
    }

    public Evaluation findById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phiếu chấm"));
    }

    @Transactional
    public void saveEvaluation(EvaluationForm form) {

        ReviewerAssignment assignment = assignmentRepository
                .findById(form.getAssignmentId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phân công chấm"));

        Long currentUserId = currentUserService.getCurrentUserId();
        if (!currentUserId.equals(assignment.getLecturerId())) {
            throw new RuntimeException("Bạn không có quyền nhập điểm cho phân công này");
        }

        if (evaluationRepository.existsByAssignmentId(
                form.getAssignmentId())) {
            throw new RuntimeException(
                    "Bạn đã chấm điểm cho phân công này rồi");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setAssignmentId(assignment.getId());
        evaluation.setScore(form.getScore());
        evaluation.setComments(form.getComments());
        evaluation.setSubmittedAt(LocalDateTime.now());

        try {
            // Flush ngay để ràng buộc UNIQUE assignment_id chặn cả hai request
            // được gửi gần như đồng thời.
            evaluationRepository.saveAndFlush(evaluation);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException(
                    "Bạn đã chấm điểm cho phân công này rồi",
                    ex);
        }
    }
}
