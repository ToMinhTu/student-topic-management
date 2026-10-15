package com.example.student_topic_management.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class GroupCreateForm {

    @NotBlank(message = "Tên nhóm không được để trống")
    @Size(max = 150, message = "Tên nhóm không được vượt quá 150 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn đợt đăng ký")
    private Long roundId;

    public GroupCreateForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
}