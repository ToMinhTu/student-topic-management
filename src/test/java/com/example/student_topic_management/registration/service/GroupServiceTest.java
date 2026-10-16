package com.example.student_topic_management.registration.service;

import com.example.student_topic_management.registration.dto.GroupCreateForm;
import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.GroupStatus;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.test.util.ReflectionTestUtils;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private StudentGroupRepository groupRepository;

    @Mock
    private GroupMemberRepository memberRepository;

    @Mock
    private StudentAccess studentAccess;

    @Mock
    private RoundAccess roundAccess;

    @Mock
    private GroupRegistrationAccess registrationAccess;

    @Mock
    private Clock clock;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query updateQuery;

    @InjectMocks
    private GroupService groupService;

    private static final Instant TEST_TIME =
            Instant.parse("2026-10-15T10:00:00Z");

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant())
                .thenReturn(TEST_TIME);
        ReflectionTestUtils.setField(groupService, "entityManager", entityManager);
    }

    // =====================================
    // TEST 1: TAO NHOM THANH CONG
    // =====================================

    @Test
    void createGroup_success() {

        GroupCreateForm form = new GroupCreateForm();

        form.setName("Nhóm đồ án 01");
        form.setRoundId(1L);

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(10L);

        when(memberRepository
                .existsByStudentIdAndLeftAtIsNull(10L))
                .thenReturn(false);

        when(groupRepository.save(any(StudentGroup.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        groupService.createGroup(form);

        verify(roundAccess).lockRound(1L);

        verify(studentAccess)
                .lockEnabledStudent(10L);

        verify(roundAccess)
                .requireStudentWindow(
                        eq(1L),
                        any(Instant.class));

        verify(groupRepository)
                .save(any(StudentGroup.class));

        verify(memberRepository)
                .save(argThat(member ->
                        member.getStudentId().equals(10L)
                        && member.getSlotNo() == 1));
    }

    // =====================================
    // TEST 2: DA CO NHOM KHONG DUOC TAO MOI
    // =====================================

    @Test
    void createGroup_studentAlreadyHasGroup() {

        GroupCreateForm form = new GroupCreateForm();

        form.setName("Nhóm đồ án 02");
        form.setRoundId(1L);

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(10L);

        when(memberRepository
                .existsByStudentIdAndLeftAtIsNull(10L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> groupService.createGroup(form));

        verify(groupRepository, never())
                .save(any(StudentGroup.class));

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }

    // =====================================
    // TEST 3: THAM GIA NHOM THANH CONG
    // =====================================

    @Test
    void joinGroup_success() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(roundAccess.getGroupMaxSize(1L))
                .thenReturn(3);

        GroupMember leader =
                new GroupMember(group, 10L, (byte) 1);

        when(memberRepository.findActiveMembers(5L))
                .thenReturn(List.of(leader));

        groupService.joinGroup(5L);

        verify(roundAccess).lockRound(1L);

        verify(studentAccess)
                .lockEnabledStudent(20L);

        verify(registrationAccess)
                .requireNoActiveRegistration(5L);

        verify(memberRepository)
                .save(argThat(member ->
                        member.getStudentId().equals(20L)
                        && member.getSlotNo() == 2));
    }

    // =====================================
    // TEST 4: NHOM DA DAY
    // =====================================

    @Test
    void joinGroup_groupIsFull() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(roundAccess.getGroupMaxSize(1L))
                .thenReturn(3);

        GroupMember leader =
                new GroupMember(group, 10L, (byte) 1);

        GroupMember member2 =
                new GroupMember(group, 11L, (byte) 2);

        GroupMember member3 =
                new GroupMember(group, 12L, (byte) 3);

        when(memberRepository.findActiveMembers(5L))
                .thenReturn(List.of(
                        leader,
                        member2,
                        member3));

        assertThrows(
                IllegalStateException.class,
                () -> groupService.joinGroup(5L));

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }

    // =====================================
    // TEST 5: SINH VIEN DA CO NHOM
    // =====================================

    @Test
    void joinGroup_studentAlreadyHasGroup() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(memberRepository
                .existsByStudentIdAndLeftAtIsNull(20L))
                .thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> groupService.joinGroup(5L));

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }

    // =====================================
    // C02.1 - TEST 6: ROI NHOM THANH CONG
    // =====================================

    @Test
    void leaveGroup_success() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        GroupMember leader =
                new GroupMember(group, 10L, (byte) 1);

        GroupMember member =
                new GroupMember(group, 20L, (byte) 2);

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(memberRepository.findActiveMembers(5L))
                .thenReturn(List.of(leader, member));

        groupService.leaveGroup(5L);

        LocalDateTime expectedTime =
                LocalDateTime.ofInstant(
                        TEST_TIME,
                        ZoneOffset.UTC);

        assertEquals(
                expectedTime,
                member.getLeftAt());

        assertNull(leader.getLeftAt());

        verify(roundAccess).lockRound(1L);

        verify(studentAccess)
                .lockEnabledStudent(20L);

        verify(roundAccess)
                .requireStudentWindow(1L, TEST_TIME);

        verify(registrationAccess)
                .requireNoActiveRegistration(5L);

        verify(memberRepository).save(member);

        verify(memberRepository, never())
                .delete(any(GroupMember.class));
    }

    // =====================================
    // C02.1 - TEST 7:
    // TRUONG NHOM KHONG DUOC ROI TRUC TIEP
    // =====================================

    @Test
    void leaveGroup_leaderCannotLeave() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        GroupMember leader =
                new GroupMember(group, 10L, (byte) 1);

        GroupMember member =
                new GroupMember(group, 20L, (byte) 2);

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(10L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(memberRepository.findActiveMembers(5L))
                .thenReturn(List.of(leader, member));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> groupService.leaveGroup(5L));

        assertTrue(
                exception.getMessage()
                        .contains("Truong nhom"));

        assertNull(leader.getLeftAt());

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }

    // =====================================
    // C02.1 - TEST 8:
    // SINH VIEN NGOAI NHOM KHONG DUOC ROI
    // =====================================

    @Test
    void leaveGroup_studentNotInGroup() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        GroupMember leader =
                new GroupMember(group, 10L, (byte) 1);

        GroupMember member =
                new GroupMember(group, 20L, (byte) 2);

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        // Sinh vien 30 khong thuoc nhom
        when(studentAccess.requireCurrentStudentId())
                .thenReturn(30L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(memberRepository.findActiveMembers(5L))
                .thenReturn(List.of(leader, member));

        assertThrows(
                IllegalStateException.class,
                () -> groupService.leaveGroup(5L));

        assertNull(leader.getLeftAt());
        assertNull(member.getLeftAt());

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }

    // =====================================
    // C02.1 - TEST 9:
    // NHOM DA KHOA KHONG DUOC ROI
    // =====================================

    @Test
    void leaveGroup_groupIsLocked() {

        StudentGroup group =
                new StudentGroup(1L, "Nhóm đồ án 01");

        // Mo phong nhom khong con FORMING
        GroupStatus lockedStatus = null;

        for (GroupStatus status : GroupStatus.values()) {
            if (status != GroupStatus.FORMING) {
                lockedStatus = status;
                break;
            }
        }

        assertNotNull(
                lockedStatus,
                "GroupStatus phai co trang thai khac FORMING");

        group.setStatus(lockedStatus);

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        assertThrows(
                IllegalStateException.class,
                () -> groupService.leaveGroup(5L));

        verify(memberRepository, never())
                .save(any(GroupMember.class));

        verify(memberRepository, never())
                .findActiveMembers(5L);
    }
    // =====================================
    // C02.2 - HAM CHUAN BI CHO CAC TEST
    // =====================================

    private StudentGroup prepareTransfer(Long actingStudentId,
                                         List<GroupMember> members) {
        StudentGroup group = new StudentGroup(1L, "Nhom test C02.2");
        when(groupRepository.findById(5L)).thenReturn(Optional.of(group));
        when(studentAccess.requireCurrentStudentId()).thenReturn(actingStudentId);
        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));
        when(memberRepository.findActiveMembers(5L)).thenReturn(members);
        return group;
    }

    private GroupMember groupMember(StudentGroup group, long studentId,
                                    int slot, long id) {
        GroupMember member = new GroupMember(group, studentId, (byte) slot);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    // TEST 10 - TRUONG NHOM CHUYEN QUYEN THANH CONG
    @Test
    void transferLeader_success() {
        StudentGroup group = new StudentGroup(1L, "Nhom test C02.2");
        GroupMember leader = groupMember(group, 10L, 1, 101L);
        GroupMember receiver = groupMember(group, 20L, 2, 102L);
        prepareTransfer(10L, List.of(leader, receiver));

        when(entityManager.createNativeQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        groupService.transferLeader(5L, 20L);

        verify(roundAccess).lockRound(1L);
        verify(studentAccess).lockEnabledStudent(10L);
        verify(roundAccess).requireStudentWindow(1L, TEST_TIME);
        verify(registrationAccess).requireNoActiveRegistration(5L);
        verify(entityManager).flush();
        verify(updateQuery, times(3)).executeUpdate();
        verify(entityManager).clear();

        // Hoan doi: truong nhom 1 -> 3 -> 2; thanh vien 2 -> 1.
        ArgumentCaptor<Object> slots = ArgumentCaptor.forClass(Object.class);
        verify(updateQuery, times(3))
                .setParameter(eq("newSlot"), slots.capture());
        assertEquals(List.of(3, 1, 2), slots.getAllValues());
        assertNull(leader.getLeftAt());
        assertNull(receiver.getLeftAt());
    }

    // TEST 11 - THANH VIEN THUONG KHONG DUOC CHUYEN QUYEN
    @Test
    void transferLeader_nonLeaderCannotTransfer() {
        StudentGroup group = new StudentGroup(1L, "Nhom test");
        prepareTransfer(20L, List.of(
                groupMember(group, 10L, 1, 101L),
                groupMember(group, 20L, 2, 102L)));

        assertThrows(IllegalStateException.class,
                () -> groupService.transferLeader(5L, 10L));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    // TEST 12 - KHONG CHUYEN CHO NGUOI NGOAI NHOM
    @Test
    void transferLeader_receiverNotInGroup() {
        StudentGroup group = new StudentGroup(1L, "Nhom test");
        prepareTransfer(10L, List.of(
                groupMember(group, 10L, 1, 101L),
                groupMember(group, 20L, 2, 102L)));

        assertThrows(IllegalStateException.class,
                () -> groupService.transferLeader(5L, 30L));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    // TEST 13 - NHOM MOT THANH VIEN
    @Test
    void transferLeader_singleMember() {
        StudentGroup group = new StudentGroup(1L, "Nhom test");
        prepareTransfer(10L, List.of(groupMember(group, 10L, 1, 101L)));

        assertThrows(IllegalStateException.class,
                () -> groupService.transferLeader(5L, 20L));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    // TEST 14 - NHOM DA KHOA
    @Test
    void transferLeader_lockedGroup() {
        StudentGroup group = new StudentGroup(1L, "Nhom test");
        GroupStatus locked = java.util.Arrays.stream(GroupStatus.values())
                .filter(status -> status != GroupStatus.FORMING)
                .findFirst().orElseThrow();
        group.setStatus(locked);
        when(groupRepository.findById(5L)).thenReturn(Optional.of(group));
        when(studentAccess.requireCurrentStudentId()).thenReturn(10L);
        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class,
                () -> groupService.transferLeader(5L, 20L));
        verify(memberRepository, never()).findActiveMembers(5L);
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    // TEST 15 - NHOM DAY 5/5 KHONG CO SLOT TAM
    @Test
    void transferLeader_fullFiveMemberGroup() {
        StudentGroup group = new StudentGroup(1L, "Nhom test");
        prepareTransfer(10L, List.of(
                groupMember(group, 10L, 1, 101L),
                groupMember(group, 20L, 2, 102L),
                groupMember(group, 30L, 3, 103L),
                groupMember(group, 40L, 4, 104L),
                groupMember(group, 50L, 5, 105L)));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> groupService.transferLeader(5L, 20L));
        assertTrue(ex.getMessage().contains("het 5 vi tri"));
        verify(entityManager, never()).createNativeQuery(anyString());
    }

    // TEST 16 - LENH SQL THAT BAI PHAI NEM EXCEPTION
    // Transaction cua Spring se rollback khi exception thoat ra.
    @Test
    void transferLeader_secondSqlUpdateFails() {
        StudentGroup group = new StudentGroup(1L, "Nhom test");
        prepareTransfer(10L, List.of(
                groupMember(group, 10L, 1, 101L),
                groupMember(group, 20L, 2, 102L)));

        when(entityManager.createNativeQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1, 0);

        assertThrows(IllegalStateException.class,
                () -> groupService.transferLeader(5L, 20L));
        verify(updateQuery, times(2)).executeUpdate();
        verify(entityManager, never()).clear();
    }

}
