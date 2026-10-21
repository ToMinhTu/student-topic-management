package com.example.student_topic_management.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TopicSupervisorForm {
    @NotNull(message = "Vui lòng chọn giảng viên")
    @Positive(message = "Giảng viên không hợp lệ")
    private Long lecturerId;

    @NotNull(message = "Vui lòng chọn vị trí")
    @Min(value = 1, message = "Vị trí phải là 1 hoặc 2")
    @Max(value = 2, message = "Vị trí phải là 1 hoặc 2")
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
