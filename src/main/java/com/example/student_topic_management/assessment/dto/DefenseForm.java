// toan-D2
// toan-D2 - thêm chức năng quản lý lịch bảo vệ
package com.example.student_topic_management.assessment.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class DefenseForm {

    private Long id;

    @NotNull(message = "Mã hồ sơ không được để trống")
    @Positive(message = "Mã hồ sơ phải là số dương")
    private Long registrationId;

    @NotNull(message = "Mã đợt không được để trống")
    @Positive(message = "Mã đợt phải là số dương")
    private Long roundId;

    @NotNull(message = "Mã hội đồng không được để trống")
    @Positive(message = "Mã hội đồng phải là số dương")
    private Long councilId;

    @NotNull(message = "Thời gian bảo vệ không được để trống")
    @Future(message = "Thời gian bảo vệ phải ở tương lai")
    private LocalDateTime scheduledAt;

    @NotBlank(message = "Địa điểm không được để trống")
    @Size(max = 200, message = "Địa điểm tối đa 200 ký tự")
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }

    public Long getCouncilId() {
        return councilId;
    }

    public void setCouncilId(Long councilId) {
        this.councilId = councilId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
