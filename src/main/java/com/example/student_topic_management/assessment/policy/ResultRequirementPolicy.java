package com.example.student_topic_management.assessment.policy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ResultRequirementPolicy {

    public record Assignment(Long id, Long lecturerId, String type) {
    }

    public record SubmittedEvaluation(
            Long assignmentId,
            String assignmentType,
            BigDecimal score,
            String comments,
            LocalDateTime submittedAt) {
    }

    public void requireValidAssignments(
            Set<Long> councilMemberIds,
            List<Assignment> assignments,
            boolean reviewerRequired) {

        if (councilMemberIds == null || councilMemberIds.isEmpty()) {
            throw new IllegalStateException("Hội đồng chưa có thành viên");
        }
        if (assignments == null || assignments.isEmpty()) {
            throw new IllegalStateException("Hồ sơ chưa có phân công chấm điểm");
        }

        Set<Long> councilGraders = new HashSet<>();
        Set<Long> reviewerGraders = new HashSet<>();
        for (Assignment assignment : assignments) {
            if (assignment.id() == null || assignment.lecturerId() == null) {
                throw new IllegalStateException("Phân công chấm điểm không hợp lệ");
            }
            if ("COUNCIL".equals(assignment.type())) {
                councilGraders.add(assignment.lecturerId());
            } else if ("REVIEWER".equals(assignment.type())) {
                reviewerGraders.add(assignment.lecturerId());
            } else {
                throw new IllegalStateException("Loại phân công chấm điểm không hợp lệ");
            }
        }

        if (!councilGraders.equals(councilMemberIds)) {
            throw new IllegalStateException("Phân công chấm của hội đồng chưa đầy đủ hoặc không khớp");
        }
        if (reviewerRequired && reviewerGraders.size() != 1) {
            throw new IllegalStateException("Đợt này phải có đúng một giảng viên phản biện");
        }
        // COURSE/RESEARCH không bắt buộc phản biện, nhưng chấp nhận tối đa một
        // phản biện độc lập để tương thích các lịch đã được D2 tạo trước đó.
        if (!reviewerRequired && reviewerGraders.size() > 1) {
            throw new IllegalStateException("Hồ sơ chỉ được có tối đa một giảng viên phản biện");
        }

        Set<Long> allGraders = new HashSet<>(councilGraders);
        allGraders.addAll(reviewerGraders);
        if (allGraders.size() != assignments.size()) {
            throw new IllegalStateException("Một giảng viên không được chấm nhiều lần trong cùng hồ sơ");
        }
    }

    public List<BigDecimal> requireCompleteEvaluations(
            List<Assignment> assignments,
            List<SubmittedEvaluation> evaluations,
            LocalDateTime reviewerDueAt) {

        if (evaluations == null || evaluations.size() != assignments.size()) {
            throw new IllegalStateException("Chưa đủ phiếu chấm điểm bắt buộc");
        }

        Set<Long> assignmentIds = new HashSet<>();
        assignments.forEach(assignment -> assignmentIds.add(assignment.id()));
        Set<Long> evaluatedAssignmentIds = new HashSet<>();

        for (SubmittedEvaluation evaluation : evaluations) {
            if (!assignmentIds.contains(evaluation.assignmentId())
                    || !evaluatedAssignmentIds.add(evaluation.assignmentId())) {
                throw new IllegalStateException("Phiếu chấm không khớp với phân công");
            }
            if (evaluation.submittedAt() == null) {
                throw new IllegalStateException("Có phiếu chấm chưa được nộp");
            }
            if (evaluation.comments() == null || evaluation.comments().isBlank()) {
                throw new IllegalStateException("Mọi phiếu chấm phải có nhận xét");
            }
            if (evaluation.score() == null
                    || evaluation.score().compareTo(BigDecimal.ZERO) < 0
                    || evaluation.score().compareTo(BigDecimal.TEN) > 0) {
                throw new IllegalStateException("Điểm thành phần phải nằm trong khoảng 0 đến 10");
            }
            if ("REVIEWER".equals(evaluation.assignmentType())
                    && reviewerDueAt != null
                    && evaluation.submittedAt().isAfter(reviewerDueAt)) {
                throw new IllegalStateException("Phiếu phản biện được nộp sau hạn của đợt");
            }
        }

        return evaluations.stream().map(SubmittedEvaluation::score).toList();
    }
}
