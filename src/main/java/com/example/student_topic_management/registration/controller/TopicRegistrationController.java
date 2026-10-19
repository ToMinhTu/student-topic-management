
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
    // C03.1 - XEM DANH SACH DE TAI THEO DOT
    // =====================================================

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping
    public String listTopics(
            @RequestParam(
                    name = "roundId",
                    required = false
            ) Long roundId,
            Model model) {

        // 1. Lay danh sach dot dang ky
        List<RegistrationRound> rounds = roundRepository
                .findAllByOrderByCreatedAtDescIdDesc(
                        Pageable.unpaged()
                )
                .getContent();

        model.addAttribute("rounds", rounds);

        // 2. Gia tri mac dinh
        List<TopicView> topics = List.of();
        Long leaderGroupId = null;

        boolean canRegister = false;
        boolean canReRegister = false;

        // 3. Chi xu ly khi co roundId hop le
        if (roundId != null && roundId > 0) {

            // Lay cac de tai da duoc cong bo
            topics = topicRepository.findPublishedTopics(roundId);

            Authentication auth = SecurityContextHolder
                    .getContext()
                    .getAuthentication();

            // Kiem tra quyen sinh vien
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

                // 4. Xac dinh sinh vien hien tai
                Long studentId =
                        studentAccess.requireCurrentStudentId();

                // 5. Tim nhom ma sinh vien la truong nhom
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

                    // 6. Lay thoi gian UTC
                    LocalDateTime now =
                            LocalDateTime.ofInstant(
                                    clock.instant(),
                                    ZoneOffset.UTC
                            );

                    // 7. Kiem tra dot dang ky OPEN
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

                    // 8. Kiem tra so thanh vien trong nhom
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

                    // 9. Lay trang thai ho so dang ky
                    List<String> registrationStatuses =
                            jdbc.queryForList(
                                    """
                                    SELECT status
                                    FROM topic_registrations
                                    WHERE group_id = ?
                                    """,
                                    String.class,
                                    leaderGroupId
                            );

                    // Nhom chua co ho so
                    boolean noRegistration =
                            registrationStatuses.isEmpty();

                    // Nhom co ho so da bi tu choi
                    boolean rejectedRegistration =
                            registrationStatuses.size() == 1
                            && "REJECTED".equals(
                                    registrationStatuses.get(0)
                            );

                    // 10. Kiem tra dieu kien chung
                    boolean validConditions =
                            validRound != null
                            && validRound > 0
                            && validGroup != null
                            && validGroup > 0;

                    // 11. Cho phep dang ky moi hoac dang ky lai
                    canRegister =
                            validConditions
                            && (
                                noRegistration
                                || rejectedRegistration
                            );

                    // 12. Xac dinh hien nut Dang ky lai
                    canReRegister =
                            validConditions
                            && rejectedRegistration;
                }
            }
        }

        // 13. Truyen du lieu sang Thymeleaf
        model.addAttribute("topics", topics);
        model.addAttribute("roundId", roundId);
        model.addAttribute("leaderGroupId", leaderGroupId);
        model.addAttribute("canRegister", canRegister);
        model.addAttribute("canReRegister", canReRegister);

        return "topics/list";
    }

    // =====================================================
    // C03.2 + C04.3 - DANG KY / DANG KY LAI DE TAI
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{topicId:\\d+}/register")
    public String registerTopic(
            @PathVariable("topicId") Long topicId,
            @RequestParam("groupId") Long groupId,
            @RequestParam("roundId") Long roundId,
            RedirectAttributes redirect) {

        try {

            // Service tu quyet dinh INSERT hay UPDATE
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

        // Tro ve danh sach de tai cua dot dang ky
        return "redirect:/topics?roundId=" + roundId;
    }
}
