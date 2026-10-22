// toan-D3
// toan-D3 - thêm chức năng tổng hợp kết quả
package com.example.student_topic_management.assessment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.assessment.dto.ResultForm;
import com.example.student_topic_management.assessment.entity.Evaluation;
import com.example.student_topic_management.assessment.entity.Result;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.assessment.repository.EvaluationRepository;
import com.example.student_topic_management.assessment.repository.ResultRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;

@Service
public class ResultService {

    private static final String RESULT_VIEW_SQL = """
            SELECT r.id, r.registration_id, r.final_score, r.component_count,
                   r.conclusion, r.confirmed_at, r.published_at,
                   g.name AS group_name, t.code AS topic_code,
                   t.title AS topic_title, u.full_name AS confirmer_name
            FROM results r
            JOIN topic_registrations tr ON tr.id = r.registration_id
            JOIN student_groups g ON g.id = tr.group_id
            JOIN topics t ON t.id = tr.topic_id
            JOIN users u ON u.id = r.confirmed_by
            """;

    // Read model for the templates; these joined fields are not columns of results.
    public record ResultView(
            Long id, Long registrationId, String groupName,
            String topicCode, String topicTitle, BigDecimal finalScore,
            Integer componentCount, String conclusion, String confirmerName,
            LocalDateTime confirmedAt, LocalDateTime publishedAt) {
    }

    private final ResultRepository resultRepository;
    private final ReviewerAssignmentRepository assignmentRepository;
    private final EvaluationRepository evaluationRepository;
    private final JdbcTemplate jdbc;

    public ResultService(
            ResultRepository resultRepository,
            ReviewerAssignmentRepository assignmentRepository,
            EvaluationRepository evaluationRepository,
            JdbcTemplate jdbc) {

        this.resultRepository = resultRepository;
        this.assignmentRepository = assignmentRepository;
        this.evaluationRepository = evaluationRepository;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<ResultView> findAll() {
        return queryResultViews(RESULT_VIEW_SQL + " ORDER BY r.confirmed_at DESC, r.id DESC");
    }

    @Transactional(readOnly = true)
    public ResultView findById(Long id) {
        return queryResultViews(RESULT_VIEW_SQL + " WHERE r.id = ?", id).stream().findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy kết quả"));
    }

    private List<ResultView> queryResultViews(String sql, Object... parameters) {
        return jdbc.query(sql, (rs, rowNum) -> new ResultView(
                rs.getLong("id"), rs.getLong("registration_id"),
                rs.getString("group_name"), rs.getString("topic_code"),
                rs.getString("topic_title"), rs.getBigDecimal("final_score"),
                rs.getInt("component_count"), rs.getString("conclusion"),
                rs.getString("confirmer_name"),
                rs.getTimestamp("confirmed_at") == null
                        ? null : rs.getTimestamp("confirmed_at").toLocalDateTime(),
                rs.getTimestamp("published_at") == null
                        ? null : rs.getTimestamp("published_at").toLocalDateTime()), parameters);
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
