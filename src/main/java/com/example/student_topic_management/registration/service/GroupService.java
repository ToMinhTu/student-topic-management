
package com.example.student_topic_management.registration.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.registration.dto.GroupCreateForm;
import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.GroupStatus;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;

@Service
public class GroupService {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final StudentAccess studentAccess;
    private final RoundAccess roundAccess;
    private final GroupRegistrationAccess registrationAccess;
    private final Clock clock;

    public GroupService(
            StudentGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            StudentAccess studentAccess,
            RoundAccess roundAccess,
            GroupRegistrationAccess registrationAccess,
            Clock clock) {

        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.studentAccess = studentAccess;
        this.roundAccess = roundAccess;
        this.registrationAccess = registrationAccess;
        this.clock = clock;
    }

    // =====================================
    // C01.3 - TAO NHOM SINH VIEN
    // =====================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long createGroup(GroupCreateForm form) {

        if (form == null) {
            throw new IllegalArgumentException(
                    "Du lieu tao nhom khong hop le");
        }

        Long roundId = form.getRoundId();

        requirePositive(roundId, "Ma dot dang ky");

        String groupName = form.getName();

        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException(
                    "Ten nhom khong duoc de trong");
        }

        groupName = groupName.trim();

        if (groupName.length() > 150) {
            throw new IllegalArgumentException(
                    "Ten nhom toi da 150 ky tu");
        }

        // Lay sinh vien tu tai khoan dang nhap
        Long studentId =
                studentAccess.requireCurrentStudentId();

        requirePositive(studentId, "Ma sinh vien");

        // Khoa theo thu tu: Round -> Student
        roundAccess.lockRound(roundId);
        studentAccess.lockEnabledStudent(studentId);

        Instant now = clock.instant();

        // Kiem tra dot dang ky dang OPEN va dung pha 2
        roundAccess.requireStudentWindow(roundId, now);

        // Moi sinh vien chi duoc co mot nhom active
        if (memberRepository
                .existsByStudentIdAndLeftAtIsNull(studentId)) {

            throw new IllegalStateException(
                    "Sinh vien da tham gia mot nhom");
        }

        // Tao nhom trang thai FORMING
        StudentGroup group =
                new StudentGroup(roundId, groupName);

        group.setStatus(GroupStatus.FORMING);

        group.setCreatedAt(
                LocalDateTime.ofInstant(
                        now,
                        ZoneOffset.UTC
                )
        );

        group = groupRepository.save(group);

        // Tu dong them sinh vien tao nhom
        // vao vi tri truong nhom slot 1
        GroupMember leader =
                new GroupMember(
                        group,
                        studentId,
                        (byte) 1
                );

        leader.setJoinedAt(
                LocalDateTime.ofInstant(
                        now,
                        ZoneOffset.UTC
                )
        );

        memberRepository.save(leader);

        return group.getId();
    }

    // =====================================
    // C01.4 - SINH VIEN THAM GIA NHOM
    // =====================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void joinGroup(Long groupId) {

        requirePositive(groupId, "Ma nhom");

        // Doc roundId ban dau de xac dinh khoa
        StudentGroup initialGroup =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nhom khong ton tai"
                                )
                        );

        Long roundId = initialGroup.getRoundId();

        // Lay ID sinh vien tu nguoi dang nhap
        Long studentId =
                studentAccess.requireCurrentStudentId();

        requirePositive(studentId, "Ma sinh vien");

        // Thu tu khoa:
        // Round -> Student -> Group
        roundAccess.lockRound(roundId);

        studentAccess.lockEnabledStudent(studentId);

        StudentGroup group =
                groupRepository.findGroupForUpdate(groupId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nhom khong ton tai"
                                )
                        );

        // Kiem tra lai sau khi lay khoa
        if (!roundId.equals(group.getRoundId())) {
            throw new IllegalStateException(
                    "Dot dang ky cua nhom da thay doi");
        }

        Instant now = clock.instant();

        // Kiem tra thoi gian dang ky
        roundAccess.requireStudentWindow(
                roundId,
                now
        );

        // Chi cho phep tham gia nhom FORMING
        if (group.getStatus() != GroupStatus.FORMING) {
            throw new IllegalStateException(
                    "Nhom da bi khoa hoac hoan thanh");
        }

        // PENDING va APPROVED khong duoc sua thanh vien
        registrationAccess
                .requireNoActiveRegistration(groupId);

        // Sinh vien khong duoc co nhom active khac
        if (memberRepository
                .existsByStudentIdAndLeftAtIsNull(studentId)) {

            throw new IllegalStateException(
                    "Sinh vien dang tham gia mot nhom khac");
        }

        // Khong quay lai nhom da tung roi theo quy tac v1
        if (memberRepository
                .existsByGroup_IdAndStudentId(
                        groupId,
                        studentId
                )) {

            throw new IllegalStateException(
                    "Sinh vien da tung tham gia nhom nay");
        }

        // Lay so thanh vien toi da cua dot dang ky
        int maxSize =
                roundAccess.getGroupMaxSize(roundId);

        if (maxSize < 1 || maxSize > 5) {
            throw new IllegalStateException(
                    "So thanh vien toi da khong hop le");
        }

        // Lay danh sach thanh vien active
        // sau khi da khoa group
        List<GroupMember> members =
                memberRepository.findActiveMembers(groupId);

        // Kiem tra suc chua nhom
        if (members.size() >= maxSize) {
            throw new IllegalStateException(
                    "Nhom da du thanh vien");
        }

        // Phai co dung mot truong nhom o slot 1
        long leaderCount = members.stream()
                .filter(member ->
                        member.getSlotNo() != null
                        && member.getSlotNo().intValue() == 1
                )
                .count();

        if (leaderCount != 1) {
            throw new IllegalStateException(
                    "Nhom khong co dung mot truong nhom");
        }

        // Tim slot trong nho nhat tu 2 den maxSize
        byte freeSlot = 0;

        for (int slot = 2; slot <= maxSize; slot++) {

            final int currentSlot = slot;

            boolean occupied = members.stream()
                    .anyMatch(member ->
                            member.getSlotNo() != null
                            && member.getSlotNo().intValue()
                                    == currentSlot
                    );

            if (!occupied) {
                freeSlot = (byte) slot;
                break;
            }
        }

        if (freeSlot == 0) {
            throw new IllegalStateException(
                    "Nhom khong con vi tri trong");
        }

        // Them sinh vien vao nhom
        GroupMember member =
                new GroupMember(
                        group,
                        studentId,
                        freeSlot
                );

        member.setJoinedAt(
                LocalDateTime.ofInstant(
                        now,
                        ZoneOffset.UTC
                )
        );

        memberRepository.save(member);
    }

    // =====================================
    // KIEM TRA ID HOP LE
    // =====================================

    private void requirePositive(Long id, String field) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    field + " phai lon hon 0");
        }
    }
}
