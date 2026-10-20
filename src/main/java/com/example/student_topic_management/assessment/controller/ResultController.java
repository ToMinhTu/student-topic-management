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

@Controller
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/results")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String listResults(Model model) {
        model.addAttribute(
                "results",
                resultService.findAll());

        return "results/list";
    }

    @GetMapping("/results/new")
    @PreAuthorize("hasRole('DEAN')")
    public String showCreateForm(Model model) {
        model.addAttribute(
                "resultForm",
                new ResultForm());

        return "results/form";
    }

    @PostMapping("/results/consolidate")
    @PreAuthorize("hasRole('DEAN')")
    public String consolidateResult(
            @Valid @ModelAttribute("resultForm") ResultForm form,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "results/form";
        }

        try {
            resultService.consolidateResult(form);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "results/form";
        }

        return "redirect:/results";
    }

    @GetMapping("/results/{id}")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String detailResult(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "result",
                resultService.findById(id));

        return "results/detail";
    }
}
