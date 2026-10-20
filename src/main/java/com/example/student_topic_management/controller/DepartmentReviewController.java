package com.example.student_topic_management.controller;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.DepartmentReviewForm;
import com.example.student_topic_management.service.DepartmentReviewService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Controller
@RequestMapping("/department/topics")
@PreAuthorize("hasRole('DEPARTMENT_HEAD')")
public class DepartmentReviewController {
    private final DepartmentReviewService service;

    public DepartmentReviewController(DepartmentReviewService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("topics", service.getPendingTopics(page));
        return "department/topics/list";
    }

    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable Long id, Model model) {
        var data = service.getReviewPage(id);
        model.addAttribute("topic", data.topic());
        model.addAttribute("canReview", data.canReview());
        if (!model.containsAttribute("reviewForm")) {
            model.addAttribute("reviewForm", new DepartmentReviewForm());
        }
        return "department/topics/review";
    }

    @PostMapping("/{id:\\d+}/review")
    public String review(@PathVariable Long id,
                         @Valid @ModelAttribute("reviewForm") DepartmentReviewForm form,
                         BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            redirect.addFlashAttribute("error", "Vui lòng chọn kết quả hợp lệ và nhập nhận xét tối đa 4000 ký tự");
            redirect.addFlashAttribute("reviewForm", form);
            return "redirect:/department/topics/" + id;
        }
        try {
            service.review(id, form);
            redirect.addFlashAttribute("success", "Đã lưu kết quả sơ duyệt bộ môn");
            return "redirect:/department/topics";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("reviewForm", form);
            return "redirect:/department/topics/" + id;
        }
    }

    @ExceptionHandler(BusinessException.class)
    public String businessError(BusinessException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/department/topics";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidParameter(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "ID đề tài hoặc số trang không hợp lệ");
        return "redirect:/department/topics";
    }

    @ExceptionHandler(DataAccessException.class)
    public String dataError(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Không thể xử lý dữ liệu, vui lòng tải lại và thử lại");
        return "redirect:/department/topics";
    }
}