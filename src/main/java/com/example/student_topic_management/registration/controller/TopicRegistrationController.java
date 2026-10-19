
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

    private record RegistrationInfo(
            Long id,
            String status,
            Long topicId
    ) {}

    // =====================================================
    // C03 - DANH SACH DE TAI
    // =====================================================

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping
    public String listTopics(
            @RequestParam(
                    name = "roundId",
                    required = false
            ) Long roundId,
            Model model) {

        List<RegistrationRound> rounds = roundRepository
                .findAllByOrderByCreatedAtDescIdDesc(
                        Pageable.unpaged()
                )
                .getContent();

        model.addAttribute("rounds", rounds);

        List<TopicView> topics = List.of();

        Long leaderGroupId = null;
        Long registrationId = null;
        Long registeredTopicId = null;

        String registrationStatus = null;

        boolean canRegister = false;
        boolean canReRegister = false;
        boolean canCancel = false;

        if (roundId != null && roundId > 0) {

            topics = topicRepository.findPublishedTopics(roundId);

            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            boolean isStudent = auth != null
                    && auth.isAuthenticated()
                    && auth.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    "ROLE_STUDENT".equals(
                                            authority.getAuthority()
                                    )
                            );

            if (isStudent) {

                Long studentId =
                        studentAccess.requireCurrentStudentId();

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

                    LocalDateTime now =
                            LocalDateTime.ofInstant(
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

                    List<RegistrationInfo> registrations =
                            jdbc.query(
                                    """
                                    SELECT id, status, topic_id
                                    FROM topic_registrations
                                    WHERE group_id = ?
                                    """,
                                    (rs, rowNum) ->
                                            new RegistrationInfo(
                                                    rs.getLong("id"),
                                                    rs.getString("status"),
                                                    rs.getLong("topic_id")
                                            ),
                                    leaderGroupId
                            );

                    boolean validWindow =
                            validRound != null
                            && validRound > 0;

                    boolean validMembers =
                            validGroup != null
                            && validGroup > 0;

                    boolean noRegistration =
                            registrations.isEmpty();

                    boolean reRegistration =
                            registrations.size() == 1
                            && (
                                "REJECTED".equals(
                                        registrations.get(0).status()
                                )
                                || "CANCELLED".equals(
                                        registrations.get(0).status()
                                )
                            );

                    if (registrations.size() == 1) {

                        RegistrationInfo current =
                                registrations.get(0);

                        registrationId = current.id();
                        registrationStatus = current.status();
                        registeredTopicId = current.topicId();

                        canCancel =
                                validWindow
                                && "PENDING".equals(
                                        current.status()
                                );
                    }

                    canRegister =
                            validWindow
                            && validMembers
                            && (
                                noRegistration
                                || reRegistration
                            );

                    canReRegister =
                            validWindow
                            && validMembers
                            && reRegistration;
                }
            }
        }

        model.addAttribute("topics", topics);
        model.addAttribute("roundId", roundId);
        model.addAttribute("leaderGroupId", leaderGroupId);

        model.addAttribute("registrationId", registrationId);
        model.addAttribute("registrationStatus", registrationStatus);
        model.addAttribute("registeredTopicId", registeredTopicId);

        model.addAttribute("canRegister", canRegister);
        model.addAttribute("canReRegister", canReRegister);
        model.addAttribute("canCancel", canCancel);

        return "topics/list";
    }

    // =====================================================
    // C03 + C04.3 + C04.4
    // DANG KY MOI / DANG KY LAI
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{topicId:\\d+}/register")
    public String registerTopic(
            @PathVariable("topicId") Long topicId,
            @RequestParam("groupId") Long groupId,
            @RequestParam("roundId") Long roundId,
            RedirectAttributes redirect) {

        try {

            registrationService.registerTopic(
                    groupId,
                    topicId
            );

            redirect.addFlashAttribute(
                    "successMessage",
                    "Dang ky de tai thanh cong! "
                            + "Ho so dang cho truong khoa duyet."
            );

        } catch (
                IllegalArgumentException |
                IllegalStateException |
                DataIntegrityViolationException ex
        ) {

            String errorMessage =
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the dang ky do vi pham "
                                    + "rang buoc du lieu."
                            : ex.getMessage();

            redirect.addFlashAttribute(
                    "errorMessage",
                    errorMessage
            );
        }

        return "redirect:/topics?roundId=" + roundId;
    }

    // =====================================================
    // C04.4 - HUY DANG KY DE TAI
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/registrations/{registrationId:\\d+}/cancel")
    public String cancelRegistration(
            @PathVariable("registrationId") Long registrationId,
            @RequestParam("roundId") Long roundId,
            RedirectAttributes redirect) {

        try {

            registrationService.cancelRegistration(
                    registrationId
            );

            redirect.addFlashAttribute(
                    "successMessage",
                    "Huy dang ky de tai thanh cong! "
                            + "Ban co the dang ky lai trong thoi han."
            );

        } catch (
                IllegalArgumentException |
                IllegalStateException |
                DataIntegrityViolationException ex
        ) {

            String errorMessage =
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the huy do vi pham "
                                    + "rang buoc du lieu."
                            : ex.getMessage();

            redirect.addFlashAttribute(
                    "errorMessage",
                    errorMessage
            );
        }

        return "redirect:/topics?roundId=" + roundId;
    }
}
