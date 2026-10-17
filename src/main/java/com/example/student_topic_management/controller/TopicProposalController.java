package com.example.student_topic_management.controller;

import com.example.student_topic_management.dto.TopicProposalForm;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.service.TopicProposalService;
import com.example.student_topic_management.common.exception.BusinessException;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/topics")
public class TopicProposalController {

    private final TopicProposalService service;

    public TopicProposalController(TopicProposalService service) {
        this.service = service;
    }

    @GetMapping("/my")
    public String myTopics(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int safePage = Math.max(page, 0);

        model.addAttribute(
                "topics",
                service.getMyTopics(
                        PageRequest.of(safePage, 10)
                )
        );

        return "topics/my-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("topicForm", new TopicProposalForm());
        model.addAttribute("editMode", false);
        return "topics/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("topicForm")
            TopicProposalForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", false);
            return "topics/form";
        }

        try {
            Topic topic = service.createDraft(form);

            redirectAttributes.addFlashAttribute(
                    "success", "Tạo đề xuất nháp thành công");

            return "redirect:/topics/" + topic.getId();

        } catch (BusinessException ex) {
            bindingResult.reject(
                    "businessError", ex.getMessage());
            model.addAttribute("editMode", false);
            return "topics/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "topic", service.getMyTopicDetail(id));

        return "topics/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        Topic topic = service.getMyTopicDetail(id);

        if (topic.getStatus()
                != com.example.student_topic_management.enums.TopicStatus.DRAFT) {
            throw new BusinessException(
                    "Chỉ được sửa đề xuất ở trạng thái nháp");
        }

        TopicProposalForm form = new TopicProposalForm();

        form.setRoundId(topic.getRoundId());
        form.setDepartmentId(topic.getDepartmentId());
        form.setCode(topic.getCode());
        form.setTitle(topic.getTitle());
        form.setDescription(topic.getDescription());
        form.setRequirements(topic.getRequirements());

        model.addAttribute("topicForm", form);
        model.addAttribute("editMode", true);
        model.addAttribute("topicId", id);

        return "topics/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("topicForm")
            TopicProposalForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("topicId", id);
            return "topics/form";
        }

        try {
            service.updateDraft(id, form);

            redirectAttributes.addFlashAttribute(
                    "success", "Cập nhật đề xuất thành công");

            return "redirect:/topics/" + id;

        } catch (BusinessException ex) {
            bindingResult.reject(
                    "businessError", ex.getMessage());
            model.addAttribute("editMode", true);
            model.addAttribute("topicId", id);
            return "topics/form";
        }
    }

    @PostMapping("/{id}/submit")
    public String submit(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        service.submitProposal(id);

        redirectAttributes.addFlashAttribute(
                "success", "Đã gửi đề xuất để xét duyệt");

        return "redirect:/topics/" + id;
    }
}
