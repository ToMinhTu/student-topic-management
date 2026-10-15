// toan-D1
package com.example.student_topic_management.assessment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "council_members")
public class CouncilMember {

    @EmbeddedId
    private CouncilMemberId id;

    @Column(name = "slot_no", nullable = false)
    private Byte slotNo;

    public CouncilMember() {
    }

    public CouncilMember(CouncilMemberId id, Byte slotNo) {
        this.id = id;
        this.slotNo = slotNo;
    }

    public CouncilMemberId getId() {
        return id;
    }

    public void setId(CouncilMemberId id) {
        this.id = id;
    }

    public void setSlotNo(Byte slotNo) {
        this.slotNo = slotNo;
    }

    public Byte getSlotNo() {
        return slotNo;
    }
}