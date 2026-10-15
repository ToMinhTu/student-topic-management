// toan-D3
package com.example.student_topic_management.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResultForm {

    @NotNull(message = "Mã hồ sơ không được để trống")
    private Long registrationId;

    @NotNull(message = "Mã người xác nhận không được để trống")
    private Long confirmedBy;

    @NotBlank(message = "Kết luận không được để trống")
    private String conclusion;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(Long confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
}