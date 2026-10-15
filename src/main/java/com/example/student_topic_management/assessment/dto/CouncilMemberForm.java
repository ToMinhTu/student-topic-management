// toan-D1
package com.example.student_topic_management.assessment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CouncilMemberForm {

    @NotNull(message = "Mã giảng viên không được để trống")
    @Positive(message = "Mã giảng viên phải là số dương")
    private Long lecturerId;

    @NotNull(message = "Vị trí không được để trống")
    @Min(value = 1, message = "Vị trí phải từ 1 đến 5")
    @Max(value = 5, message = "Vị trí phải từ 1 đến 5")
    private Integer slotNo;

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public Integer getSlotNo() {
        return slotNo;
    }

    public void setSlotNo(Integer slotNo) {
        this.slotNo = slotNo;
    }
}