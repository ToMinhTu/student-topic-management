package com.example.student_topic_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "topic_supervisors")
public class TopicSupervisor {

    @EmbeddedId
    private TopicSupervisorId id;

    @Column(name = "slot_no", nullable = false)
    private Byte slotNo;

    public TopicSupervisor() {
    }

    public TopicSupervisor(Long topicId, Long lecturerId, Byte slotNo) {
        this.id = new TopicSupervisorId(topicId, lecturerId);
        this.slotNo = slotNo;
    }

    public TopicSupervisorId getId() {
        return id;
    }

    public void setId(TopicSupervisorId id) {
        this.id = id;
    }

    public Byte getSlotNo() {
        return slotNo;
    }

    public void setSlotNo(Byte slotNo) {
        this.slotNo = slotNo;
    }
}
