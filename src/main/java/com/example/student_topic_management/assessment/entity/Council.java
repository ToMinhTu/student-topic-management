// toan-D1
package com.example.student_topic_management.assessment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "councils")
public class Council {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouncilStatus status = CouncilStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Council() {
        this.status = CouncilStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Council(Long roundId, String name) {
        this.roundId = roundId;
        this.name = name;
        this.status = CouncilStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    public CouncilStatus getStatus() {
        return status;
    }

    public void setStatus(CouncilStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Long getCreatedBy() {
    return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
    }
}