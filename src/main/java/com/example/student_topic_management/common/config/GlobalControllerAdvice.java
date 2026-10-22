package com.example.student_topic_management.common.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.identity.service.AnnouncementService;

@ControllerAdvice
public class GlobalControllerAdvice {
    private static final Pattern REPORT_UPLOAD_PATH =
            Pattern.compile("(?:^|.*/)reports/(\\d+)/upload$");

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

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleOversizedReport(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "error",
                "File PDF không được vượt quá 10 MB"
        );

        return reportRedirect(request);
    }

    @ExceptionHandler(MultipartException.class)
    public String handleInvalidMultipartRequest(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Không thể đọc file tải lên. Vui lòng chọn lại file PDF"
        );

        return reportRedirect(request);
    }

    private String reportRedirect(HttpServletRequest request) {
        Matcher matcher = REPORT_UPLOAD_PATH.matcher(request.getRequestURI());

        if (matcher.matches()) {
            return "redirect:/reports/" + matcher.group(1);
        }

        return "redirect:/groups";
    }
}
