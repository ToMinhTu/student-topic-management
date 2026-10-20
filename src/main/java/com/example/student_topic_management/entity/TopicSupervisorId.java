package com.example.student_topic_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TopicSupervisorId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    public TopicSupervisorId() {
    }

    public TopicSupervisorId(Long topicId, Long lecturerId) {
        this.topicId = topicId;
        this.lecturerId = lecturerId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TopicSupervisorId other)) return false;

        return Objects.equals(topicId, other.topicId)
                && Objects.equals(lecturerId, other.lecturerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, lecturerId);
    }
}
