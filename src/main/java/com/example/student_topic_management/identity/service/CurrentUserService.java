package com.example.student_topic_management.identity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.repository.StudentProfileRepository;

@Service
public class CurrentUserService {
    private final AppUserRepository userRepository;
    private final StudentProfileRepository studentRepo;
    private final LecturerProfileRepository lecturerRepo;

    public CurrentUserService(AppUserRepository userRepository, StudentProfileRepository studentRepo,
            LecturerProfileRepository lecturerRepo) {
        this.userRepository = userRepository;
        this.studentRepo = studentRepo;
        this.lecturerRepo = lecturerRepo;
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentUser() {
        // Lay thong tin xac thuc tu Context cua Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException("Người dùng chưa đăng nhập");
        }

        // auth.getName() la username
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản hiện tại"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    // Lay studentId
    @Transactional(readOnly = true)
    public Long getStudentId() {
        return studentRepo.findById(getCurrentUserId())
                .map(profile -> profile.getUserId())
                .orElseThrow(() -> new BusinessException("Tài khoản hiện tại không phải là sinh viên"));
    }

    // Lay lecturerId
    @Transactional(readOnly = true)
    public Long getLecturerId() {
        return lecturerRepo.findById(getCurrentUserId())
                .map(profile -> profile.getUserid())
                .orElseThrow(() -> new BusinessException("Tài khoản hiện tại không phải là giảng viên"));
    }

    // Lay departmentId
    @Transactional(readOnly = true)
    public Long getLecturerDepartmentId() {
        return lecturerRepo.findById(getCurrentUserId())
                .map(profile -> profile.getDepartment().getId())
                .orElseThrow(() -> new BusinessException("Tài khoản hiện tại không phải là giảng viên"));
    }
}
