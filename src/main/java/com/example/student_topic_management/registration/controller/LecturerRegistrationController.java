package com.example.student_topic_management.registration.controller;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.registration.repository.DeanRegistrationRepository;
import com.example.student_topic_management.registration.repository.DeanRegistrationRepository.RegistrationRow;
import com.example.student_topic_management.registration.service.DeanRegistrationService;
import com.example.student_topic_management.repository.RegistrationRoundRepository;

@Controller
@RequestMapping("/lecturer/registrations")
@PreAuthorize("hasRole('LECTURER')")
public class LecturerRegistrationController {
    private final DeanRegistrationRepository registrations;
    private final RegistrationRoundRepository rounds;
    private final DeanRegistrationService service;
    public LecturerRegistrationController(DeanRegistrationRepository registrations, RegistrationRoundRepository rounds, DeanRegistrationService service) {
        this.registrations = registrations; this.rounds = rounds; this.service = service;
    }
    @GetMapping
    public String list(@RequestParam(required=false) Long roundId, @RequestParam(required=false) String status,
                       @RequestParam(defaultValue="0") int page, Model model) {
        String selectedStatus = status == null || status.isBlank() ? null : status.toUpperCase();
        int currentPage = Math.max(0, page);
        long total = registrations.countRegistrations(roundId, selectedStatus);
        long pages = (total + 9) / 10;
        if (pages > 0 && currentPage >= pages) currentPage = (int) pages - 1;
        List<RegistrationRow> rows = registrations.findRegistrations(roundId, selectedStatus, currentPage, 10);
        model.addAttribute("registrations", rows);
        model.addAttribute("rounds", rounds.findAllByOrderByCreatedAtDescIdDesc(Pageable.unpaged()).getContent());
        model.addAttribute("selectedRoundId", roundId); model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("currentPage", currentPage); model.addAttribute("totalPages", pages); model.addAttribute("totalItems", total);
        model.addAttribute("hasPrevious", currentPage > 0); model.addAttribute("hasNext", currentPage + 1 < pages);
        return "lecturer/registrations/list";
    }
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirect) { try { service.approveRegistration(id); redirect.addFlashAttribute("success", "Đã duyệt hồ sơ nhóm"); } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); } return "redirect:/lecturer/registrations"; }
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason, RedirectAttributes redirect) { try { service.rejectRegistration(id, reason); redirect.addFlashAttribute("success", "Đã từ chối hồ sơ nhóm"); } catch (RuntimeException e) { redirect.addFlashAttribute("error", e.getMessage()); } return "redirect:/lecturer/registrations"; }
}
