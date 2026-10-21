package com.example.student_topic_management.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            @Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
            PasswordEncoder encoder) {

        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider provider) throws Exception {

        http.authenticationProvider(provider);

        http.authorizeHttpRequests(auth -> auth
                // Duong dan cong khai
                .requestMatchers(
                        "/login",
                        "/error",
                        "/vendor/**",
                        "/css/**"
                ).permitAll()

                // Quan ly thong bao
                .requestMatchers("/management/announcements/**")
                .hasAnyRole("ADMIN", "DEAN")

                .requestMatchers("/topic-registration", "/topic-registration/**")
                .hasRole("STUDENT")
                .requestMatchers("/dean/registrations/**")
                .hasRole("DEAN")

                // B02 - Sinh vien chi duoc xem; giang vien duoc tao va quan ly de xuat
                .requestMatchers(HttpMethod.GET, "/topics/my", "/topics/*")
                .hasAnyRole("LECTURER", "STUDENT")
                .requestMatchers("/topics/new", "/topics", "/topics/*/edit", "/topics/*/submit", "/topics/*/delete", "/topics/*")
                .hasRole("LECTURER")

                // Chuc nang hoi dong tu main
                .requestMatchers("/councils/**")
                .hasAnyRole("DEAN", "DEPARTMENT_HEAD", "LECTURER")

                // Phan quyen cac vai tro
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/dean/**").hasRole("DEAN")
                .requestMatchers("/department/**").hasRole("DEPARTMENT_HEAD")
                .requestMatchers("/lecturer/**").hasRole("LECTURER")
                .requestMatchers("/student/**").hasRole("STUDENT")

                // Cac URL khac phai dang nhap
                .anyRequest().authenticated()
        );

        http.formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
        );

        http.sessionManagement(session -> session
                .maximumSessions(-1)
                .sessionRegistry(sessionRegistry())
                .expiredUrl("/login?expired")
        );

        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
        );

        http.exceptionHandling(errors -> errors
                .accessDeniedPage("/access-denied")
        );

        // Giu nguyen CSRF mac dinh
        return http.build();
    }
}
