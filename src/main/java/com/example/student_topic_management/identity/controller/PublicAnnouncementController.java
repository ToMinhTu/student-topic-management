package com.example.student_topic_management.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String listPublic(Model model) {
        model.addAttribute("announcements", service.getPublished());
        return "announcements/list";
    }

    // Chi tiet thong bao
    @GetMapping("/{id}")
    public String viewDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        try {
            Announcement ann = service.getById(id);

            // Chan bao mat: Neu thong bao chua cong bo, khong cho xem noi dung
            if (ann.getPublishedAt() == null) {
                throw new BusinessException("Lỗi truy cập: Thông báo này đang là bản nháp, chưa được công bố!");
            }

            model.addAttribute("announcement", ann);
            return "announcements/detail";
        } catch (BusinessException ex) {
            // Bat loi va chuyen huong ve ds thong bao kem canh bao
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/announcements";
        }

    }
}
