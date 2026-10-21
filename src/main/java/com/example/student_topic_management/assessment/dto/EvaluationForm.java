// toan-D3
// toan-D3 - thêm chức năng nhập điểm
package com.example.student_topic_management.assessment.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EvaluationForm {

    @NotNull(message = "Mã phân công không được để trống")
    private Long assignmentId;

    @NotNull(message = "Điểm không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm nhỏ nhất là 0")
    @DecimalMax(value = "10.0", message = "Điểm lớn nhất là 10")
    private BigDecimal score;

    @NotBlank(message = "Nhận xét không được để trống")
    @Size(max = 5000, message = "Nhận xét không được vượt quá 5000 ký tự")
    private String comments;

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
