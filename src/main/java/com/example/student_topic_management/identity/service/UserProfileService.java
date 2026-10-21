package com.example.student_topic_management.identity.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.ChangePasswordForm;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;

@Service
public class UserProfileService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserProfileService(AppUserRepository userRepository, PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void changePassword(String username, ChangePasswordForm form) {

        // Check password xac nhan
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp với mật khẩu mới!");
        }

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin tài khoản"));

        // check password cu co dung khong
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }

        // kiem tra mk moi co trung vs mk cu hay khong
        if (passwordEncoder.matches(form.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu mới không được giống mật khẩu hiện tại!");
        }

        // Doi mat mau
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);

        // Phat tin hieu thu hoi moi phien login cua user
        eventPublisher.publishEvent(new AccountAccessChanged(user.getId(),
                user.getUsername()));

    }
}
