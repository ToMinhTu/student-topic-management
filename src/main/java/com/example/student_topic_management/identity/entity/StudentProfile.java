package com.example.student_topic_management.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
public class StudentProfile {
    @Id
    @Column(name = "user_id")
    private Long userId;

    // Bao cho spring lay ID tu AppUser lam ID cho bang nay
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "student_code", nullable = false, unique = true, length = 30)
    private String studentCode;

    @Column(name = "class_name", length = 100)
    private String className;

    // Khoa ngoai tro toi department
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    public StudentProfile(Long userId, AppUser user, String studentCode, String className, Department department) {
        this.userId = userId;
        this.user = user;
        this.studentCode = studentCode;
        this.className = className;
        this.department = department;
    }

}
