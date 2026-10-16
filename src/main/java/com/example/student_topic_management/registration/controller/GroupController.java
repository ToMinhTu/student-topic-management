
package com.example.student_topic_management.registration.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import com.example.student_topic_management.registration.dto.GroupCreateForm;
import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.GroupStatus;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;
import com.example.student_topic_management.registration.service.GroupService;
import com.example.student_topic_management.registration.service.StudentAccess;

@Controller
@RequestMapping("/groups")
public class GroupController {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final JdbcTemplate jdbc;
    private final GroupService groupService;
    private final Clock clock;
    private final StudentAccess studentAccess;

    public GroupController(
            StudentGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            JdbcTemplate jdbc,
            GroupService groupService,
            Clock clock,
            StudentAccess studentAccess) {

        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.jdbc = jdbc;
        this.groupService = groupService;
        this.clock = clock;
        this.studentAccess = studentAccess;
    }

    // =====================================
    // CAC DTO HIEN THI
    // =====================================

    public record GroupView(
            Long id,
            Long roundId,
            String name,
            String status,
            long memberCount
    ) {}

    public record MemberView(
            Long studentId,
            String studentCode,
            String fullName,
            Byte slotNo
    ) {}

    private record StudentInfo(
            Long studentId,
            String studentCode,
            String fullName
    ) {}

    public record RoundOption(
            Long id,
            String name
    ) {}

    // =====================================
    // LAY CAC DOT DANG KY DANG MO
    // =====================================

    private void loadOpenRounds(Model model) {

        LocalDateTime now = LocalDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );

        List<RoundOption> rounds = jdbc.query(
                """
                SELECT id, name
                FROM registration_rounds
                WHERE status = 'OPEN'
                  AND student_starts_at <= ?
                  AND student_ends_at > ?
                ORDER BY id DESC
                """,
                (rs, rowNum) -> new RoundOption(
                        rs.getLong("id"),
                        rs.getString("name")
                ),
                now,
                now
        );

