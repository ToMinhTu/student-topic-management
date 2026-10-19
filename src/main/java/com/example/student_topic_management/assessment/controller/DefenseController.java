// toan-D2
// toan-D2 - thêm chức năng quản lý lịch bảo vệ
package com.example.student_topic_management.assessment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.student_topic_management.assessment.dto.DefenseForm;
import com.example.student_topic_management.assessment.repository.CouncilRepository;
import com.example.student_topic_management.assessment.service.DefenseService;
import com.example.student_topic_management.repository.RegistrationRoundRepository;

import jakarta.validation.Valid;

@Controller
public class DefenseController {

    private final DefenseService defenseService;
    private final CouncilRepository councilRepository;
    private final RegistrationRoundRepository roundRepository;

    public DefenseController(DefenseService defenseService,
            CouncilRepository councilRepository,
            RegistrationRoundRepository roundRepository) {
        this.defenseService = defenseService;
        this.councilRepository = councilRepository;
        this.roundRepository = roundRepository;
    }

    @GetMapping("/defenses")
    public String listDefenses(Model model) {
        var defenses = defenseService.findAll();
        Map<Long, String> councilNames = new HashMap<>();
        Map<Long, String> roundNames = new HashMap<>();
        defenses.forEach(defense -> {
            councilRepository.findById(defense.getCouncilId())
                    .ifPresent(council -> councilNames.put(defense.getCouncilId(), council.getName()));
            roundRepository.findById(defense.getRoundId())
                    .ifPresent(round -> roundNames.put(defense.getRoundId(), round.getName()));
        });
        model.addAttribute("defenses", defenses);
        model.addAttribute("councilNames", councilNames);
        model.addAttribute("roundNames", roundNames);
        return "defenses/list";
    }

    @GetMapping("/defenses/new")
    public String showCreateForm(Model model) {
        model.addAttribute(
                "defenseForm",
                new DefenseForm());

        return "defenses/form";
    }

    @GetMapping("/defenses/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("defenseForm", defenseService.getForm(id));
        return "defenses/form";
    }

    @PostMapping("/defenses/save")
    public String saveDefense(
            @Valid @ModelAttribute("defenseForm") DefenseForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "defenses/form";
        }

        try {
            defenseService.saveDefense(form);
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "defenses/form";
        }

        return "redirect:/defenses";
    }

    @PostMapping("/defenses/update")
    public String updateDefense(@Valid @ModelAttribute("defenseForm") DefenseForm form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "defenses/form";
        }
        try {
            defenseService.updateDefense(form);
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "defenses/form";
        }
        return "redirect:/defenses";
    }

    @PostMapping("/defenses/{id}/delete")
    public String deleteDefense(@PathVariable Long id) {
        defenseService.deleteDefense(id);
        return "redirect:/defenses";
    }

    @GetMapping("/defenses/{id}")
    public String detailDefense(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "defense",
                defenseService.findById(id));
        var defense = defenseService.findById(id);
        model.addAttribute("roundName", roundRepository.findById(defense.getRoundId())
                .map(round -> round.getName()).orElse("Không xác định"));
        model.addAttribute("councilName", councilRepository.findById(defense.getCouncilId())
                .map(council -> council.getName()).orElse("Không xác định"));

        return "defenses/detail";
    }
}
