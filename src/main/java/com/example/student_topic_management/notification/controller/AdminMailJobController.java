package com.example.student_topic_management.notification.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.notification.repository.EmailOutboxRepository;
import com.example.student_topic_management.notification.service.EmailOutboxService;

@Controller
@RequestMapping("/admin/mail-jobs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMailJobController {
    private final EmailOutboxRepository repo;
    private final EmailOutboxService service;

    public AdminMailJobController(EmailOutboxRepository repo, EmailOutboxService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public String listJobs(Model model) {
        model.addAttribute("jobs", repo.findAllJobs(PageRequest.of(0, 50)).getContent());
        return "mail-jobs/list";
    }

    @PostMapping("/{id}/retry")
    public String retryJob(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.retryFailedJob(id);
            redirect.addFlashAttribute("success", "Đã đặt lệnh thử lại thành công");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/mail-jobs";
    }
}
