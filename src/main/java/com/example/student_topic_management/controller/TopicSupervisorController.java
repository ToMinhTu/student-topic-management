package com.example.student_topic_management.controller;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.TopicSupervisorForm;
import com.example.student_topic_management.service.SupervisorService;
import com.example.student_topic_management.service.SupervisorViewService;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dean/topics")
@PreAuthorize("hasRole('DEAN')")
public class TopicSupervisorController {
    private static final String CHOOSE_REDIRECT = "redirect:/dean/topics/supervisors";
    private static final String DATA_ERROR = "Không thể xử lý dữ liệu phân công lúc này. Vui lòng tải lại và thử lại.";
    private final SupervisorService supervisorService;
    private final SupervisorViewService viewService;

    public TopicSupervisorController(SupervisorService supervisorService, SupervisorViewService viewService) {
        this.supervisorService = supervisorService;
        this.viewService = viewService;
    }

    @GetMapping("/supervisors")
    public String chooseTopic(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("topics", viewService.getTopics(page));
        return "supervisors/topics";
    }

    @GetMapping("/{topicId}/supervisors")
    public String list(@PathVariable Long topicId, Model model) {
        var data = viewService.getPage(topicId);
        model.addAttribute("topic", data.topic());
        model.addAttribute("slots", data.slots());
        model.addAttribute("candidates", data.candidates());
        model.addAttribute("availableSlots", data.availableSlots());
        model.addAttribute("canEdit", data.canEdit());
        model.addAttribute("approvedRegistration", data.approvedRegistration());
        if (!model.containsAttribute("supervisorForm")) {
            model.addAttribute("supervisorForm", new TopicSupervisorForm());
        }
        return "supervisors/list";
    }

    @PostMapping("/{topicId}/supervisors")
    public String add(@PathVariable Long topicId,
                      @Valid @ModelAttribute("supervisorForm") TopicSupervisorForm form,
                      BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            redirect.addFlashAttribute("supervisorForm", form);
            redirect.addFlashAttribute("formErrors", result.getFieldErrors().stream().map(error ->
                    error.isBindingFailure() ? ("lecturerId".equals(error.getField())
                            ? "Vui lòng chọn giảng viên hợp lệ" : "Vị trí phải là 1 hoặc 2")
                            : error.getDefaultMessage()).distinct().toList());
            return detailRedirect(topicId);
        }
        try {
            supervisorService.addSupervisor(topicId, form.getLecturerId(), form.getSlotNo());
            redirect.addFlashAttribute("success", "Phân công giảng viên hướng dẫn thành công");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            redirect.addFlashAttribute("supervisorForm", form);
        } catch (DataAccessException ex) {
            redirect.addFlashAttribute("error", DATA_ERROR);
            redirect.addFlashAttribute("supervisorForm", form);
        }
        return detailRedirect(topicId);
    }

    @PostMapping("/{topicId}/supervisors/{lecturerId}/delete")
    public String remove(@PathVariable Long topicId, @PathVariable Long lecturerId, RedirectAttributes redirect) {
        try {
            supervisorService.removeSupervisor(topicId, lecturerId);
            redirect.addFlashAttribute("success", "Đã xóa phân công giảng viên hướng dẫn");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataAccessException ex) {
            redirect.addFlashAttribute("error", DATA_ERROR);
        }
        return detailRedirect(topicId);
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessError(BusinessException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return CHOOSE_REDIRECT;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleInvalidParameter(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Mã đề tài, mã giảng viên hoặc số trang không hợp lệ");
        return CHOOSE_REDIRECT;
    }

    @ExceptionHandler(DataAccessException.class)
    public String handleDataError(Model model) {
        model.addAttribute("topics", Page.empty());
        model.addAttribute("error", DATA_ERROR);
        return "supervisors/topics";
    }

    private String detailRedirect(Long topicId) {
        return "redirect:/dean/topics/" + topicId + "/supervisors";
    }
}
