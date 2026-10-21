
package com.example.student_topic_management.registration.controller;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.registration.repository.DeanRegistrationRepository;
import com.example.student_topic_management.registration.repository.DeanRegistrationRepository.RegistrationRow;
import com.example.student_topic_management.registration.service.DeanRegistrationService;
import com.example.student_topic_management.repository.RegistrationRoundRepository;

@Controller
@RequestMapping("/dean/registrations")
@PreAuthorize("hasRole('DEAN')")
public class DeanRegistrationController {

    private static final int PAGE_SIZE = 10;

    private static final Set<String> VALID_STATUSES =
            Set.of(
                    "PENDING",
                    "APPROVED",
                    "REJECTED",
                    "CANCELLED"
            );

    private final DeanRegistrationRepository registrationRepository;
    private final RegistrationRoundRepository roundRepository;
    private final DeanRegistrationService deanRegistrationService;

    public DeanRegistrationController(
            DeanRegistrationRepository registrationRepository,
            RegistrationRoundRepository roundRepository,
            DeanRegistrationService deanRegistrationService) {

        this.registrationRepository = registrationRepository;
        this.roundRepository = roundRepository;
        this.deanRegistrationService = deanRegistrationService;
    }

    // =====================================================
    // C04.1 - DANH SACH HO SO DANG KY DANH CHO DEAN
    // =====================================================

    @GetMapping
    public String listRegistrations(
            @RequestParam(
                    name = "roundId",
                    required = false
            ) Long roundId,

            @RequestParam(
                    name = "status",
                    required = false
            ) String status,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            ) int page,

            Model model) {

        // 1. Chuan hoa so trang
        int requestedPage = Math.max(0, page);

        // 2. Chuan hoa trang thai
        String selectedStatus = null;

        if (status != null && !status.isBlank()) {

            String normalized = status
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (!VALID_STATUSES.contains(normalized)) {
                throw new IllegalArgumentException(
                        "Trạng thái đăng ký không hợp lệ"
                );
            }

            selectedStatus = normalized;
        }

        // 3. Kiem tra ma dot dang ky
        Long selectedRoundId = null;

        if (roundId != null) {

            if (roundId <= 0) {
                throw new IllegalArgumentException(
                        "Mã đợt đăng ký không hợp lệ"
                );
            }

            selectedRoundId = roundId;
        }

        // 4. Lay danh sach cac dot dang ky
        List<RegistrationRound> rounds =
                roundRepository
                        .findAllByOrderByCreatedAtDescIdDesc(
                                Pageable.unpaged()
                        )
                        .getContent();

        // 5. Dem tong so ho so
        long totalItems =
                registrationRepository.countRegistrations(
                        selectedRoundId,
                        selectedStatus
                );

        // 6. Tinh tong so trang
        long totalPages =
                (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;

        // 7. Xac dinh trang hien tai
        int currentPage = requestedPage;

        if (totalPages == 0) {

            currentPage = 0;

        } else if (currentPage >= totalPages) {

            currentPage = (int) totalPages - 1;
        }

        // 8. Lay danh sach ho so theo trang
        List<RegistrationRow> registrations =
                registrationRepository.findRegistrations(
                        selectedRoundId,
                        selectedStatus,
                        currentPage,
                        PAGE_SIZE
                );

        // 9. Dua du lieu sang Thymeleaf
        model.addAttribute(
                "registrations",
                registrations
        );

        model.addAttribute(
                "rounds",
                rounds
        );

        model.addAttribute(
                "selectedRoundId",
                selectedRoundId
        );

        model.addAttribute(
                "selectedStatus",
                selectedStatus
        );

        model.addAttribute(
                "currentPage",
                currentPage
        );

        model.addAttribute(
                "totalPages",
                totalPages
        );

        model.addAttribute(
                "totalItems",
                totalItems
        );

        model.addAttribute(
                "hasPrevious",
                currentPage > 0
        );

        model.addAttribute(
                "hasNext",
                currentPage + 1 < totalPages
        );

        return "dean/registrations/list";
    }

    // =====================================================
    // C04.2 - DEAN DUYET HO SO DANG KY DE TAI
    // =====================================================

    @PostMapping("/{registrationId}/approve")
    public String approveRegistration(
            @PathVariable("registrationId") Long registrationId,
            RedirectAttributes redirectAttributes) {

        try {

            // Goi Service duyet ho so
            deanRegistrationService.approveRegistration(
                    registrationId
            );

            // Thong bao thanh cong
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Duyệt đăng ký đề tài thành công"
            );

        } catch (IllegalArgumentException | IllegalStateException ex) {

            // Thong bao loi nghiep vu
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/dean/registrations";
    }

    // =====================================================
    // C04.3 - DEAN TU CHOI HO SO DANG KY DE TAI
    // =====================================================

    @PostMapping("/{registrationId}/reject")
    public String rejectRegistration(
            @PathVariable("registrationId") Long registrationId,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes) {

        try {

            // Goi Service kiem tra va tu choi ho so
            deanRegistrationService.rejectRegistration(
                    registrationId,
                    reason
            );

            // Thong bao thanh cong
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Từ chối đăng ký đề tài thành công"
            );

        } catch (IllegalArgumentException | IllegalStateException ex) {

            // Thong bao loi nghiep vu
            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/dean/registrations";
    }
}
