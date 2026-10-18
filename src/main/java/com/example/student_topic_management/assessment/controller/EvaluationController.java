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

@Controller
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/evaluations")
    public String listEvaluations(Model model) {
        model.addAttribute(
                "evaluations",
                evaluationService.findAll());

        return "evaluations/list";
    }

    @GetMapping("/evaluations/new")
    public String showCreateForm(Model model) {
        model.addAttribute(
                "evaluationForm",
                new EvaluationForm());

        return "evaluations/form";
    }

    @PostMapping("/evaluations/save")
    public String saveEvaluation(
            @Valid @ModelAttribute("evaluationForm") EvaluationForm form,
            BindingResult result) {

        if (result.hasErrors()) {
            return "evaluations/form";
        }

        evaluationService.saveEvaluation(form);

        return "redirect:/evaluations";
    }

    @GetMapping("/evaluations/{id}")
    public String detailEvaluation(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "evaluation",
                evaluationService.findById(id));

        return "evaluations/detail";
    }
}