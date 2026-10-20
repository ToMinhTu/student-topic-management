package com.example.student_topic_management.identity.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
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

    // =========================== ADMIN VA DEAN ===========================
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public List<Announcement> getManagementList() {
        // Lay 50 thong bao moi nhat
        return announcementRepository.findAllWithAuthor(PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public Announcement getForManagementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông báo"));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void createDraft(AnnouncementForm form) {
        // authorId lay tu principal
        AppUser author = currentUserService.getCurrentUser();

        Announcement announcement = new Announcement();
        announcement.setTitle(form.getTitle());
        announcement.setContent(form.getContent());
        announcement.setAuthor(author);
        // publishedAt tu dong la NULL (Ban nhap)

        announcementRepository.save(announcement);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void updateDraft(Long id, AnnouncementForm form) {
        Announcement announcement = getForManagementById(id);

        // chi sua ban nhap
        if (announcement.getPublishedAt() != null) {
            throw new BusinessException("Không thể sửa thông báo đã được công bố");
        }

        announcement.updateDraft(form.getTitle(), form.getContent());
        announcementRepository.save(announcement);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void deleteDraft(Long id) {
        Announcement announcement = getForManagementById(id);

        // chi xoa ban nhap
        if (announcement.getPublishedAt() != null) {
            throw new BusinessException("Không thể xoá thông báo đã được công bố");
        }

        announcementRepository.delete(announcement);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'DEAN')")
    public void publish(Long id) {
        Announcement announcement = getForManagementById(id);

        // Neu da cong bo thi bo qua
        // khong doi ngay gio cap nhay lai lap lai
        if (announcement.getPublishedAt() != null) {
            return;
        }

        // Set thoi gian he thong server (UTC)
        announcement.publish(LocalDateTime.now(ZoneOffset.UTC));
        announcementRepository.save(announcement);
    }

    // =========================== TAT CA USER ===========================
    @Transactional(readOnly = true)
    public List<Announcement> getPublishedList() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return announcementRepository.findPublished(now, PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    public Announcement getPublishedById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Thông báo không tồn tại"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (announcement.getPublishedAt() == null || announcement.getPublishedAt().isAfter(now)) {
            throw new BusinessException("Thông báo này chưa được công bố");
        }

        return announcement;
    }
}
