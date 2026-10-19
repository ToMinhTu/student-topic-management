// toan-D1
// toan-D1 - thêm chức năng quản lý hội đồng
package com.example.student_topic_management.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CouncilForm {

    private Long id;

    @NotNull(message = "Đợt đăng ký không được để trống")
    private Long roundId;

    @NotBlank(message = "Tên hội đồng không được để trống")
    @Size(max = 150, message = "Tên hội đồng tối đa 150 ký tự")
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}