// toan-D3
// toan-D3 - thêm chức năng tổng hợp kết quả
package com.example.student_topic_management.assessment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.student_topic_management.assessment.dto.ResultForm;
import com.example.student_topic_management.assessment.entity.Evaluation;
import com.example.student_topic_management.assessment.entity.Result;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.assessment.repository.EvaluationRepository;
import com.example.student_topic_management.assessment.repository.ResultRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;

@Service
public class ResultService {

    private final ResultRepository resultRepository;
    private final ReviewerAssignmentRepository assignmentRepository;
    private final EvaluationRepository evaluationRepository;

    public ResultService(
            ResultRepository resultRepository,
            ReviewerAssignmentRepository assignmentRepository,
            EvaluationRepository evaluationRepository) {

        this.resultRepository = resultRepository;
        this.assignmentRepository = assignmentRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public List<Result> findAll() {
        return resultRepository.findAll();
    }

    public Result findById(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy kết quả"));
    }

    public void consolidateResult(ResultForm form) {

        if (resultRepository.existsByRegistrationId(
                form.getRegistrationId())) {
            throw new RuntimeException(
                    "Hồ sơ này đã được tổng hợp kết quả");
        }

        List<ReviewerAssignment> assignments =
                assignmentRepository.findByRegistrationId(
                        form.getRegistrationId());

        if (assignments.isEmpty()) {
            throw new RuntimeException(
                    "Hồ sơ chưa có phân công chấm điểm");
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        int componentCount = 0;

        for (ReviewerAssignment assignment : assignments) {
            Evaluation evaluation = evaluationRepository
                    .findByAssignmentId(assignment.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Chưa đủ phiếu chấm điểm"));

            if (evaluation.getSubmittedAt() == null) {
                throw new RuntimeException(
                        "Phiếu chấm chưa được nộp");
            }

            totalScore = totalScore.add(evaluation.getScore());
            componentCount++;
        }

        BigDecimal finalScore = totalScore
                .divide(
                        BigDecimal.valueOf(componentCount),
                        2,
                        RoundingMode.HALF_UP);

        Result result = new Result();
        result.setRegistrationId(form.getRegistrationId());
        result.setFinalScore(finalScore);
        result.setComponentCount(componentCount);
        result.setConclusion(form.getConclusion());
        result.setConfirmedBy(form.getConfirmedBy());
        result.setConfirmedAt(LocalDateTime.now());

        resultRepository.save(result);
    }
}