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

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.AnnouncementForm;
import com.example.student_topic_management.identity.entity.Announcement;
import com.example.student_topic_management.identity.service.AnnouncementService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/management/announcements")
public class ManagementAnnouncementController {
    private final AnnouncementService service;

    public ManagementAnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    // Xem danh sach Nhap
    @GetMapping
    public String listManagementAnnouncements(Model model) {
        model.addAttribute("announcements", service.getManagementList());
        return "announcements/management-list";
    }

    // Mo form soan Nhap
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new AnnouncementForm());
        return "announcements/form";
    }

    @PostMapping("/new")
    public String createDraft(@Valid @ModelAttribute("form") AnnouncementForm form,
            BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "announcements/form";
        }
        try {
            service.createDraft(form);
            redirect.addFlashAttribute("success", "Đã tạo bản nháp thành công");
            return "redirect:/management/announcements";
        } catch (BusinessException ex) {
            errors.reject("business", ex.getMessage());
            return "announcements/form";
        }
    }

    //
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Announcement ann = service.getForManagementById(id);
            if (ann.getPublishedAt() != null) {
                redirect.addFlashAttribute("error", "Không thể sửa thông báo đã công bố");
                return "redirect:/management/announcements";
            }

            AnnouncementForm form = new AnnouncementForm();
            form.setId(ann.getId());
            form.setTitle(ann.getTitle());
            form.setContent(ann.getContent());
            model.addAttribute("form", form);
            return "announcements/form";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/management/announcements";
        }
    }

    // Luu ban nhap
    @PostMapping("/{id}/edit")
    public String updateDraft(@PathVariable Long id, @Valid @ModelAttribute("form") AnnouncementForm form,
            BindingResult errors,
            RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "announcements/form";
        }
        try {
            service.updateDraft(id, form);
            redirect.addFlashAttribute("success", "Đã cập nhật bản nháp thành công!");
            return "redirect:/management/announcements";
        } catch (BusinessException ex) {
            errors.reject("business", ex.getMessage());
            return "announcements/form";
        }
    }

    // Xoa ban nhap
    @PostMapping("/{id}/delete")
    public String deleteDraft(@PathVariable Long id,
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
    public String publishAnnouncement(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.publish(id);
            redirect.addFlashAttribute("success", "Thông báo đã được công bố chính thức!");

        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());

        }
        return "redirect:/management/announcements";

    }
}
