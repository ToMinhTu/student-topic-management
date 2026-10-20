package com.example.student_topic_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DepartmentReviewForm {
    public enum Decision { APPROVED, REJECTED }

    @NotNull(message = "Vui lòng chọn kết quả sơ duyệt")
    private Decision decision;

    @Size(max = 4000, message = "Nhận xét không được vượt quá 4000 ký tự")
    private String comment;

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}