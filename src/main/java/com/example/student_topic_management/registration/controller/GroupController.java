
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
import org.springframework.web.bind.annotation.RequestParam;
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
    // DTO HIEN THI
    // =====================================

    public record GroupView(
            Long id,
            Long roundId,
            String roundName,
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

    private String findRoundName(Long roundId) {
        return jdbc.query(
                "SELECT name FROM registration_rounds WHERE id = ?",
                (rs, rowNum) -> rs.getString("name"),
                roundId
        ).stream().findFirst().orElse("Không xác định");
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
                        findRoundName(group.getRoundId()),
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
            @Valid @ModelAttribute("groupForm")
            GroupCreateForm form,
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
                                "Nhom khong ton tai"
                        ));

        List<Map<String, Object>> councils = jdbc.queryForList(
                "SELECT id, name, status FROM councils WHERE round_id = ? ORDER BY id",
                group.getRoundId());
        model.addAttribute("councils", councils);
        List<GroupMember> activeMembers =
                memberRepository.findActiveMembers(id);

        Authentication auth =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        boolean isStudent = hasRole(auth, "ROLE_STUDENT");
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN");

        Long currentStudentId = isStudent
                ? studentAccess.requireCurrentStudentId()
                : null;

        GroupMember myMembership = currentStudentId == null
                ? null
                : activeMembers.stream()
                        .filter(m -> currentStudentId.equals(
                                m.getStudentId()))
                        .findFirst()
                        .orElse(null);

        boolean isLeader = myMembership != null
                && myMembership.getSlotNo() != null
                && myMembership.getSlotNo().intValue() == 1;

        boolean canViewMembers =
                isAdmin || myMembership != null;

        List<MemberView> members = List.of();

        // Chi doc thong tin ca nhan cua thanh vien
        // neu nguoi xem co quyen.

        if (canViewMembers) {

            List<StudentInfo> studentInfos = jdbc.query(
                    """
                    SELECT
                        s.user_id AS student_id,
                        s.student_code,
                        u.full_name
                    FROM group_members gm
                    JOIN students s
                        ON s.user_id = gm.student_id
                    JOIN users u
                        ON u.id = s.user_id
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

            members = activeMembers.stream()
                    .map(member -> {

                        StudentInfo info =
                                infoById.get(
                                        member.getStudentId());

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
        }

        // =====================================
        // QUYEN HIEN THI CAC NUT
        // =====================================

        boolean canJoin = false;
        boolean canLeave = false;
        boolean canTransferLeader = false;

        if (isStudent
                && group.getStatus() == GroupStatus.FORMING) {

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
                    group.getRoundId(),
                    now,
                    now
            );

            boolean roundOpen =
                    validRound != null && validRound > 0;

            if (roundOpen) {

                Integer activeRegistrations =
                        jdbc.queryForObject(
                                """
                                SELECT COUNT(*)
                                FROM topic_registrations
                                WHERE group_id = ?
                                  AND status IN (
                                      'PENDING',
                                      'APPROVED'
                                  )
                                """,
                                Integer.class,
                                id
                        );

                boolean noRegistration =
                        activeRegistrations != null
                        && activeRegistrations == 0;

                if (noRegistration) {

                    // C02.1 - Roi nhom:
                    // Truong nhom khong duoc roi truc tiep.

                    canLeave = myMembership != null
                            && myMembership.getSlotNo() != null
                            && myMembership.getSlotNo()
                                    .intValue() != 1;

                    // C02.2 - Chuyen truong nhom:
                    // Can co it nhat 2 thanh vien,
                    // co slot tam trong pham vi 1..5.

                    boolean hasReceiver =
                            activeMembers.stream()
                                    .anyMatch(m ->
                                            m.getSlotNo() != null
                                            && m.getSlotNo()
                                                    .intValue() >= 2
                                            && m.getSlotNo()
                                                    .intValue() <= 5);

                    boolean hasTemporarySlot =
                            java.util.stream.IntStream
                                    .rangeClosed(2, 5)
                                    .anyMatch(slot ->
                                            activeMembers.stream()
                                                    .noneMatch(m ->
                                                            m.getSlotNo()
                                                                    != null
                                                            && m.getSlotNo()
                                                                    .intValue()
                                                                    == slot));

                    long leaderCount =
                            activeMembers.stream()
                                    .filter(m ->
                                            m.getSlotNo() != null
                                            && m.getSlotNo()
                                                    .intValue() == 1)
                                    .count();

                    canTransferLeader = isLeader
                            && activeMembers.size() >= 2
                            && leaderCount == 1
                            && hasReceiver
                            && hasTemporarySlot;

                    // C01.4 - Tham gia nhom.

                    if (myMembership == null
                            && !memberRepository
                                    .existsByStudentIdAndLeftAtIsNull(
                                            currentStudentId)
                            && !memberRepository
                                    .existsByGroup_IdAndStudentId(
                                            id,
                                            currentStudentId)) {

                        Integer maxSize = jdbc.queryForObject(
                                """
                                SELECT group_max_size
                                FROM registration_rounds
                                WHERE id = ?
                                """,
                                Integer.class,
                                group.getRoundId()
                        );

                        canJoin = maxSize != null
                                && maxSize >= 1
                                && maxSize <= 5
                                && activeMembers.size() < maxSize
                                && leaderCount == 1;
                    }
                }
            }
        }
        // =====================================
        // C05 - LIEN KET BAO CAO PDF CUA NHOM
        // =====================================

        Long reportRegistrationId = null;

        // Chi sinh vien dang la thanh vien cua nhom moi duoc xem
        if (isStudent && myMembership != null) {

        List<Long> approvedRegistrations = jdbc.queryForList(
                """
                SELECT id
                FROM topic_registrations
                WHERE group_id = ?
                AND round_id = ?
                AND status = 'APPROVED'
                ORDER BY id DESC
                LIMIT 1
                """,
                Long.class,
                group.getId(),
                group.getRoundId()
        );

        if (!approvedRegistrations.isEmpty()) {
                reportRegistrationId = approvedRegistrations.get(0);
        }
        }

        model.addAttribute("reportRegistrationId", reportRegistrationId);
        // =====================================
        // TRUYEN DU LIEU SANG THYMELEAF
        // =====================================

        model.addAttribute("group", group);
        model.addAttribute("roundName", findRoundName(group.getRoundId()));
        model.addAttribute("members", members);

        model.addAttribute(
                "memberCount",
                activeMembers.size()
        );

        model.addAttribute(
                "canViewMembers",
                canViewMembers
        );

        model.addAttribute("canJoin", canJoin);
        model.addAttribute("canLeave", canLeave);
        model.addAttribute("isLeader", isLeader);

        model.addAttribute(
                "canTransferLeader",
                canTransferLeader
        );

        return "groups/detail";
    }

    // =====================================
    // KIEM TRA VAI TRO DANG NHAP
    // =====================================

    private boolean hasRole(
            Authentication auth,
            String role) {

        return auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                        .anyMatch(a ->
                                role.equals(a.getAuthority()));
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
                            ? "Khong the tham gia do vi pham "
                                + "rang buoc du lieu."
                            : ex.getMessage()
            );
        }

        return "redirect:/groups/" + id;
    }

    // =====================================
    // C02.1 - ROI NHOM
    // POST /groups/{id}/leave
    // =====================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{id:\\d+}/leave")
    public String leaveGroup(
            @PathVariable("id") Long id,
            RedirectAttributes redirect) {

        try {

            groupService.leaveGroup(id);

            redirect.addFlashAttribute(
                    "successMessage",
                    "Ban da roi nhom thanh cong!"
            );

            return "redirect:/groups";

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException ex) {

            redirect.addFlashAttribute(
                    "errorMessage",
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the roi nhom do "
                                + "rang buoc du lieu."
                            : ex.getMessage()
            );

            return "redirect:/groups/" + id;
        }
    }

    // =====================================
    // C02.2 - CHUYEN QUYEN TRUONG NHOM
    // POST /groups/{id}/transfer-leader
    // =====================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{id:\\d+}/transfer-leader")
    public String transferLeader(
            @PathVariable("id") Long id,
            @RequestParam("newLeaderStudentId")
            Long newLeaderStudentId,
            RedirectAttributes redirect) {

        try {

            // GroupService tu lay nguoi thao tac
            // tu tai khoan dang nhap.

            groupService.transferLeader(
                    id,
                    newLeaderStudentId
            );

            redirect.addFlashAttribute(
                    "successMessage",
                    "Chuyen quyen truong nhom thanh cong!"
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 DataIntegrityViolationException ex) {

            redirect.addFlashAttribute(
                    "errorMessage",
                    ex instanceof DataIntegrityViolationException
                            ? "Khong the chuyen quyen do "
                                + "rang buoc du lieu."
                            : ex.getMessage()
            );
        }

        return "redirect:/groups/" + id;
    }
}
