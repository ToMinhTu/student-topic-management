
package com.example.student_topic_management.registration.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;
import com.example.student_topic_management.identity.repository.StudentProfileRepository;

@Service
public class StudentAccessImpl implements StudentAccess {

    private final AppUserRepository userRepository;
    private final StudentProfileRepository studentRepository;
    private final JdbcTemplate jdbc;

    public StudentAccessImpl(
            AppUserRepository userRepository,
            StudentProfileRepository studentRepository,
            JdbcTemplate jdbc) {

        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.jdbc = jdbc;
    }

    // Lay sinh vien dang dang nhap
    @Override
    public Long requireCurrentStudentId() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        // Khong cho phep tai khoan chua dang nhap
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                    "Ban chua dang nhap");
        }

        String username = authentication.getName();

        AppUser user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Tai khoan khong ton tai"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalStateException(
                      "Tai khoan da bi vo hieu hoa");
}

        Long studentId = user.getId();

        if (!studentRepository.existsById(studentId)) {
            throw new IllegalStateException(
                    "Tai khoan khong phai sinh vien");
        }

        return studentId;
    }

    // Khoa sinh vien trong transaction
    @Override
    public void lockEnabledStudent(Long studentId) {

        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException(
                    "Ma sinh vien khong hop le");
        }

        if (!TransactionSynchronizationManager
                .isActualTransactionActive()) {

            throw new IllegalStateException(
                    "Thao tac phai nam trong transaction");
        }

        List<Long> ids = jdbc.queryForList(
                """
                SELECT s.user_id
                FROM students s
                JOIN users u ON u.id = s.user_id
                WHERE s.user_id = ?
                  AND u.enabled = 1
                FOR UPDATE
                """,
                Long.class,
                studentId
        );

        if (ids.isEmpty()) {
            throw new IllegalStateException(
                    "Sinh vien khong ton tai hoac da bi khoa");
        }
    }
}
