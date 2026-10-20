package com.example.student_topic_management.controller;

import com.example.student_topic_management.dto.TopicProposalForm;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.service.TopicProposalService;
import com.example.student_topic_management.common.exception.BusinessException;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

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
                        PageRequest.of(safePage, 10, Sort.by(Sort.Direction.DESC, "id"))
                )
        );

        return "topics/my-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("topicForm", new TopicProposalForm());
        model.addAttribute("editMode", false);
        return prepareForm(model);
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
            return prepareForm(model);
        }

        try {
            Topic topic = service.createDraft(form);

            redirectAttributes.addFlashAttribute(
                    "success", "Tạo đề xuất nháp thành công");

            return "redirect:/topics/" + topic.getId();

        } catch (BusinessException | DataIntegrityViolationException ex) {
            bindingResult.reject(
                    "businessError", friendlyMessage(ex));
            model.addAttribute("editMode", false);
            return prepareForm(model);
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
                != com.example.student_topic_management.enums.TopicStatus.DRAFT
                && topic.getStatus() != com.example.student_topic_management.enums.TopicStatus.REJECTED) {
            throw new BusinessException(
                    "Chỉ được sửa đề xuất nháp hoặc bị từ chối");
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

        return prepareForm(model);
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
            return prepareForm(model);
        }

        try {
            service.updateDraft(id, form);

            redirectAttributes.addFlashAttribute(
                    "success", "Cập nhật đề xuất thành công");

            return "redirect:/topics/" + id;

        } catch (BusinessException | DataIntegrityViolationException ex) {
            bindingResult.reject(
                    "businessError", friendlyMessage(ex));
            model.addAttribute("editMode", true);
            model.addAttribute("topicId", id);
            return prepareForm(model);
        }
    }

    @PostMapping("/{id}/submit")
    public String submit(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            service.submitProposal(id);
        } catch (BusinessException | DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", friendlyMessage(ex));
            return "redirect:/topics/" + id;
        }

        redirectAttributes.addFlashAttribute(
                "success", "Đã gửi đề xuất để xét duyệt");

        return "redirect:/topics/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            service.deleteDraft(id);
            attributes.addFlashAttribute("success", "Đã xóa đề xuất nháp");
        } catch (BusinessException ex) {
            attributes.addFlashAttribute("error", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            attributes.addFlashAttribute("error", "Không thể xóa đề tài vì còn dữ liệu liên quan (GVHD hoặc đăng ký).");
        }
        return "redirect:/topics/my";
    }

    private String prepareForm(Model model) {
        TopicProposalForm form = (TopicProposalForm) model.getAttribute("topicForm");
        model.addAttribute("roundOptions", service.getProposalRounds(form == null ? null : form.getRoundId()));
        model.addAttribute("departmentOptions", service.getDepartments());
        return "topics/form";
    }

    private String friendlyMessage(Exception ex) {
        return ex instanceof BusinessException ? ex.getMessage()
                : "Không thể lưu đề xuất. Mã đề tài có thể đã được sử dụng hoặc dữ liệu liên quan đã thay đổi. Vui lòng kiểm tra và thử lại.";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessError(BusinessException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/topics/my";
    }
}
