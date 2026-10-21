// toan-D2
// toan-D2 - thêm chức năng quản lý lịch bảo vệ
package com.example.student_topic_management.assessment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.assessment.dto.DefenseForm;
import com.example.student_topic_management.assessment.service.DefenseService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class DefenseController {

    private final DefenseService defenseService;

    public DefenseController(DefenseService defenseService) {
        this.defenseService = defenseService;
    }

    @GetMapping("/defenses")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String listDefenses(Model model) {
        model.addAttribute("defenses", defenseService.findAll());
        model.addAttribute("completableDefenseIds", defenseService.findCompletableDefenseIds());
        return "defenses/list";
    }

    @GetMapping("/defenses/new")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String showCreateForm(Model model) {
        model.addAttribute(
                "defenseForm",
                new DefenseForm());

        return "defenses/form";
    }

    @PostMapping("/defenses/save")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String saveDefense(
            @Valid @ModelAttribute("defenseForm") DefenseForm form,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "defenses/form";
        }

        try {
            defenseService.saveDefense(form);
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "defenses/form";
        }

        return "redirect:/defenses";
    }

    @GetMapping("/defenses/{id}")
    @PreAuthorize("hasAnyRole('LECTURER','DEAN')")
    public String detailDefense(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "defense",
                defenseService.findById(id));
        model.addAttribute("canComplete", defenseService.canCurrentUserComplete(id));

        return "defenses/detail";
    }

    @PostMapping("/defenses/{id}/complete")
    @PreAuthorize("hasRole('LECTURER')")
    public String completeDefense(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            defenseService.completeDefense(id);
            redirectAttributes.addFlashAttribute("success", "Đã kết thúc buổi bảo vệ");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/defenses/" + id;
    }
}
