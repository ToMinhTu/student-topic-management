
package com.example.student_topic_management.registration.service;

import com.example.student_topic_management.registration.dto.GroupCreateForm;
import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
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

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {

        lenient()
                .when(clock.instant())
                .thenReturn(
                        Instant.parse("2026-10-15T10:00:00Z")
                );
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
                        invocation.getArgument(0)
                );

        groupService.createGroup(form);

        verify(roundAccess).lockRound(1L);

        verify(studentAccess)
                .lockEnabledStudent(10L);

        verify(roundAccess)
                .requireStudentWindow(
                        eq(1L),
                        any(Instant.class)
                );

        verify(groupRepository)
                .save(any(StudentGroup.class));

        verify(memberRepository)
                .save(argThat(member ->
                        member.getStudentId().equals(10L)
                                && member.getSlotNo() == 1
                ));
    }

    // =====================================
    // TEST 2: SINH VIEN DA CO NHOM
    // KHONG DUOC TAO NHOM MOI
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
                () -> groupService.createGroup(form)
        );

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
                new StudentGroup(
                        1L,
                        "Nhóm đồ án 01"
                );

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(roundAccess.getGroupMaxSize(1L))
                .thenReturn(3);

        GroupMember leader =
                new GroupMember(
                        group,
                        10L,
                        (byte) 1
                );

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
                                && member.getSlotNo() == 2
                ));
    }

    // =====================================
    // TEST 4: NHOM DA DU THANH VIEN
    // KHONG DUOC THAM GIA
    // =====================================

    @Test
    void joinGroup_groupIsFull() {

        StudentGroup group =
                new StudentGroup(
                        1L,
                        "Nhóm đồ án 01"
                );

        when(groupRepository.findById(5L))
                .thenReturn(Optional.of(group));

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(20L);

        when(groupRepository.findGroupForUpdate(5L))
                .thenReturn(Optional.of(group));

        when(roundAccess.getGroupMaxSize(1L))
                .thenReturn(3);

        GroupMember leader =
                new GroupMember(
                        group,
                        10L,
                        (byte) 1
                );

        GroupMember member2 =
                new GroupMember(
                        group,
                        11L,
                        (byte) 2
                );

        GroupMember member3 =
                new GroupMember(
                        group,
                        12L,
                        (byte) 3
                );

        when(memberRepository.findActiveMembers(5L))
                .thenReturn(List.of(
                        leader,
                        member2,
                        member3
                ));

        assertThrows(
                IllegalStateException.class,
                () -> groupService.joinGroup(5L)
        );

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }

    // =====================================
    // TEST 5: SINH VIEN DA CO NHOM
    // KHONG DUOC THAM GIA NHOM KHAC
    // =====================================

    @Test
    void joinGroup_studentAlreadyHasGroup() {

        StudentGroup group =
                new StudentGroup(
                        1L,
                        "Nhóm đồ án 01"
                );

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
                () -> groupService.joinGroup(5L)
        );

        verify(memberRepository, never())
                .save(any(GroupMember.class));
    }
}
