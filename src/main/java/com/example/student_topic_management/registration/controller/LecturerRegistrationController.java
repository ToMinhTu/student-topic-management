package com.example.student_topic_management.registration.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.student_topic_management.registration.service.DeanRegistrationService;
import com.example.student_topic_management.registration.service.LecturerRegistrationService;

@Controller
@RequestMapping("/lecturer/registrations")
@PreAuthorize("hasRole('LECTURER') and !hasRole('STUDENT')")
public class LecturerRegistrationController {
    private final LecturerRegistrationService queries;
    private final DeanRegistrationService decisions;

    public LecturerRegistrationController(LecturerRegistrationService queries, DeanRegistrationService decisions) {
        this.queries = queries;
        this.decisions = decisions;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long roundId,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        String selectedStatus = LecturerRegistrationService.normalizeStatus(status);
        model.addAttribute("registrations", queries.findRegistrations(roundId, selectedStatus, page));
        model.addAttribute("rounds", queries.findRounds());
        model.addAttribute("selectedRoundId", roundId);
        model.addAttribute("selectedStatus", selectedStatus);
        return "lecturer/registrations/list";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            decisions.approveAsSupervisor(id);
            redirect.addFlashAttribute("success", "Đã duyệt hồ sơ và khóa nhóm sinh viên.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataAccessException ex) {
            redirect.addFlashAttribute("error", "Không thể lưu thay đổi. Vui lòng tải lại danh sách và thử lại.");
        }
        return "redirect:/lecturer/registrations";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirect) {
        try {
            decisions.rejectAsSupervisor(id, reason);
            redirect.addFlashAttribute("success", "Đã từ chối hồ sơ đăng ký.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (DataAccessException ex) {
            redirect.addFlashAttribute("error", "Không thể lưu thay đổi. Vui lòng tải lại danh sách và thử lại.");
        }
        return "redirect:/lecturer/registrations";
    }
}
