package com.example.student_topic_management.identity.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.CreateLecturerForm;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.entity.LecturerProfile;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.repository.AppUserRepository;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.repository.RoleRepository;

@Service
public class LecturerAccountService {
    private AppUserRepository userRepository;
    private LecturerProfileRepository lecturerProfileRepository;
    private RoleRepository roleRepository;
    private DepartmentRepository departmentRepository;
    private PasswordEncoder passwordEncoder;

    public LecturerAccountService(AppUserRepository userRepository, LecturerProfileRepository lecturerProfileRepository,
            RoleRepository roleRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.lecturerProfileRepository = lecturerProfileRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createLecturerAccount(CreateLecturerForm form) {
        // Chuan hoa du lieu
        String username = form.getUsername().trim().toLowerCase(Locale.ROOT);
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        String lecturerCode = form.getLecturerCode().trim();

        // check du lieu ton tai
        if (userRepository.existsByUsername(username))
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        if (userRepository.existsByEmail(email))
            throw new BusinessException("Email đã tồn tại");
        if (lecturerProfileRepository.existsByLecturerCode(lecturerCode))
            throw new BusinessException("Mã giảng viên đã tồn tại");

        Department department = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new BusinessException("Bộ môn không tồn tại"));

        // Create AppUser
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullname(form.getFullname().trim());
        user.setEnabled(true);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));

        // gan quyen mac dinh LECTURER
        Role lecturerRole = roleRepository.findByCode("LECTURER")
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình: Thiếu role LECTURER"));
        user.getRoles().add(lecturerRole);

        // Neu form co chon Truong khoa, gan them quyen DEAN
        if (form.isAssignDean()) {
            Role deanRole = roleRepository.findByCode("DEAN")
                    .orElseThrow(() -> new BusinessException("Lỗi cấu hình: Thiếu role DEAN"));
            user.getRoles().add(deanRole);
        }

        // Neu form co chon Truong bo mon, gan them quyen DEPARTMENT_HEAD
        if (form.isAssignDepartmentHead()) {
            Role headRole = roleRepository.findByCode("DEPARTMENT_HEAD")
                    .orElseThrow(() -> new BusinessException("Lỗi cấu hình: Thiếu role DEPARTMENT_HEAD"));
            user.getRoles().add(headRole);
        }

        userRepository.save(user);

        // Create Lecturer Profile
        LecturerProfile profile = new LecturerProfile();
        profile.setUser(user);
        profile.setLecturerCode(lecturerCode);
        profile.setDepartment(department);

        lecturerProfileRepository.save(profile);
    }
}
