package com.example.student_topic_management.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Profile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.AnnouncementForm;
import com.example.student_topic_management.identity.entity.Announcement;
import com.example.student_topic_management.identity.service.AnnouncementService;

import jakarta.validation.Valid;

@Controller
@Profile("legacy-announcements")
@RequestMapping("/management/announcements")
public class AnnouncementManagementController {
    private final AnnouncementService service;

    public AnnouncementManagementController(AnnouncementService service) {
        this.service = service;
    }

    // Xem danh sach Nhap
    @GetMapping
    public String listDrafts(Model model) {
        model.addAttribute("drafts", service.getDrafts());
        return "management/announcements/list";
    }

    // Mo form soan Nhap
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new AnnouncementForm());
        return "management/announcements/form";
    }

    //
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        try {
            Announcement ann = service.getById(id);
            if (ann.getPublishedAt() != null)
                throw new BusinessException("Không thể sửa bản đã công bố");

            AnnouncementForm form = new AnnouncementForm();
            form.setId(ann.getId());
            form.setTitle(ann.getTitle());
            form.setContent(ann.getContent());
            model.addAttribute("form", form);
            return "management/announcements/form";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/management/announcements";
        }
    }

    // Luu ban nhap
    @PostMapping("/save")
    public String saveDraft(@Valid @ModelAttribute("form") AnnouncementForm form, BindingResult errors,
            RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "management/announcements/form";
        }
        try {
            service.saveDraft(form);
            redirect.addFlashAttribute("success", "Lưu bản nháp thành công!");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/management/announcements";
    }

    // Xoa ban nhap
    @PostMapping("/{id}/delete")
    public String deleteDraft(@PathVariable("id") Long id,
            RedirectAttributes redirect) {
        try {
            service.deleteDraft(id);
            redirect.addFlashAttribute("success", "Xóa bản nháp thành công!");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/management/announcements";
    }

    // Cong bo thong bao
    @PostMapping("/{id}/publish")
    public String publish(@PathVariable("id") Long id, RedirectAttributes redirect) {
        service.publish(id);
        redirect.addFlashAttribute("success", "Đã công bố thông báo lên hệ thống!");
        return "redirect:/management/announcements";
    }
}
