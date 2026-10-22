package com.example.student_topic_management.assessment.controller;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.example.student_topic_management.assessment.dto.ReviewerAssignmentForm;
import com.example.student_topic_management.assessment.service.ReviewerAssignmentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/dean/assignments")
@PreAuthorize("hasRole('DEAN')")
public class ReviewerAssignmentController {

    private final ReviewerAssignmentService assignmentService;

    public ReviewerAssignmentController(
            ReviewerAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("assignments", assignmentService.findAll());
        model.addAttribute(
                "registrations",
                assignmentService.findEligibleRegistrations()
        );
        return "assignments/list";
    }

    @GetMapping("/new")
    public String createForm(
            @RequestParam(required = false) Long registrationId,
            Model model) {

        ReviewerAssignmentForm form = new ReviewerAssignmentForm();
        form.setRegistrationId(registrationId);
        model.addAttribute("assignmentForm", form);
        loadFormOptions(model);
        return "assignments/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("assignmentForm") ReviewerAssignmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            loadFormOptions(model);
            return "assignments/form";
        }

        try {
            assignmentService.create(form);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Phân công chấm điểm thành công"
            );
            return "redirect:/dean/assignments";
        } catch (IllegalArgumentException
                | IllegalStateException
                | DataIntegrityViolationException ex) {
            model.addAttribute("error", ex.getMessage());
            loadFormOptions(model);
            return "assignments/form";
        }
    }

    @PostMapping("/sync-council")
    public String syncCouncil(
            @RequestParam(required = false) Long registrationId,
            RedirectAttributes redirectAttributes) {

        try {
            int created = assignmentService.assignCouncilMembers(registrationId);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã tạo " + created + " phân công cho thành viên hội đồng"
            );
        } catch (IllegalArgumentException
                | IllegalStateException
                | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/dean/assignments";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            assignmentService.delete(id);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã xóa phân công chấm điểm"
            );
        } catch (IllegalArgumentException
                | IllegalStateException
                | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/dean/assignments";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidParameter(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "error",
                "Mã hồ sơ hoặc mã phân công không hợp lệ"
        );
        return "redirect:/dean/assignments";
    }

    private void loadFormOptions(Model model) {
        model.addAttribute(
                "registrations",
                assignmentService.findEligibleRegistrations()
        );
        model.addAttribute(
                "lecturers",
                assignmentService.findActiveLecturers()
        );
    }
}
