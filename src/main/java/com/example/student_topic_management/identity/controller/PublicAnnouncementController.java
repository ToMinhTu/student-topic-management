package com.example.student_topic_management.identity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.entity.Announcement;
import com.example.student_topic_management.identity.service.AnnouncementService;

@Controller
@RequestMapping("/announcements")
public class PublicAnnouncementController {
    private final AnnouncementService service;

    public PublicAnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    // Danh sach da cong bo
    @GetMapping
    public String listPublicAnnouncements(Model model) {
        model.addAttribute("announcements", service.getPublishedList());
        return "announcements/public-list";
    }

    // Chi tiet thong bao
    @GetMapping("/{id}")
    public String viewAnnouncementDetail(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("announcement", service.getPublishedById(id));
            return "announcements/detail";
        } catch (BusinessException ex) {
            // Bat loi va chuyen huong ve ds thong bao kem canh bao
            // redirect.addFlashAttribute("error", "Không tìm thấy thông báo hoặc bạn không
            // có quyền xem.");
            // return "redirect:/announcements";

            // return http 404 not found -> redirect to access-denied
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }

    }
}
