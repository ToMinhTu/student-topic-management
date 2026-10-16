package com.example.student_topic_management.identity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;

@Service
public class UserManagementService {
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Chi Admin moi thuc hien duoc
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void lockAccount(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Tài khoảng không tồn tại"));

        // check tai khoan bi khoa co phai la ADMIN
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getCode().equals("ADMIN"));

        // Neu la ADMIN, kiem tra co phai la ADMIN duy nhat enable
        if (isAdmin && userRepository.countActiveAdmins() <= 1) {
            throw new BusinessException("Không thể khoá quản trị viên duy nhất còn hoạt động");
        }

        // Khoa tai khoan
        user.setEnabled(false);
        userRepository.save(user);

        // Phat tien hieu thu hoi moi phien cua tai khoan nay
        eventPublisher.publishEvent(new AccountAccessChanged(user.getId(), user.getUsername()));
    }
}
