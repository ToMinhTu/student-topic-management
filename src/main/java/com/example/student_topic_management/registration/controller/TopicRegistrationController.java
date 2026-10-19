
package com.example.student_topic_management.registration.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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

import com.example.student_topic_management.registration.dto.TopicView;
import com.example.student_topic_management.registration.repository.TopicRepository;
import com.example.student_topic_management.registration.service.StudentAccess;
import com.example.student_topic_management.registration.service.TopicRegistrationService;

@Controller
@RequestMapping("/topics")
public class TopicRegistrationController {

    private final TopicRepository topicRepository;
    private final TopicRegistrationService registrationService;
    private final StudentAccess studentAccess;
    private final JdbcTemplate jdbc;
    private final Clock clock;

    public TopicRegistrationController(
            TopicRepository topicRepository,
            TopicRegistrationService registrationService,
            StudentAccess studentAccess,
            JdbcTemplate jdbc,
            Clock clock) {

        this.topicRepository = topicRepository;
        this.registrationService = registrationService;
        this.studentAccess = studentAccess;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    // C03.1 - Danh sach de tai da cong bo
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping
    public String listTopics(
            @RequestParam(name = "roundId", required = false)
            Long roundId,
            Model model) {

        List<TopicView> topics = List.of();
        Long leaderGroupId = null;
        boolean canRegister = false;

        if (roundId != null && roundId > 0) {

            topics = topicRepository.findPublishedTopics(roundId);

            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            boolean isStudent = auth != null
                    && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_STUDENT"
                            .equals(a.getAuthority()));

            if (isStudent) {

                Long studentId =
                        studentAccess.requireCurrentStudentId();

                // Tim nhom ma sinh vien la truong nhom
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

                    // Kiem tra thoi han dang ky
                    LocalDateTime now = LocalDateTime.ofInstant(
                            clock.instant(),
                            ZoneOffset.UTC
                    );

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

                    // Kiem tra so thanh vien
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

                    // Nhom chua tung dang ky de tai
                    Integer registrations = jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM topic_registrations
                            WHERE group_id = ?
                            """,
                            Integer.class,
                            leaderGroupId
                    );

                    canRegister = validRound != null
                            && validRound > 0
                            && validGroup != null
                            && validGroup > 0
                            && registrations != null
                            && registrations == 0;
                }
            }
        }

        model.addAttribute("topics", topics);
        model.addAttribute("roundId", roundId);
        model.addAttribute("leaderGroupId", leaderGroupId);
        model.addAttribute("canRegister", canRegister);

        return "topics/list";
    }

    // C03.2 - Truong nhom dang ky de tai
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{topicId:\\d+}/register")
    public String registerTopic(
            @PathVariable("topicId") Long topicId,
            @RequestParam("groupId") Long groupId,
            @RequestParam("roundId") Long roundId,
            RedirectAttributes redirect) {

        try {
            registrationService.registerTopic(groupId, topicId);

            redirect.addFlashAttribute(
                    "successMessage",
                    "Dang ky de tai thanh cong! Dang cho duyet."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException ex) {

            redirect.addFlashAttribute(
                    "errorMessage",
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the dang ky do vi pham rang buoc du lieu."
                            : ex.getMessage()
            );
        }

        return "redirect:/topics?roundId=" + roundId;
    }
}
