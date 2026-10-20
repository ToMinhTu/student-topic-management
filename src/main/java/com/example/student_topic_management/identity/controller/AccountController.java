package com.example.student_topic_management.identity.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.ChangePasswordForm;
import com.example.student_topic_management.identity.service.AccountSecurityService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final AccountSecurityService accountSecurityService;

    public AccountController(AccountSecurityService accountSecurityService) {
        this.accountSecurityService = accountSecurityService;
    }

    @GetMapping("/password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("form", new ChangePasswordForm());
        return "account/change-password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("form") ChangePasswordForm form, BindingResult errors,
            RedirectAttributes redirect, HttpServletRequest request, HttpServletResponse response) {
        if (errors.hasErrors()) {
            return "account/change-password";
        }

        try {
            accountSecurityService.changePassword(form);
        } catch (BusinessException ex) {
            errors.reject("business", ex.getMessage());
            return "account/change-password";
        }

        // Xoa thong tin dang nhap cua trinh duyet
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        // Sau khi doi password thi truyen param success ve url login
        return "redirect:/login?passwordChanged=true";
    }
}
