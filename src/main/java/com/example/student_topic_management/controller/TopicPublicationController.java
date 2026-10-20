package com.example.student_topic_management.controller;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.service.TopicPublicationService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dean/topics/publish")
@PreAuthorize("hasRole('DEAN')")
public class TopicPublicationController {
    private final TopicPublicationService service;

    public TopicPublicationController(TopicPublicationService service) { this.service = service; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("topics", service.getApprovedTopics(page));
        return "dean/topics/publish-list";
    }

    @PostMapping("/{id:\\d+}")
    public String publish(@PathVariable Long id, RedirectAttributes redirect) {
        service.publish(id);
        redirect.addFlashAttribute("success", "Đã công bố đề tài. Có thể tra cứu trong danh sách đã công bố.");
        return "redirect:/dean/topics/publish";
    }

    @ExceptionHandler(BusinessException.class)
    public String businessError(BusinessException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dean/topics/publish";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidParameter(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "ID đề tài hoặc số trang không hợp lệ");
        return "redirect:/dean/topics/publish";
    }

    @ExceptionHandler(DataAccessException.class)
    public String dataError(Model model) {
        model.addAttribute("topics", Page.empty());
        model.addAttribute("error", "Không thể công bố hoặc tải dữ liệu, vui lòng thử lại");
        return "dean/topics/publish-list";
    }
}