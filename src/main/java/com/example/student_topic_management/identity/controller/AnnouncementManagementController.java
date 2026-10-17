package com.example.student_topic_management.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.identity.dto.AnnouncementForm;
import com.example.student_topic_management.identity.repository.AnnouncementRepository;
import com.example.student_topic_management.identity.service.AnnouncementService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/management/announcements")
public class AnnouncementManagementController {
    private final AnnouncementService announcementService;
    private final AnnouncementRepository announcementRepository;

    public AnnouncementManagementController(AnnouncementService announcementService,
            AnnouncementRepository announcementRepository) {
        this.announcementService = announcementService;
        this.announcementRepository = announcementRepository;
    }

    // Xem danh sach Nhap
    @GetMapping
    public String listDrafts(Model model) {
        model.addAttribute("drafts", announcementRepository.findByPublishedAtIsNullOrderByCreatedAtDesc());
        return "management/announcements/list";
    }

    // Mo form soan Nhap
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new AnnouncementForm());
        return "management/announcements/new";
    }

    // Luu ban nhap
    @PostMapping
    public String createDraft(@Valid @ModelAttribute("form") AnnouncementForm form, BindingResult errors,
            RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "management/announcements/new";
        }
        announcementService.createDraft(form);
        redirect.addFlashAttribute("success", "Đã lưu bản nháp thành công!");
        return "redirect:/management/announcements";
    }

    // Cong bo thong bao
    @PostMapping("/{id}/publish")
    public String publish(@PathVariable("id") Long id, RedirectAttributes redirect) {
        announcementService.publish(id);
        redirect.addFlashAttribute("success", "Đã công bố thông báo lên hệ thống!");
        return "redirect:/management/announcements";
    }
}
