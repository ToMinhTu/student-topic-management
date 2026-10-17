package com.example.student_topic_management.registration.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

    @PersistenceContext
    private EntityManager entityManager;

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
    // C02.1 - SINH VIEN ROI NHOM
    // =====================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void leaveGroup(Long groupId) {

        requirePositive(groupId, "Ma nhom");

        // Tim nhom de lay ma dot dang ky
        StudentGroup initialGroup =
                groupRepository.findById(groupId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nhom khong ton tai")
                        );

        Long roundId = initialGroup.getRoundId();

        // Lay sinh vien dang dang nhap
        Long studentId =
                studentAccess.requireCurrentStudentId();

        requirePositive(studentId, "Ma sinh vien");

        // Khoa theo thu tu:
        // Round -> Student -> Group
        roundAccess.lockRound(roundId);

        studentAccess.lockEnabledStudent(studentId);

        StudentGroup group =
                groupRepository.findGroupForUpdate(groupId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nhom khong ton tai")
                        );

        // Kiem tra lai dot dang ky sau khi khoa
        if (!roundId.equals(group.getRoundId())) {
            throw new IllegalStateException(
                    "Dot dang ky cua nhom da thay doi");
        }

        Instant now = clock.instant();

        // Kiem tra dot dang ky con cho phep
        // sinh vien thay doi thanh vien nhom
        roundAccess.requireStudentWindow(
                roundId,
                now
        );

        // Nhom phai o trang thai FORMING
        if (group.getStatus() != GroupStatus.FORMING) {
            throw new IllegalStateException(
                    "Nhom da bi khoa, khong the roi");
        }

        // Khong cho sua thanh vien khi nhom
        // co dang ky de tai PENDING/APPROVED
        registrationAccess
                .requireNoActiveRegistration(groupId);

        // Lay danh sach thanh vien dang hoat dong
        List<GroupMember> members =
                memberRepository.findActiveMembers(groupId);

        // Tim thanh vien dang dang nhap
        GroupMember currentMember = members.stream()
                .filter(member ->
                        studentId.equals(member.getStudentId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Ban khong thuoc nhom nay")
                );

        // Truong nhom o slot 1
        // phai chuyen quyen truoc khi roi
        if (currentMember.getSlotNo() != null
                && currentMember.getSlotNo().intValue() == 1) {

            throw new IllegalStateException(
                    "Truong nhom phai chuyen quyen "
                    + "truoc khi roi nhom");
        }

        // Khong xoa ban ghi, chi danh dau da roi
        currentMember.setLeftAt(
                LocalDateTime.ofInstant(
                        now,
                        ZoneOffset.UTC
                )
        );

        memberRepository.save(currentMember);
    }

    // =====================================
    // C02.2 - CHUYEN QUYEN TRUONG NHOM
    // =====================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void transferLeader(Long groupId, Long newLeaderStudentId) {
        requirePositive(groupId, "Ma nhom");
        requirePositive(newLeaderStudentId, "Ma sinh vien nhan quyen");

        StudentGroup initialGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhom khong ton tai"));
        Long roundId = initialGroup.getRoundId();
        Long currentStudentId = studentAccess.requireCurrentStudentId();
        requirePositive(currentStudentId, "Ma sinh vien");

        if (currentStudentId.equals(newLeaderStudentId)) {
            throw new IllegalArgumentException("Khong the tu chuyen quyen cho minh");
        }

        // Thu tu khoa: Round -> Student -> Group
        roundAccess.lockRound(roundId);
        studentAccess.lockEnabledStudent(currentStudentId);
        StudentGroup group = groupRepository.findGroupForUpdate(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Nhom khong ton tai"));

        if (!roundId.equals(group.getRoundId())) {
            throw new IllegalStateException("Dot dang ky cua nhom da thay doi");
        }

        Instant now = clock.instant();
        roundAccess.requireStudentWindow(roundId, now);
        if (group.getStatus() != GroupStatus.FORMING) {
            throw new IllegalStateException("Nhom khong cho phep chuyen quyen");
        }
        registrationAccess.requireNoActiveRegistration(groupId);

        List<GroupMember> members = memberRepository.findActiveMembers(groupId);
        if (members.size() < 2 || members.size() > 5) {
            throw new IllegalStateException("So thanh vien nhom khong hop le");
        }

        List<GroupMember> leaders = members.stream()
                .filter(member -> member.getSlotNo() != null
                        && member.getSlotNo().intValue() == 1)
                .toList();
        if (leaders.size() != 1) {
            throw new IllegalStateException("Nhom khong co dung mot truong nhom");
        }

        GroupMember oldLeader = leaders.get(0);
        if (!currentStudentId.equals(oldLeader.getStudentId())) {
            throw new IllegalStateException("Chi truong nhom moi duoc chuyen quyen");
        }

        GroupMember newLeader = members.stream()
                .filter(member -> newLeaderStudentId.equals(member.getStudentId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Sinh vien nhan quyen khong thuoc nhom"));
        if (newLeader.getSlotNo() == null) {
            throw new IllegalStateException("Vi tri nguoi nhan quyen khong hop le");
        }
        int receiverSlot = newLeader.getSlotNo().intValue();
        if (receiverSlot < 2 || receiverSlot > 5) {
            throw new IllegalStateException("Vi tri nguoi nhan quyen khong hop le");
        }

        // Dung slot 2..5 con trong de tranh vi pham unique (group_id, active_slot_no).
        int temporarySlot = 0;
        for (int slot = 2; slot <= 5; slot++) {
            final int candidate = slot;
            boolean occupied = members.stream()
                    .anyMatch(member -> member.getSlotNo() != null
                            && member.getSlotNo().intValue() == candidate);
            if (!occupied) {
                temporarySlot = slot;
                break;
            }
        }
        if (temporarySlot == 0) {
            throw new IllegalStateException(
                    "Nhom da su dung het 5 vi tri. Chua ho tro chuyen quyen khi nhom day 5 thanh vien");
        }

        if (entityManager == null) {
            throw new IllegalStateException("EntityManager chua duoc khoi tao");
        }
        // Flush truoc native SQL; ba update nam trong cung transaction.
        entityManager.flush();
        updateMemberSlot(oldLeader.getId(), groupId, 1, temporarySlot);
        updateMemberSlot(newLeader.getId(), groupId, receiverSlot, 1);
        updateMemberSlot(oldLeader.getId(), groupId, temporarySlot, receiverSlot);
        // JPA cache da cu do SQL native thay doi slot_no.
        entityManager.clear();
    }

    private void updateMemberSlot(Long memberId, Long groupId,
                                  int expectedSlot, int newSlot) {
        int updated = entityManager.createNativeQuery("""
                UPDATE group_members
                   SET slot_no = :newSlot
                 WHERE id = :memberId
                   AND group_id = :groupId
                   AND slot_no = :expectedSlot
                   AND left_at IS NULL
                """)
                .setParameter("newSlot", newSlot)
                .setParameter("memberId", memberId)
                .setParameter("groupId", groupId)
                .setParameter("expectedSlot", expectedSlot)
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("Khong the cap nhat vi tri thanh vien");
        }
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
