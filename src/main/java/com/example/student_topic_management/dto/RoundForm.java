
package com.example.student_topic_management.dto;

import com.example.student_topic_management.enums.RoundType;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RoundForm {

    @NotBlank(message = "Vui lòng nhập tên đợt")
    @Size(max = 200, message = "Tên đợt tối đa 200 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn loại đợt")
    private RoundType roundType;

    @NotNull(message = "Vui lòng nhập ngày bắt đầu đề xuất")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime proposalStartsAt;

    @NotNull(message = "Vui lòng nhập ngày kết thúc đề xuất")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime proposalEndsAt;

    @NotNull(message = "Vui lòng nhập ngày bắt đầu đăng ký")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime studentStartsAt;

    @NotNull(message = "Vui lòng nhập ngày kết thúc đăng ký")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime studentEndsAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reviewerDueAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate councilDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reportDueAt;

    @NotNull(message = "Vui lòng nhập số lượng tối thiểu")
    @Min(value = 1, message = "Tối thiểu phải từ 1")
    @Max(value = 5, message = "Không vượt quá 5")
    private Integer groupMinSize = 3;

    @NotNull(message = "Vui lòng nhập số lượng tối đa")
    @Min(value = 1, message = "Tối thiểu phải từ 1")
    @Max(value = 5, message = "Không vượt quá 5")
    private Integer groupMaxSize = 3;

    public RoundForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.strip();
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

    public Integer getGroupMinSize() {
        return groupMinSize;
    }

    public void setGroupMinSize(Integer groupMinSize) {
        this.groupMinSize = groupMinSize;
    }

    public Integer getGroupMaxSize() {
        return groupMaxSize;
    }

    public void setGroupMaxSize(Integer groupMaxSize) {
        this.groupMaxSize = groupMaxSize;
    }
}
