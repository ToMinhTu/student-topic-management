package com.example.student_topic_management.identity.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.repository.AppUserRepository;
import com.example.student_topic_management.identity.repository.RoleRepository;

@Component
public class AdminSeeder implements CommandLineRunner {
    private AppUserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public AdminSeeder(AppUserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Chi tao tok neu chua co user nao ten admin
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setFullname("Quản trị viên hệ thống");
            admin.setEnabled(true);

            admin.setPasswordHash(passwordEncoder.encode("admin123"));

            Role adminRole = roleRepository.findByCode("ADMIN").orElseThrow();
            admin.getRoles().add(adminRole);

            userRepository.save(admin);
            System.out.println("Đã khởi tạo tài khoản ADMIN thành công (u: admin, p: admin123)");
        }
    }
}
