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
import java.time.ZoneOffset;

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
    private Clock clock;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant())
                .thenReturn(Instant.parse("2026-10-15T10:00:00Z"));
    }

    @Test
    void createGroup_success() {

        GroupCreateForm form = new GroupCreateForm();
        form.setName("Nhóm đồ án 01");
        form.setRoundId(1L);

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(10L);

        when(memberRepository.existsByStudentIdAndLeftAtIsNull(10L))
                .thenReturn(false);

        when(groupRepository.save(any(StudentGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        groupService.createGroup(form);

        verify(roundAccess).lockRound(1L);
        verify(studentAccess).lockEnabledStudent(10L);

        verify(groupRepository).save(any(StudentGroup.class));

        verify(memberRepository).save(argThat(member ->
                member.getStudentId().equals(10L)
                && member.getSlotNo() == 1
        ));
    }

    @Test
    void createGroup_studentAlreadyHasGroup() {

        GroupCreateForm form = new GroupCreateForm();
        form.setName("Nhóm đồ án 02");
        form.setRoundId(1L);

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(10L);

        when(memberRepository.existsByStudentIdAndLeftAtIsNull(10L))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> groupService.createGroup(form));

        verify(groupRepository, never()).save(any());
        verify(memberRepository, never()).save(any());
    }
}