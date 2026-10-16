package com.example.student_topic_management.identity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;

@Service
public class CurrentUserService {
    private final AppUserRepository userRepository;

    public CurrentUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
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
}
