// toan-D3
// toan-D3 - thêm chức năng tổng hợp kết quả
package com.example.student_topic_management.assessment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.student_topic_management.assessment.dto.ResultForm;
import com.example.student_topic_management.assessment.service.ResultService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/results")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','DEAN')")
    public String listResults(Model model) {
        model.addAttribute("results", resultService.findVisibleResults());
        return "results/list";
    }

    @GetMapping("/results/new")
    @PreAuthorize("hasRole('LECTURER') and !hasRole('STUDENT')")
    public String showCreateForm(Model model) {
        model.addAttribute("resultForm", new ResultForm());
        model.addAttribute("candidates", resultService.findConfirmationCandidates());
        return "results/form";
    }

    @PostMapping("/results/consolidate")
    @PreAuthorize("hasRole('LECTURER') and !hasRole('STUDENT')")
    public String consolidateResult(
            @Valid @ModelAttribute("resultForm") ResultForm form,
            BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("candidates", resultService.findConfirmationCandidates());
            return "results/form";
        }

        try {
            var saved = resultService.consolidateResult(form);
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận và tính điểm trung bình thành công");
            return "redirect:/results/" + saved.getId();
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("candidates", resultService.findConfirmationCandidates());
            return "results/form";
        }
    }

    @GetMapping("/results/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','LECTURER','DEAN')")
    public String detailResult(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "result",
                resultService.findVisibleById(id));

        return "results/detail";
    }

    @PostMapping("/dean/results/{id}/publish")
    @PreAuthorize("hasRole('DEAN') and !hasRole('STUDENT')")
    public String publishResult(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            boolean published = resultService.publishResult(id);
            redirectAttributes.addFlashAttribute("success",
                    published ? "Đã công bố kết quả và tạo thông báo cho sinh viên"
                            : "Kết quả này đã được công bố trước đó");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/results/" + id;
    }
}
