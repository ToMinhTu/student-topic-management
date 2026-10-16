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
@Table(name = "lecturers")
@Getter 
@Setter 
@NoArgsConstructor 
public class LecturerProfile {
    @Id
    @Column(name = "user_id")
    private Long userid;

    // Bao cho spring lay ID tu AppUser lam ID cho bang nay
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "lecturer_code", nullable = false, unique = true, length = 30)
    private String lecturerCode;

    // Khoa ngoai tro toi department
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id")
    private Department department;

    public LecturerProfile(Long userid, AppUser user, String lecturerCode, Department department) {
        this.userid = userid;
        this.user = user;
        this.lecturerCode = lecturerCode;
        this.department = department;
    }
}
