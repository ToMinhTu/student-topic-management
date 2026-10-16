package com.example.student_topic_management.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // Dung @PreAuthorize tren service
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // cost 12 theo cau hinh
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider provider)
            throws Exception {
        // Tam thoi cho phep truy cap khong can login
        // http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        // .csrf(csrf -> csrf.disable());
        // return http.build();

        http.authenticationProvider(provider);

        http.authorizeHttpRequests(auth -> auth
                // Duong dan ma ai cung vao duoc
                .requestMatchers("/login", "/error", "/vendor/**", "/css/**").permitAll()
                // Phan quyen theo URL
                .requestMatchers("/management/announcements/**").hasAnyRole("ADMIN", "DEAN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/dean/**").hasRole("DEAN")
                .requestMatchers("/department/**").hasRole("DEPARTMENT_HEAD")
                .requestMatchers("/lecturer/**").hasRole("LECTURER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                // Moi request khac deu phai login
                .anyRequest().authenticated());

        // Cau hinh form login
        http.formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // Login thanh cong thi ve homepage
                .failureUrl("/login?error")
                .permitAll());

        // Cau hinh logout
        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout"));

        // Cau hinh trang loi 403 tu choi truy cap
        http.exceptionHandling(errors -> errors
                .accessDeniedPage("/access-denied"));
        // Khong disable CSRF
        return http.build();
    }
}
