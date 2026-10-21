package com.example.student_topic_management.identity.controller;

import java.security.Principal;

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
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AppUserRepository;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.repository.StudentProfileRepository;
import com.example.student_topic_management.identity.service.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final AppUserRepository userRepository;
    private final StudentProfileRepository studentRepo;
    private final LecturerProfileRepository lecturerRepo;

    public UserProfileController(UserProfileService userProfileService, AppUserRepository userRepository,
            StudentProfileRepository studentRepo, LecturerProfileRepository lecturerRepo) {
        this.userProfileService = userProfileService;
        this.userRepository = userRepository;
        this.studentRepo = studentRepo;
        this.lecturerRepo = lecturerRepo;
    }

    @GetMapping
    public String viewProfile(Principal principal, Model model) {
        // Lay username cua nguoi dang dang nhap
        String username = principal.getName();
        AppUser user = userRepository.findByUsername(username).orElseThrow();

        model.addAttribute("user", user);

        // Lay thong tin profile tuong ung
        studentRepo.findById(user.getId()).ifPresent(sp -> model.addAttribute("studentProfile", sp));
        lecturerRepo.findById(user.getId()).ifPresent(lp -> model.addAttribute("lecturerProfile", lp));

        // Khoi tao form doi mk
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new ChangePasswordForm());
        }

        return "users/profile";
    }

    @PostMapping("/password")
    public String changePassword(Principal principal, @Valid @ModelAttribute("passwordForm") ChangePasswordForm form,
            BindingResult errors,
            RedirectAttributes redirect, HttpServletRequest request, HttpServletResponse response) {
        // Nhap sai -> tra lai form kem loi validation
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.passwordForm", errors);
            redirect.addFlashAttribute("passwordForm", form);
            return "redirect:/profile";
        }

        try {
            userProfileService.changePassword(principal.getName(), form);
            // redirect.addFlashAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            // Xoa thong tin dang nhap cua trinh duyet
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            // Sau khi doi password thi truyen param success ve url login
            return "redirect:/login?passwordChanged=true";
            // return "redirect:/logout";
        } catch (BusinessException ex) {
            // errors.reject("business", ex.getMessage());
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("passwordForm", form);
            return "redirect:/profile";
        }

    }
}
