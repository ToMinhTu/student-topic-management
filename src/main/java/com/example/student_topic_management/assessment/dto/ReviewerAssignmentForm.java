package com.example.student_topic_management.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class ReviewerAssignmentForm {

    @NotNull(message = "Vui lòng chọn hồ sơ đăng ký")
    @Positive(message = "Mã hồ sơ đăng ký không hợp lệ")
    private Long registrationId;

    @NotNull(message = "Vui lòng chọn giảng viên")
    @Positive(message = "Mã giảng viên không hợp lệ")
    private Long lecturerId;

    @NotBlank(message = "Vui lòng chọn loại phân công")
    @Pattern(regexp = "REVIEWER|COUNCIL", message = "Loại phân công không hợp lệ")
    private String assignmentType = "REVIEWER";

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }
}
