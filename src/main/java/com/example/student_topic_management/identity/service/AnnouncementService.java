package com.example.student_topic_management.identity.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.AnnouncementForm;
import com.example.student_topic_management.identity.entity.Announcement;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.repository.AnnouncementRepository;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final CurrentUserService currentUserService;

    public AnnouncementService(AnnouncementRepository announcementRepository, CurrentUserService currentUserService) {
        this.announcementRepository = announcementRepository;
        this.currentUserService = currentUserService;
    }

    // Chi ADMIN hoac DEAN moi duoc tao
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void createDraft(AnnouncementForm form) {
        AppUser author = currentUserService.getCurrentUser(); // lay tk dang nhap

        Announcement ann = new Announcement();
        ann.setTitle(form.getTitle());
        ann.setContent(form.getContent());
        ann.setAuthor(author);

        announcementRepository.save(ann);
    }

    // Chi ADMIN hoac DEAN moi dc bam cong bo
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void publish(Long id) {
        Announcement ann = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông báo"));

        // Neu da cong bo thi bo qua
        // khong doi ngay gio cap nhay lai lap lai
        if (ann.getPublishedAt() != null) {
            return;
        }

        ann.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        announcementRepository.save(ann);
    }
}
