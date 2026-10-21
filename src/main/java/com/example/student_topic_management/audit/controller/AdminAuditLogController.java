package com.example.student_topic_management.audit.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.student_topic_management.audit.repository.AuditLogRepository;

@Controller
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {
    private final AuditLogRepository auditLogRepository;

    public AdminAuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String listAuditLogs(Model model) {
        // Load new 100 nhat ky
        model.addAttribute("logs", auditLogRepository.findAllWithActor(PageRequest.of(0, 100)).getContent());
        return "audit-logs/list";
    }
}
