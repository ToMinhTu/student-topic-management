package com.example.student_topic_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DeanTopicReviewForm {
    public enum Decision { APPROVED, REJECTED }

    @NotNull(message = "Vui lòng chọn quyết định của trưởng khoa")
    private Decision decision;

    @Size(max = 4000, message = "Lý do từ chối không được vượt quá 4000 ký tự")
    private String rejectionReason;

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}