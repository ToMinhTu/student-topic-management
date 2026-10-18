// toan-D3
// toan-D3 - thêm chức năng phân công chấm điểm
package com.example.student_topic_management.assessment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reviewer_assignments")
public class ReviewerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_id", nullable = false)
    private Long registrationId;

    @Column(name = "lecturer_id", nullable = false)
    private Long lecturerId;

    @Column(name = "assignment_type", nullable = false, length = 20)
    private String assignmentType = "REVIEWER";

    @Column(name = "assigned_by", nullable = false)
    private Long assignedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ReviewerAssignment() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(Long lecturerId) {
        this.lecturerId = lecturerId;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}