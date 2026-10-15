package com.example.student_topic_management.identity.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.CreateStudentForm;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.entity.StudentProfile;
import com.example.student_topic_management.identity.repository.AppUserRepository;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.identity.repository.RoleRepository;
import com.example.student_topic_management.identity.repository.StudentProfileRepository;

@Service
public class StudentAccountService {
    private AppUserRepository userRepository;
    private StudentProfileRepository studentProfileRepository;
    private RoleRepository roleRepository;
    private DepartmentRepository departmentRepository;
    private PasswordEncoder passwordEncoder;

    public StudentAccountService(AppUserRepository userRepository, StudentProfileRepository studentProfileRepository,
            RoleRepository roleRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // Neu co loi xay ra, se rollback lai ban dau
    public void createStudentAccount(CreateStudentForm form) {
        // chuan hoa du lieu
        String username = form.getUsername().trim().toLowerCase(Locale.ROOT);
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        String studentCode = form.getStudentCode().trim();

        // check du lieu ton tai
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email đã tồn tại");
        }
        if (studentProfileRepository.existsByStudentCode(studentCode)) {
            throw new BusinessException("Mã số sinh viên đã tồn tại");
        }

        // Lay du lieu role (student) va department tu db
        Role studentRole = roleRepository.findByCode("STUDENT")
                .orElseThrow(() -> new BusinessException("Lỗi hệ thống: Chưa cấu hình Role STUDENT"));
        Department department = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new BusinessException("Bộ môn không tồn tại"));

        // Create AppUser
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullname(form.getFullname().trim());
        user.setEnabled(true);
        // Bam mat khau
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.getRoles().add(studentRole);

        // Luu user de co ID
        userRepository.save(user);

        // Create StudentProfile
        StudentProfile profile = new StudentProfile();
        profile.setUser(user); // JPA MapsId lay ID tu user nay gan vao profile
        profile.setStudentCode(studentCode);
        profile.setClassName(form.getClassName());
        profile.setDepartment(department);

        // Luu profile
        studentProfileRepository.save(profile);
    }
}
