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

    private final EmailOutboxRepository outboxRepository;
    private final EmailOutboxService outboxService;

    public AdminMailJobController(
            EmailOutboxRepository outboxRepository,
            EmailOutboxService outboxService) {
        this.outboxRepository = outboxRepository;
        this.outboxService = outboxService;
    }

    @GetMapping
    public String listJobs(Model model) {
        model.addAttribute("jobs", outboxRepository
                .findAllJobs(PageRequest.of(0, 100))
                .getContent());
        return "mail-jobs/list";
    }

    @PostMapping("/{id}/retry")
    public String retryJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            outboxService.retryFailedJob(id);
            redirectAttributes.addFlashAttribute("success", "Đã đưa email vào hàng đợi gửi lại");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/mail-jobs";
    }
}
