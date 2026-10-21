package com.example.student_topic_management.b03;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.*;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.repository.TopicRepository;
import com.example.student_topic_management.service.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TopicPublicationServiceB033Test {
    TopicRepository repository;
    CurrentUserService users;
    TopicAuditService audit;
    TopicPublicationService service;
    Topic topic;
    AppUser dean;
    final Instant now = Instant.parse("2026-10-20T10:00:00Z");

    @BeforeEach void setup() {
        repository = mock(TopicRepository.class);
        users = mock(CurrentUserService.class);
        audit = mock(TopicAuditService.class);
        service = new TopicPublicationService(repository, users, audit, Clock.fixed(now, ZoneOffset.UTC));
        dean = mock(AppUser.class);
        when(dean.getId()).thenReturn(20L);
        when(dean.getEnabled()).thenReturn(true);
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn("DEAN");
        when(dean.getRoles()).thenReturn(Set.of(role));
        when(users.getCurrentUser()).thenReturn(dean);
        topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setStatus(TopicStatus.APPROVED);
        topic.setApprovedBy(20L);
        topic.setApprovedAt(LocalDateTime.ofInstant(now.minusSeconds(60), ZoneOffset.UTC));
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
    }

    @Test void publishesApprovedTopicWithUtcTimestampAndAuditUnderWriteLock() {
        service.publish(10L);
        assertEquals(TopicStatus.PUBLISHED, topic.getStatus());
        assertEquals(LocalDateTime.ofInstant(now, ZoneOffset.UTC), topic.getPublishedAt());
        assertEquals(20L, topic.getApprovedBy());
        assertEquals(LocalDateTime.ofInstant(now.minusSeconds(60), ZoneOffset.UTC), topic.getApprovedAt());
        var order = inOrder(repository, audit);
        order.verify(repository).findByIdForUpdate(10L);
        order.verify(repository).saveAndFlush(topic);
        order.verify(audit).record(topic, 20L, "TOPIC_PUBLISH");
        order.verify(audit).snapshotReview(topic, 20L);
    }

    @ParameterizedTest @EnumSource(value = TopicStatus.class, names = {"DRAFT", "SUBMITTED", "REJECTED", "PUBLISHED"})
    void rejectsNonApprovedStates(TopicStatus state) {
        topic.setStatus(state);
        assertThrows(BusinessException.class, () -> service.publish(10L));
        assertNull(topic.getPublishedAt());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void duplicateRequestCannotChangePublicationTimeOrWriteAnotherAudit() {
        service.publish(10L);
        LocalDateTime first = topic.getPublishedAt();
        clearInvocations(repository, audit);
        assertThrows(BusinessException.class, () -> service.publish(10L));
        assertEquals(first, topic.getPublishedAt());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void existingPublicationTimestampBlocksEvenIfStatusIsInconsistent() {
        topic.setPublishedAt(LocalDateTime.ofInstant(now.minusSeconds(30), ZoneOffset.UTC));
        assertThrows(BusinessException.class, () -> service.publish(10L));
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest @NullSource @ValueSource(longs = {0, -1})
    void invalidIdCannotWrite(Long id) {
        assertThrows(BusinessException.class, () -> service.publish(id));
        verifyNoInteractions(repository, audit);
    }

    @Test void missingTopicCannotWrite() {
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.publish(10L));
        verifyNoInteractions(audit);
    }

    @Test void disabledOrNonDeanCannotListOrPublish() {
        when(dean.getEnabled()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.getApprovedTopics(0));
        assertThrows(AccessDeniedException.class, () -> service.publish(10L));
        when(dean.getEnabled()).thenReturn(true);
        when(dean.getRoles()).thenReturn(Set.of());
        assertThrows(AccessDeniedException.class, () -> service.getApprovedTopics(0));
        assertThrows(AccessDeniedException.class, () -> service.publish(10L));
        verifyNoInteractions(repository, audit);
    }

    @Test void listUsesApprovedScopeAndStableDatabasePagination() {
        PageRequest request = PageRequest.of(0, 10, Sort.by("code").ascending().and(Sort.by("id").ascending()));
        Page<Topic> expected = new PageImpl<>(List.of(topic), request, 1);
        when(repository.findByStatusAndPublishedAtIsNull(TopicStatus.APPROVED, request)).thenReturn(expected);
        assertSame(expected, service.getApprovedTopics(0));
        assertThrows(BusinessException.class, () -> service.getApprovedTopics(-1));
    }

    @Test void failedSaveDoesNotReportPublicationToAudit() {
        when(repository.saveAndFlush(topic)).thenThrow(new IllegalStateException("save failed"));
        assertThrows(IllegalStateException.class, () -> service.publish(10L));
        verifyNoInteractions(audit);
    }
}