package com.example.student_topic_management.identity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.ChangePasswordForm;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;

@Service
public class AccountSecurityService {
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public void changePassword(ChangePasswordForm form) {
        AppUser currenUser = currentUserService.getCurrentUser();

        // check password co khop vs db
        if (!passwordEncoder.matches(form.getCurrentPassword(), currenUser.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }
        // Check password xac nhan
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }

        // Doi mat mau
        currenUser.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(currenUser);

        // Phat tin hieu thu hoi moi phien login cua user
        eventPublisher.publishEvent(new AccountAccessChanged(currenUser.getId(), currenUser.getUsername()));

    }
}