        model.addAttribute("rounds", rounds);
    }

    // =====================================
    // C01.1 - DANH SACH NHOM
    // GET /groups
    // =====================================

    @GetMapping
    public String listGroups(Model model) {

        List<GroupView> groups = groupRepository.findAll()
                .stream()
                .map(group -> new GroupView(
                        group.getId(),
                        group.getRoundId(),
                        group.getName(),
                        group.getStatus().name(),
                        memberRepository
                                .countByGroup_IdAndLeftAtIsNull(
                                        group.getId())
                ))
                .toList();

        model.addAttribute("groups", groups);

        return "groups/list";
    }

    // =====================================
    // C01.3 - FORM TAO NHOM
    // GET /groups/create
    // =====================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/create")
    public String showCreateGroup(Model model) {

        model.addAttribute(
                "groupForm",
                new GroupCreateForm()
        );

        loadOpenRounds(model);

        return "groups/create";
    }

    // =====================================
    // C01.3 - XU LY TAO NHOM
    // POST /groups/create
    // =====================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/create")
    public String createGroup(
            @Valid @ModelAttribute("groupForm") GroupCreateForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirect) {

        if (errors.hasErrors()) {
            loadOpenRounds(model);
            return "groups/create";
        }

        try {
            Long groupId = groupService.createGroup(form);

            redirect.addFlashAttribute(
                    "successMessage",
                    "Tao nhom thanh cong!"
            );

            return "redirect:/groups/" + groupId;

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException ex) {

            model.addAttribute(
                    "errorMessage",
                    ex instanceof DataIntegrityViolationException
                            ? "Du lieu vi pham rang buoc."
                            : ex.getMessage()
            );

            loadOpenRounds(model);

            return "groups/create";
        }
    }

    // =====================================
    // C01.2 - CHI TIET NHOM
    // GET /groups/{id}
    // =====================================

    @GetMapping("/{id:\\d+}")
    public String groupDetail(
            @PathVariable("id") Long id,
            Model model) {

        StudentGroup group = groupRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Nhom khong ton tai")
                );

        List<GroupMember> activeMembers =
                memberRepository.findActiveMembers(id);

        List<StudentInfo> studentInfos = jdbc.query(
                """
                SELECT
                    s.user_id AS student_id,
                    s.student_code,
                    u.full_name
                FROM group_members gm
                JOIN students s ON s.user_id = gm.student_id
                JOIN users u ON u.id = s.user_id
                WHERE gm.group_id = ?
                  AND gm.left_at IS NULL
                """,
                (rs, rowNum) -> new StudentInfo(
                        rs.getLong("student_id"),
                        rs.getString("student_code"),
                        rs.getString("full_name")
                ),
                id
        );

        Map<Long, StudentInfo> infoById =
                studentInfos.stream()
                        .collect(Collectors.toMap(
                                StudentInfo::studentId,
                                Function.identity()
                        ));

        List<MemberView> members = activeMembers.stream()
                .map(member -> {

                    StudentInfo info = infoById.get(
                            member.getStudentId()
                    );

                    return new MemberView(
                            member.getStudentId(),
                            info != null
                                    ? info.studentCode()
                                    : "Khong tim thay",
                            info != null
                                    ? info.fullName()
                                    : "Khong tim thay",
                            member.getSlotNo()
                    );
                })
                .sorted((a, b) ->
                        Byte.compare(
                                a.slotNo(),
                                b.slotNo()
                        ))
                .toList();

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("memberCount", members.size());

        // C01 - KIEM TRA QUYEN XEM THANH VIEN

        Authentication currentAuth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean isAdmin = currentAuth != null
                && currentAuth.getAuthorities().stream()
                        .anyMatch(a ->
                                "ROLE_ADMIN".equals(a.getAuthority()));

        boolean isStudentMember = false;

        boolean authenticatedStudent = currentAuth != null
                && currentAuth.isAuthenticated()
                && currentAuth.getAuthorities().stream()
                        .anyMatch(a ->
                                "ROLE_STUDENT".equals(a.getAuthority()));

        if (authenticatedStudent) {
            Long currentStudentId =
                    studentAccess.requireCurrentStudentId();

            isStudentMember = activeMembers.stream()
                    .anyMatch(member ->
                            currentStudentId.equals(
                                    member.getStudentId()));
        }

        boolean canViewMembers = isAdmin || isStudentMember;

        model.addAttribute(
                "canViewMembers",
                canViewMembers
        );

        // Khong gui danh sach thanh vien cho
        // nguoi khong co quyen xem
        if (!canViewMembers) {
            model.addAttribute("members", List.of());
        }

        // =====================================
        // KIEM TRA DIEU KIEN HIEN NUT JOIN
        // =====================================

        boolean canJoin = false;

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean isStudent = auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                        .anyMatch(authority ->
                                "ROLE_STUDENT".equals(
                                        authority.getAuthority()
                                ));

        if (isStudent
                && group.getStatus() == GroupStatus.FORMING) {

            Long studentId =
                    studentAccess.requireCurrentStudentId();

            boolean alreadyInAnyGroup =
                    memberRepository
                            .existsByStudentIdAndLeftAtIsNull(
                                    studentId);

            boolean joinedThisGroupBefore =
                    memberRepository
                            .existsByGroup_IdAndStudentId(
                                    id,
                                    studentId
                            );

            if (!alreadyInAnyGroup
                    && !joinedThisGroupBefore) {

                Integer maxSize = jdbc.queryForObject(
                        """
                        SELECT group_max_size
                        FROM registration_rounds
                        WHERE id = ?
                        """,
                        Integer.class,
                        group.getRoundId()
                );

                Integer activeRegistrations =
                        jdbc.queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM topic_registrations
                                WHERE group_id = ?
                                  AND status IN ('PENDING', 'APPROVED')
                                """,
                                Integer.class,
                                id
                        );

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
                        group.getRoundId(),
                        now,
                        now
                );

                long leaderCount = members.stream()
                        .filter(member ->
                                member.slotNo() != null
                                && member.slotNo().intValue() == 1)
                        .count();

                canJoin = maxSize != null
                        && maxSize >= 1
                        && maxSize <= 5
                        && members.size() < maxSize
                        && leaderCount == 1
                        && activeRegistrations != null
                        && activeRegistrations == 0
                        && validRound != null
                        && validRound > 0;
            }
        }

        model.addAttribute("canJoin", canJoin);

        return "groups/detail";
    }

    // =====================================
    // C01.4 - THAM GIA NHOM
    // POST /groups/{id}/join
    // =====================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{id:\\d+}/join")
    public String joinGroup(
            @PathVariable("id") Long id,
            RedirectAttributes redirect) {

        try {
            groupService.joinGroup(id);

            redirect.addFlashAttribute(
                    "successMessage",
                    "Tham gia nhom thanh cong!"
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException ex) {

            redirect.addFlashAttribute(
                    "errorMessage",
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the tham gia do vi pham rang buoc du lieu."
                            : ex.getMessage()
            );
        }

        return "redirect:/groups/" + id;
    }
}
