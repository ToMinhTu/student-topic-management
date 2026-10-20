package com.example.student_topic_management.registration.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import jakarta.persistence.*;

@Entity
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "slot_no", nullable = false)
    private Byte slotNo;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "active_student_id",
            insertable = false, updatable = false)
    private Long activeStudentId;

    @Column(name = "active_slot_no",
            insertable = false, updatable = false)
    private Byte activeSlotNo;

    public GroupMember() {
    }

    public GroupMember(StudentGroup group, Long studentId, Byte slotNo) {
        this.group = group;
        this.studentId = studentId;
        this.slotNo = slotNo;
    }

    @PrePersist
    void initializeTime() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public void setGroup(StudentGroup group) {
        this.group = group;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Byte getSlotNo() {
        return slotNo;
    }

    public void setSlotNo(Byte slotNo) {
        this.slotNo = slotNo;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public Long getActiveStudentId() {
        return activeStudentId;
    }

    public Byte getActiveSlotNo() {
        return activeSlotNo;
    }
}