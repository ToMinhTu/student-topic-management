package com.example.student_topic_management.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize("hasRole('ADMIN')")
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12); // Ma hoa chuan pass Bcrypt
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider(
                        UserDetailsService userDetailsService,
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
                                                "/css/**",
                                                "/images/**")
                                .permitAll()

                                // Quan ly thong bao
                                .requestMatchers("/management/announcements/**")
                                .hasAnyRole("ADMIN", "DEAN")

                                // B02 - De xuat de tai
                                .requestMatchers("/topics", "/topics/**")
                                .hasAnyRole("LECTURER", "STUDENT")

                                // Chuc nang hoi dong tu main
                                .requestMatchers("/councils/**")
                                .hasAnyRole("DEAN", "DEPARTMENT_HEAD", "LECTURER")

                                // C05 - Nop va tai bao cao PDF
                                .requestMatchers("/reports", "/reports/**")
                                .hasRole("STUDENT")

                                // Phan quyen cac vai tro
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/dean/**").hasRole("DEAN")
                                .requestMatchers("/department/**").hasRole("DEPARTMENT_HEAD")
                                .requestMatchers("/lecturer/**").hasRole("LECTURER")
                                .requestMatchers("/student/**").hasRole("STUDENT")

                                // Cac URL khac phai dang nhap
                                .anyRequest().authenticated());

                http.formLogin(login -> login
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/", true) // Redirect ve "/" sau khi login success
                                .failureUrl("/login?error=true")
                                .permitAll());

                http.sessionManagement(session -> session
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry())
                                .expiredUrl("/login?expired=true"));

                http.logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout=true")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll());

                http.exceptionHandling(errors -> errors
                                .accessDeniedPage("/access-denied"));

                // Giu nguyen CSRF mac dinh
                return http.build();
        }
}
