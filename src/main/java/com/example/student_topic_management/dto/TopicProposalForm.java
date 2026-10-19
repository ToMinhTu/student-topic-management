package com.example.student_topic_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TopicProposalForm {

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    @Positive(message = "Đợt đăng ký không hợp lệ")
    private Long roundId;

    @NotNull(message = "Vui lòng chọn bộ môn")
    @Positive(message = "Bộ môn không hợp lệ")
    private Long departmentId;

    @NotBlank(message = "Vui lòng nhập mã đề tài")
    @Size(max = 40, message = "Mã đề tài tối đa 40 ký tự")
    private String code;

    @NotBlank(message = "Vui lòng nhập tên đề tài")
    @Size(max = 255, message = "Tên đề tài tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Vui lòng nhập mô tả đề tài")
    private String description;

    private String requirements;

    public TopicProposalForm() {
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.strip().toUpperCase(java.util.Locale.ROOT);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}
