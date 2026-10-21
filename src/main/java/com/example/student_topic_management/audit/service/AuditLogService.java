package com.example.student_topic_management.audit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.audit.entity.AuditLog;
import com.example.student_topic_management.audit.repository.AuditLogRepository;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.service.CurrentUserService;

@Service 
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditLogService(AuditLogRepository auditLogRepository, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional (propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String targetType, Long targetId, String details) {
        AppUser actor = null;
        try {
            actor = currentUserService.getCurrentUser();
        } catch (Exception e) {
            // TODO: handle exception
        }

        AuditLog auditLog = new AuditLog(actor, action, targetType, targetId, details);
        auditLogRepository.save(auditLog);
    }
}
