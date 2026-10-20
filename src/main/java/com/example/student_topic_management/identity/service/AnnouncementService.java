package com.example.student_topic_management.identity.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.AnnouncementForm;
import com.example.student_topic_management.identity.entity.Announcement;
import com.example.student_topic_management.identity.repository.AnnouncementRepository;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final CurrentUserService currentUserService;

    public AnnouncementService(AnnouncementRepository announcementRepository, CurrentUserService currentUserService) {
        this.announcementRepository = announcementRepository;
        this.currentUserService = currentUserService;
    }

    public List<Announcement> getDrafts() {
        return announcementRepository.findByPublishedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Announcement> getPublished() {
        return announcementRepository.findByPublishedAtIsNotNullAndPublishedAtLessThanEqualOrderByPublishedAtDesc(
                LocalDateTime.now(ZoneOffset.UTC));
    }

    public Announcement getById(Long id) {
        return announcementRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy thông báo"));
    }

    // Chi ADMIN hoac DEAN moi duoc tao
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void saveDraft(AnnouncementForm form) {
        // 1. Tao moi nhap
        if (form.getId() == null) {
            Announcement ann = new Announcement();
            ann.setTitle(form.getTitle());
            ann.setContent(form.getContent());
            ann.setAuthor(currentUserService.getCurrentUser()); // tu dong lay nguoi dang nhap
            announcementRepository.save(ann);
        }
        // 2. sua nhap
        else {
            Announcement ann = getById(form.getId());
            if (ann.getPublishedAt() != null) {
                throw new BusinessException("Không thể sửa thông báo đã được công bố");
            }
            ann.setTitle(form.getTitle());
            ann.setContent(form.getContent());
            announcementRepository.save(ann);
        }
    }

    // Chi ADMIN hoac DEAN moi duoc xoa
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void deleteDraft(Long id) {
        Announcement ann = getById(id);
        if (ann.getPublishedAt() != null) {
            throw new BusinessException("Không thể xóa thông báo đã công bố");
        }
        announcementRepository.delete(ann);
    }

    // Chi ADMIN hoac DEAN moi dc bam cong bo
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void publish(Long id) {
        Announcement ann = getById(id);

        // Neu da cong bo thi bo qua
        // khong doi ngay gio cap nhay lai lap lai
        if (ann.getPublishedAt() != null) {
            return;
        }

        ann.setPublishedAt(LocalDateTime.now(ZoneOffset.UTC));
        announcementRepository.save(ann);
    }
}
