// toan-D3
package com.example.student_topic_management.assessment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.student_topic_management.assessment.dto.ResultForm;
import com.example.student_topic_management.assessment.service.ResultService;

import jakarta.validation.Valid;

@Controller
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/results")
    public String listResults(Model model) {
        model.addAttribute(
                "results",
                resultService.findAll());

        return "results/list";
    }

    @GetMapping("/results/new")
    public String showCreateForm(Model model) {
        model.addAttribute(
                "resultForm",
                new ResultForm());

        return "results/form";
    }

    @PostMapping("/results/consolidate")
    public String consolidateResult(
            @Valid @ModelAttribute("resultForm") ResultForm form,
            BindingResult result) {

        if (result.hasErrors()) {
            return "results/form";
        }

        resultService.consolidateResult(form);

        return "redirect:/results";
    }

    @GetMapping("/results/{id}")
    public String detailResult(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "result",
                resultService.findById(id));

        return "results/detail";
    }
}