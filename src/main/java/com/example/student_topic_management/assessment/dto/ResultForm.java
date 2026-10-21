// toan-D3
// toan-D3 - thêm chức năng tổng hợp kết quả
package com.example.student_topic_management.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ResultForm {

    @NotNull(message = "Mã hồ sơ không được để trống")
    @Positive(message = "Mã hồ sơ không hợp lệ")
    private Long registrationId;

    @NotBlank(message = "Kết luận không được để trống")
    @Size(max = 5000, message = "Kết luận không được vượt quá 5000 ký tự")
    private String conclusion;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
}
