// toan-D3
// toan-D3 - thêm chức năng nhập điểm
package com.example.student_topic_management.assessment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.student_topic_management.assessment.dto.EvaluationForm;
import com.example.student_topic_management.assessment.service.EvaluationService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/evaluations")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String listEvaluations(Model model) {
        model.addAttribute(
                "evaluations",
                evaluationService.findAll());
        model.addAttribute("assignments", evaluationService.findAssignmentsForCurrentUser());

        return "evaluations/list";
    }

    @GetMapping("/evaluations/new")
    @PreAuthorize("hasRole('LECTURER')")
    public String showCreateForm(@RequestParam(required = false) Long assignmentId, Model model) {
        EvaluationForm form = new EvaluationForm();
        form.setAssignmentId(assignmentId);
        model.addAttribute("evaluationForm", form);

        return "evaluations/form";
    }

    @PostMapping("/evaluations/save")
    @PreAuthorize("hasRole('LECTURER')")
    public String saveEvaluation(
            @Valid @ModelAttribute("evaluationForm") EvaluationForm form,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "evaluations/form";
        }

        try {
            evaluationService.saveEvaluation(form);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "evaluations/form";
        }

        return "redirect:/evaluations";
    }

    @GetMapping("/evaluations/{id}")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String detailEvaluation(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "evaluation",
                evaluationService.findById(id));

        return "evaluations/detail";
    }
}
