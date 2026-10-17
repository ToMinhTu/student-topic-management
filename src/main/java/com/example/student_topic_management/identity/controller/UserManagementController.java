package com.example.student_topic_management.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.service.UserManagementService;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {
    private UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    // Hien thi danh sach tai khoan
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userManagementService.getAllUsers());
        return "users/list";
    }

    // Chuyen trang thai khoa/mo khoa
    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            userManagementService.toggleUserLock(id);
            redirect.addFlashAttribute("success", "Cập nhật trạng thái tài khoản thành công.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
