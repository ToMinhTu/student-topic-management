// toan-D1
// toan-D1 - thêm chức năng quản lý hội đồng
package com.example.student_topic_management.assessment.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable

public class CouncilMemberId implements Serializable {

    private Long councilId;
    private Long lecturerId;

    public CouncilMemberId() {
    }

    public CouncilMemberId(Long councilId, Long lecturerId) {
        this.councilId = councilId;
        this.lecturerId = lecturerId;
    }

    public Long getCouncilId() {
        return councilId;
    }

    public void setCouncilId(Long councilId) {
        this.councilId = councilId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CouncilMemberId))
            return false;

        CouncilMemberId that = (CouncilMemberId) o;

        return Objects.equals(councilId, that.councilId)
                && Objects.equals(lecturerId, that.lecturerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(councilId, lecturerId);
    }
}