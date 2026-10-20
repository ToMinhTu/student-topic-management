package com.example.student_topic_management.identity.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.UserListDTO;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.repository.AppUserRepository;

@Service
public class UserManagementService {
    private final AppUserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserManagementService(AppUserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<UserListDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserListDTO dto = new UserListDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setFullname(user.getFullname());
            dto.setEmail(user.getEmail());
            dto.setEnabled(user.getEnabled());

            // Noi danh sach Role thanh 1 chuoi
            String roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.joining(", "));
            dto.setRoleNames(roles);
            return dto;
        }).collect(Collectors.toList());
    }

    // Chi Admin moi thuc hien duoc
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void toggleUserLock(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại"));

        // check tai khoan bi khoa co phai la ADMIN
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getCode().equals("ADMIN"));

        // Neu la ADMIN, kiem tra co phai la ADMIN cuoi cung hay khong
        if (isAdmin && user.getEnabled() && userRepository.countActiveAdmins() <= 1) {
            throw new BusinessException("Không thể khoá quản trị viên duy nhất còn hoạt động");
        }

        // Dao trang thai (Khoa -> Mo, Mo -> Khoa)
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);

        // Neu tai khoan bi khoa. Phat su kien thu hoi moi session cua tai khoan nay
        if (!user.getEnabled()) {
            eventPublisher.publishEvent(new AccountAccessChanged(user.getId(), user.getUsername()));
        }
    }
}
