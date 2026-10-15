
package com.example.student_topic_management.registration.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;

@Controller
@RequestMapping("/groups")
public class GroupController {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public GroupController(
            StudentGroupRepository groupRepository,
            GroupMemberRepository memberRepository) {

        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    // Du lieu cho bang danh sach nhom
    public record GroupView(
            Long id,
            Long roundId,
            String name,
            String status,
            long memberCount
    ) {}

    // Du lieu hien thi thanh vien
    public record MemberView(
            Long studentId,
            Byte slotNo
    ) {}

    // =====================================
    // 1. HIEN THI DANH SACH NHOM
    // GET /groups
    // =====================================

    @GetMapping
    public String listGroups(Model model) {

        List<GroupView> groups =
                groupRepository.findAll()
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
    // 2. HIEN THI CHI TIET NHOM
    // GET /groups/{id}
    // =====================================

    @GetMapping("/{id}")
    public String groupDetail(
            @PathVariable("id") Long id,
            Model model) {

        StudentGroup group =
                groupRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Nhom khong ton tai"
                                ));

        List<GroupMember> activeMembers =
                memberRepository.findActiveMembers(id);

        List<MemberView> members = activeMembers
                .stream()
                .map(member -> new MemberView(
                        member.getStudentId(),
                        member.getSlotNo()
                ))
                .toList();

        model.addAttribute("group", group);
        model.addAttribute("members", members);
        model.addAttribute("memberCount", members.size());

        // Chua co xac thuc va kiem tra quyen.
        // Tam thoi khong hien nut tham gia nhom.
        model.addAttribute("canJoin", false);

        return "groups/detail";
    }
}
