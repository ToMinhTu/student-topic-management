package com.example.student_topic_management.controller;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.DeanTopicReviewForm;
import com.example.student_topic_management.service.DeanTopicReviewService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dean/topics/review")
@PreAuthorize("hasRole('DEAN')")
public class DeanTopicReviewController {
    private final DeanTopicReviewService service;

    public DeanTopicReviewController(DeanTopicReviewService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("topics", service.getPendingTopics(page));
        return "dean/topics/review-list";
    }

    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable Long id, Model model) {
        var data = service.getReviewPage(id);
        model.addAttribute("topic", data.topic());
        model.addAttribute("canReview", data.canReview());
        if (!model.containsAttribute("reviewForm")) {
            model.addAttribute("reviewForm", new DeanTopicReviewForm());
        }
        return "dean/topics/review-detail";
    }

    @PostMapping("/{id:\\d+}")
    public String review(@PathVariable Long id,
                         @Valid @ModelAttribute("reviewForm") DeanTopicReviewForm form,
                         BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("error", "Vui lòng chọn quyết định hợp lệ và nhập lý do tối đa 4000 ký tự");
            redirect.addFlashAttribute("reviewForm", form);
            return detailRedirect(id);
        }
        try {
            service.review(id, form);
            redirect.addFlashAttribute("success", "Đã lưu quyết định của trưởng khoa");
            return "redirect:/dean/topics/review";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("reviewForm", form);
            return detailRedirect(id);
        }
    }

    private String detailRedirect(Long id) { return "redirect:/dean/topics/review/" + id; }

    @ExceptionHandler(BusinessException.class)
    public String businessError(BusinessException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dean/topics/review";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidParameter(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "ID đề tài hoặc số trang không hợp lệ");
        return "redirect:/dean/topics/review";
    }

    @ExceptionHandler(DataAccessException.class)
    public String dataError(Model model) {
        // Render a safe empty list instead of repeatedly redirecting to a failing database query.
        model.addAttribute("topics", Page.empty());
        model.addAttribute("error", "Không thể xử lý dữ liệu, vui lòng tải lại và thử lại");
        return "dean/topics/review-list";
    }
}