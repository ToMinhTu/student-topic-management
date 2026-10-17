
package com.example.student_topic_management.entity;

import com.example.student_topic_management.enums.RoundType;
import com.example.student_topic_management.enums.RoundStatus;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registration_rounds")
public class RegistrationRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false,
            length = 20, columnDefinition = "varchar(20)")
    private RoundType roundType;

    @Column(name = "proposal_starts_at", nullable = false)
    private LocalDateTime proposalStartsAt;

    @Column(name = "proposal_ends_at", nullable = false)
    private LocalDateTime proposalEndsAt;

    @Column(name = "student_starts_at", nullable = false)
    private LocalDateTime studentStartsAt;

    @Column(name = "student_ends_at", nullable = false)
    private LocalDateTime studentEndsAt;

    @Column(name = "reviewer_due_at")
    private LocalDateTime reviewerDueAt;

    @Column(name = "council_date")
    private LocalDate councilDate;

    @Column(name = "report_due_at")
    private LocalDateTime reportDueAt;

    @Column(name = "group_min_size", nullable = false)
    private Byte groupMinSize = 3;

    @Column(name = "group_max_size", nullable = false)
    private Byte groupMaxSize = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,
            length = 20, columnDefinition = "varchar(20)")
    private RoundStatus status = RoundStatus.DRAFT;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false,
            insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public RegistrationRound() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoundType getRoundType() {
        return roundType;
    }

    public void setRoundType(RoundType roundType) {
        this.roundType = roundType;
    }

    public LocalDateTime getProposalStartsAt() {
        return proposalStartsAt;
    }

    public void setProposalStartsAt(LocalDateTime proposalStartsAt) {
        this.proposalStartsAt = proposalStartsAt;
    }

    public LocalDateTime getProposalEndsAt() {
        return proposalEndsAt;
    }

    public void setProposalEndsAt(LocalDateTime proposalEndsAt) {
        this.proposalEndsAt = proposalEndsAt;
    }

    public LocalDateTime getStudentStartsAt() {
        return studentStartsAt;
    }

    public void setStudentStartsAt(LocalDateTime studentStartsAt) {
        this.studentStartsAt = studentStartsAt;
    }

    public LocalDateTime getStudentEndsAt() {
        return studentEndsAt;
    }

    public void setStudentEndsAt(LocalDateTime studentEndsAt) {
        this.studentEndsAt = studentEndsAt;
    }

    public LocalDateTime getReviewerDueAt() {
        return reviewerDueAt;
    }

    public void setReviewerDueAt(LocalDateTime reviewerDueAt) {
        this.reviewerDueAt = reviewerDueAt;
    }

    public LocalDate getCouncilDate() {
        return councilDate;
    }

    public void setCouncilDate(LocalDate councilDate) {
        this.councilDate = councilDate;
    }

    public LocalDateTime getReportDueAt() {
        return reportDueAt;
    }

    public void setReportDueAt(LocalDateTime reportDueAt) {
        this.reportDueAt = reportDueAt;
    }

    public Byte getGroupMinSize() {
        return groupMinSize;
    }

    public void setGroupMinSize(Byte groupMinSize) {
        this.groupMinSize = groupMinSize;
    }

    public Byte getGroupMaxSize() {
        return groupMaxSize;
    }

    public void setGroupMaxSize(Byte groupMaxSize) {
        this.groupMaxSize = groupMaxSize;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
