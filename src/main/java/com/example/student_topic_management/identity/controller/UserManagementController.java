package com.example.student_topic_management.identity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.service.UserManagementService;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {
    @Autowired
    private UserManagementService userManagementService;

    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable("id") Long userId, RedirectAttributes redirect) {
        try {
            userManagementService.lockAccount(userId);
            redirect.addFlashAttribute("success", "Đã khoá tài khoản và thu hồi phiên thành công.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        // back to homepage
        return "redirect:/";
    }
}
