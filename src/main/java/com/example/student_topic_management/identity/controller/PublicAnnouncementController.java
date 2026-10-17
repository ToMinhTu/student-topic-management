package com.example.student_topic_management.identity.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.entity.Announcement;
import com.example.student_topic_management.identity.repository.AnnouncementRepository;

@Controller
@RequestMapping("/announcements")
public class PublicAnnouncementController {
    private final AnnouncementRepository announcementRepository;

    public PublicAnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    // Danh sach da cong bo
    @GetMapping
    public String listPublic(Model model) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        model.addAttribute("announcements", announcementRepository
                .findByPublishedAtIsNotNullAndPublishedAtLessThanEqualOrderByPublishedAtDesc(now));
        return "announcements/list";
    }

    // Chi tiet thong bao
    @GetMapping("/{id}")
    public String viewDetail(@PathVariable("id") Long id, Model model) {
        Announcement ann = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông báo"));
        // Chan bao mat: Neu thong bao chua cong bo, khong cho xem noi dung
        if (ann.getPublishedAt() == null || ann.getPublishedAt().isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new BusinessException("Lỗi truy cập: Thông báo này chưa được công bố!");
        }

        model.addAttribute("announcement", ann);
        return "announcements/detail";
    }
}
