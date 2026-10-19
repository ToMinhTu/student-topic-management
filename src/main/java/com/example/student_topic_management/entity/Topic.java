package com.example.student_topic_management.entity;

import com.example.student_topic_management.enums.TopicStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "topics",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"round_id", "code"}
    )
)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_id", nullable = false)
    private Long roundId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "proposed_by", nullable = false)
    private Long proposedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "varchar(20)")
    private TopicStatus status = TopicStatus.DRAFT;

    @Column(name = "department_decision", length = 10)
    private String departmentDecision;

    @Column(name = "department_reviewed_by")
    private Long departmentReviewedBy;

    @Column(name = "department_reviewed_at")
    private LocalDateTime departmentReviewedAt;

    @Column(name = "department_comment", columnDefinition = "TEXT")
    private String departmentComment;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public Topic() {
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Long getProposedBy() {
        return proposedBy;
    }

    public void setProposedBy(Long proposedBy) {
        this.proposedBy = proposedBy;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public String getDepartmentDecision() {
        return departmentDecision;
    }

    public void setDepartmentDecision(String departmentDecision) {
        this.departmentDecision = departmentDecision;
    }

    public Long getDepartmentReviewedBy() {
        return departmentReviewedBy;
    }

    public void setDepartmentReviewedBy(Long departmentReviewedBy) {
        this.departmentReviewedBy = departmentReviewedBy;
    }

    public LocalDateTime getDepartmentReviewedAt() {
        return departmentReviewedAt;
    }

    public void setDepartmentReviewedAt(LocalDateTime departmentReviewedAt) {
        this.departmentReviewedAt = departmentReviewedAt;
    }

    public String getDepartmentComment() {
        return departmentComment;
    }

    public void setDepartmentComment(String departmentComment) {
        this.departmentComment = departmentComment;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
