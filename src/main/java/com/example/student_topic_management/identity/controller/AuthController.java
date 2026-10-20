package com.example.student_topic_management.identity.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String loginPage() {
        // Lay thong tin user dang nhap
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Neu user ton tai
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            // Day ve homepage
            return "redirect:/";
        }
        // Neu chua dang nhap thi hien thi login page
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
