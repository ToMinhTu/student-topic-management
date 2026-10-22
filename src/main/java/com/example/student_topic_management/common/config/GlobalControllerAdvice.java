package com.example.student_topic_management.common.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.student_topic_management.identity.service.AnnouncementService;

@ControllerAdvice
public class GlobalControllerAdvice {
    private final AnnouncementService announcementService;

    public GlobalControllerAdvice(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // Su dung dc o moi UI thymeleaf
    @ModelAttribute("announcementCount")
    public int getAnnouncementCount() {
        // Lay so luong danh sach thong bao da cong bo
        return announcementService.getPublishedList().size();
    }
}
