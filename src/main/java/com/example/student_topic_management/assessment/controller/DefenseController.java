// toan-D2
package com.example.student_topic_management.assessment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.student_topic_management.assessment.dto.DefenseForm;
import com.example.student_topic_management.assessment.service.DefenseService;

import jakarta.validation.Valid;

@Controller
public class DefenseController {

    private final DefenseService defenseService;

    public DefenseController(DefenseService defenseService) {
        this.defenseService = defenseService;
    }

    @GetMapping("/defenses")
    public String listDefenses(Model model) {
        model.addAttribute("defenses", defenseService.findAll());
        return "defenses/list";
    }

    @GetMapping("/defenses/new")
    public String showCreateForm(Model model) {
        model.addAttribute(
                "defenseForm",
                new DefenseForm());

        return "defenses/form";
    }

    @PostMapping("/defenses/save")
    public String saveDefense(
            @Valid @ModelAttribute("defenseForm") DefenseForm form,
            BindingResult result) {

        if (result.hasErrors()) {
            return "defenses/form";
        }

        defenseService.saveDefense(form);

        return "redirect:/defenses";
    }

    @GetMapping("/defenses/{id}")
    public String detailDefense(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "defense",
                defenseService.findById(id));

        return "defenses/detail";
    }
}