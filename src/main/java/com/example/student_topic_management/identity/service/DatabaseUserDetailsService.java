package com.example.student_topic_management.identity.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.identity.repository.AppUserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;

    public DatabaseUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String supplied) {
        String username = supplied.strip().toLowerCase(Locale.ROOT);

        var user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Thông tin đăng nhập không hợp lệ"));

        // Chuyen list role thanh dinh dang ROLE_ADMIN,....
        String[] authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getCode())
                .toArray(String[]::new);

        // return object User cua Spring Security
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash()) // SPring tu so sanh vs mk nguoi dung go vao
                .disabled(!user.getEnabled())
                .authorities(authorities)
                .build();
    }
}
