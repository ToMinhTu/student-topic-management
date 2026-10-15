
package com.example.student_topic_management.registration.controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;

@Controller
@RequestMapping("/groups")
public class GroupController {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final JdbcTemplate jdbc;

    public GroupController(
            StudentGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            JdbcTemplate jdbc) {

        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.jdbc = jdbc;
    }

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

    // GET /groups
    @GetMapping
    public String listGroups(Model model) {

        List<GroupView> groups = groupRepository.findAll()
                .stream()
                .map(group -> new GroupView(
                        group.getId(),
                        group.getRoundId(),
                        group.getName(),
                        group.getStatus().name(),
                        memberRepository.countByGroup_IdAndLeftAtIsNull(
                                group.getId())
                ))
                .toList();

        model.addAttribute("groups", groups);
        return "groups/list";
    }

    // GET /groups/{id}
    @GetMapping("/{id}")
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

        // Lay thong tin sinh vien cua nhom bang mot truy van.
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
                .sorted((a, b) -> Byte.compare(
                        a.slotNo(), b.slotNo()))
                .toList();

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("memberCount", members.size());

        // Chua co dang nhap va phan quyen thuc te.
        model.addAttribute("canJoin", false);

        return "groups/detail";
    }
}
