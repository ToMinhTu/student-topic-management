
package com.example.student_topic_management.registration.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.registration.dto.TopicView;
import com.example.student_topic_management.registration.repository.TopicRepository;
import com.example.student_topic_management.registration.service.StudentAccess;
import com.example.student_topic_management.registration.service.TopicRegistrationService;
import com.example.student_topic_management.repository.RegistrationRoundRepository;

@Controller
@RequestMapping("/topics")
public class TopicRegistrationController {

    private final TopicRepository topicRepository;
    private final TopicRegistrationService registrationService;
    private final StudentAccess studentAccess;
    private final RegistrationRoundRepository roundRepository;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public TopicRegistrationController(
            TopicRepository topicRepository,
            TopicRegistrationService registrationService,
            StudentAccess studentAccess,
            JdbcTemplate jdbc,
            Clock clock,
            RegistrationRoundRepository roundRepository) {

        this.topicRepository = topicRepository;
        this.registrationService = registrationService;
        this.studentAccess = studentAccess;
        this.jdbc = jdbc;
        this.clock = clock;
        this.roundRepository = roundRepository;
    }

    // =====================================================
    // C03.1 - XEM DANH SACH DE TAI THEO DOT DANG KY
    // =====================================================

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping
    public String listTopics(
            @RequestParam(
                    name = "roundId",
                    required = false
            ) Long roundId,
            Model model) {

        // Danh sach cac dot dang ky
        List<RegistrationRound> rounds = roundRepository
                .findAllByOrderByCreatedAtDescIdDesc(
                        Pageable.unpaged()
                )
                .getContent();

        model.addAttribute("rounds", rounds);

        // Gia tri mac dinh
        List<TopicView> topics = List.of();
        Long leaderGroupId = null;
        boolean canRegister = false;

        // Chi lay de tai khi da chon dot hop le
        if (roundId != null && roundId > 0) {

            // Lay de tai da cong bo
            topics = topicRepository.findPublishedTopics(roundId);

            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            boolean isStudent = auth != null
                    && auth.getAuthorities().stream()
                    .anyMatch(a ->
                            "ROLE_STUDENT".equals(
                                    a.getAuthority()
                            )
                    );

            if (isStudent) {

                // Xac dinh sinh vien dang dang nhap
                Long studentId =
                        studentAccess.requireCurrentStudentId();

                // Tim nhom sinh vien dang lam truong nhom
                List<Long> leaderGroups = jdbc.queryForList(
                        """
                        SELECT sg.id
                        FROM student_groups sg
                        JOIN group_members gm
                          ON gm.group_id = sg.id
                        WHERE sg.round_id = ?
                          AND sg.status = 'FORMING'
                          AND gm.student_id = ?
                          AND gm.slot_no = 1
                          AND gm.left_at IS NULL
                        """,
                        Long.class,
                        roundId,
                        studentId
                );

                if (!leaderGroups.isEmpty()) {

                    leaderGroupId = leaderGroups.get(0);

                    // Thoi gian hien tai theo UTC
                    LocalDateTime now =
                            LocalDateTime.ofInstant(
                                    clock.instant(),
                                    ZoneOffset.UTC
                            );

                    // Kiem tra dot dang ky dang mo
                    Integer validRound = jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM registration_rounds
                            WHERE id = ?
                              AND status = 'OPEN'
                              AND student_starts_at <= ?
                              AND student_ends_at > ?
                            """,
                            Integer.class,
                            roundId,
                            now,
                            now
                    );

                    // Kiem tra so luong thanh vien
                    Integer validGroup = jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM student_groups sg
                            JOIN registration_rounds r
                              ON r.id = sg.round_id
                            WHERE sg.id = ?
                              AND (
                                  SELECT COUNT(*)
                                  FROM group_members gm
                                  WHERE gm.group_id = sg.id
                                    AND gm.left_at IS NULL
                              ) BETWEEN r.group_min_size
                                    AND r.group_max_size
                            """,
                            Integer.class,
                            leaderGroupId
                    );

                    // Kiem tra nhom chua tung dang ky de tai
                    Integer registrations = jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM topic_registrations
                            WHERE group_id = ?
                            """,
                            Integer.class,
                            leaderGroupId
                    );

                    canRegister =
                            validRound != null
                            && validRound > 0
                            && validGroup != null
                            && validGroup > 0
                            && registrations != null
                            && registrations == 0;
                }
            }
        }

        // Truyen du lieu sang giao dien Thymeleaf
        model.addAttribute("topics", topics);
        model.addAttribute("roundId", roundId);
        model.addAttribute("leaderGroupId", leaderGroupId);
        model.addAttribute("canRegister", canRegister);

        return "topics/list";
    }

    // =====================================================
    // C03.2 - TRUONG NHOM DANG KY DE TAI
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{topicId:\\d+}/register")
    public String registerTopic(
            @PathVariable("topicId") Long topicId,
            @RequestParam("groupId") Long groupId,
            @RequestParam("roundId") Long roundId,
            RedirectAttributes redirect) {

        try {

            // Xu ly dang ky trong Service
            registrationService.registerTopic(
                    groupId,
                    topicId
            );

            redirect.addFlashAttribute(
                    "successMessage",
                    "Dang ky de tai thanh cong! Dang cho duyet."
            );

        } catch (
                IllegalArgumentException |
                IllegalStateException |
                DataIntegrityViolationException ex
        ) {

            redirect.addFlashAttribute(
                    "errorMessage",
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the dang ky do vi pham rang buoc du lieu."
                            : ex.getMessage()
            );
        }

        // Quay lai danh sach de tai cua dot da chon
        return "redirect:/topics?roundId=" + roundId;
    }
}
