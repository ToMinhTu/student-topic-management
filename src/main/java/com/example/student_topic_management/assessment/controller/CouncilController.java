// toan-D1
// toan-D1 - thêm chức năng quản lý hội đồng
package com.example.student_topic_management.assessment.controller;

import com.example.student_topic_management.assessment.dto.CouncilMemberForm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.student_topic_management.assessment.dto.CouncilForm;
import com.example.student_topic_management.assessment.service.CouncilService;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("hasAnyRole('LECTURER','DEAN')")
public class CouncilController {

    private final CouncilService councilService;

    public CouncilController(CouncilService councilService) {
        this.councilService = councilService;
    }

    @GetMapping("/councils")
    public String listCouncils(Model model) {
        model.addAttribute("councils", councilService.findAll());
        return "councils/list";
    }

    @GetMapping("/councils/new")
    public String showCreateForm(Model model) {
        CouncilForm form = new CouncilForm();
        form.setRoundId(1L);

        model.addAttribute("councilForm", form);
        return "councils/form";
    }

    @PostMapping("/councils/save")
    public String saveCouncil(
            @Valid @ModelAttribute("councilForm") CouncilForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "councils/form";
        }

        councilService.saveCouncil(form);
        return "redirect:/councils";
    }

    @GetMapping("/councils/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        var council = councilService.findById(id);

        CouncilForm form = new CouncilForm();
        form.setId(council.getId());
        form.setRoundId(council.getRoundId());
        form.setName(council.getName());

        model.addAttribute("councilForm", form);
        return "councils/form";
    }

    @GetMapping("/councils/{id}/members")
    public String listMembers(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("council", councilService.findById(id));
        model.addAttribute(
                "members",
                councilService.findMembers(id));
        model.addAttribute("canActivate", councilService.canActivate(id));

        return "councils/members";
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('DEAN','DEPARTMENT_HEAD')")
    @PostMapping("/councils/{id}/activate")
    public String activateCouncil(@PathVariable Long id) {
        councilService.activate(id);
        return "redirect:/councils/{id}/members";
    }

    @GetMapping("/councils/{id}/members/new")
    public String showMemberForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("council", councilService.findById(id));
        model.addAttribute(
                "memberForm",
                new CouncilMemberForm());

        return "councils/member-form";
    }

    @PostMapping("/councils/{id}/members/save")
    public String saveMember(
            @PathVariable Long id,
            @Valid @ModelAttribute("memberForm") CouncilMemberForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("council", councilService.findById(id));
            return "councils/member-form";
        }

        councilService.addMember(id, form);

        return "redirect:/councils/{id}/members";
    }

}
