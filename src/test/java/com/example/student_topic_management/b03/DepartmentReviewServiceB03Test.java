package com.example.student_topic_management.b03;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.DepartmentReviewForm;
import com.example.student_topic_management.entity.*;
import com.example.student_topic_management.enums.*;
import com.example.student_topic_management.identity.entity.*;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.policy.RoundPolicy;
import com.example.student_topic_management.repository.*;
import com.example.student_topic_management.service.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DepartmentReviewServiceB03Test {
    TopicRepository topics;
    RegistrationRoundRepository rounds;
    LecturerProfileRepository lecturers;
    CurrentUserService users;
    TopicAuditService audit;
    DepartmentReviewService service;
    Topic topic;
    RegistrationRound round;
    AppUser user;
    LecturerProfile profile;
    final Instant now = Instant.parse("2026-10-20T10:00:00Z");

    @BeforeEach void setup() {
        topics = mock(TopicRepository.class);
        rounds = mock(RegistrationRoundRepository.class);
        lecturers = mock(LecturerProfileRepository.class);
        users = mock(CurrentUserService.class);
        audit = mock(TopicAuditService.class);
        service = serviceAt(now);
        user = mock(AppUser.class);
        when(user.getId()).thenReturn(20L);
        when(user.getEnabled()).thenReturn(true);
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn("DEPARTMENT_HEAD");
        when(user.getRoles()).thenReturn(Set.of(role));
        when(users.getCurrentUser()).thenReturn(user);
        profile = mock(LecturerProfile.class);
        Department department = mock(Department.class);
        when(department.getId()).thenReturn(9L);
        when(profile.getDepartment()).thenReturn(department);
        when(lecturers.findById(20L)).thenReturn(Optional.of(profile));

        topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setRoundId(7L);
        topic.setDepartmentId(9L);
        topic.setStatus(TopicStatus.SUBMITTED);
        topic.setCode("B03-01");
        round = new RegistrationRound();
        ReflectionTestUtils.setField(round, "id", 7L);
        round.setStatus(RoundStatus.OPEN);
        round.setProposalStartsAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        round.setProposalEndsAt(LocalDateTime.ofInstant(now.plusSeconds(3600), ZoneOffset.UTC));
        when(topics.findById(10L)).thenReturn(Optional.of(topic));
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(rounds.findById(7L)).thenReturn(Optional.of(round));
        when(rounds.findByIdForUpdate(7L)).thenReturn(Optional.of(round));
        when(topics.saveAndFlush(topic)).thenReturn(topic);
    }

    DepartmentReviewService serviceAt(Instant time) {
        return new DepartmentReviewService(topics, rounds, lecturers, users,
                new RoundPolicy(), Clock.fixed(time, ZoneOffset.UTC), audit);
    }

    DepartmentReviewForm form(DepartmentReviewForm.Decision decision, String comment) {
        DepartmentReviewForm form = new DepartmentReviewForm();
        form.setDecision(decision);
        form.setComment(comment);
        return form;
    }

    @Test void approvalRecordsPreliminaryDecisionWithoutFinalApproval() {
        service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, "  Đồng ý  "));
        assertEquals(TopicStatus.SUBMITTED, topic.getStatus());
        assertEquals("APPROVED", topic.getDepartmentDecision());
        assertEquals("Đồng ý", topic.getDepartmentComment());
        assertEquals(20L, topic.getDepartmentReviewedBy());
        assertEquals(LocalDateTime.ofInstant(now, ZoneOffset.UTC), topic.getDepartmentReviewedAt());
        assertNull(topic.getApprovedBy());
        assertNull(topic.getApprovedAt());
        assertNull(topic.getPublishedAt());
        var order = inOrder(rounds, topics, audit);
        order.verify(topics).findByIdForUpdate(10L);
        order.verify(rounds).findByIdForUpdate(7L);
        order.verify(topics).saveAndFlush(topic);
        order.verify(audit).record(topic, 20L, "TOPIC_DEPARTMENT_APPROVED");
        order.verify(audit).snapshotReview(topic, 20L);
    }

    @Test void rejectionEnablesExistingRejectedResubmissionFlowAndKeepsReasonInDepartmentComment() {
        service.review(10L, form(DepartmentReviewForm.Decision.REJECTED, "  Cần bổ sung mục tiêu  "));
        assertEquals(TopicStatus.REJECTED, topic.getStatus());
        assertEquals("REJECTED", topic.getDepartmentDecision());
        assertEquals("Cần bổ sung mục tiêu", topic.getDepartmentComment());
        assertEquals("Cần bổ sung mục tiêu", topic.getRejectionReason());
        assertNull(topic.getApprovedBy());
        assertNull(topic.getPublishedAt());
        verify(audit).record(topic, 20L, "TOPIC_DEPARTMENT_REJECTED");
    }

    @Test void approvalAllowsMissingComment() {
        service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null));
        assertNull(topic.getDepartmentComment());
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t\n"})
    void rejectionRequiresReason(String comment) {
        assertThrows(BusinessException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.REJECTED, comment)));
        verify(topics, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void invalidFormCannotWrite() {
        assertThrows(BusinessException.class, () -> service.review(10L, null));
        assertThrows(BusinessException.class, () -> service.review(10L, form(null, "note")));
        assertThrows(BusinessException.class, () -> service.review(10L,
                form(DepartmentReviewForm.Decision.APPROVED, "x".repeat(4001))));
        verify(topics, never()).saveAndFlush(any());
    }

    @ParameterizedTest @EnumSource(value = TopicStatus.class, names = {"DRAFT", "APPROVED", "REJECTED", "PUBLISHED"})
    void onlySubmittedTopicsCanBeReviewed(TopicStatus status) {
        topic.setStatus(status);
        assertThrows(BusinessException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(audit);
        verify(topics, never()).saveAndFlush(any());
    }

    @Test void duplicateReviewIsRejectedEvenIfStillSubmitted() {
        topic.setDepartmentDecision("APPROVED");
        assertThrows(BusinessException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.REJECTED, "Changed")));
        verifyNoInteractions(audit);
    }

    @ParameterizedTest @CsvSource({"-1,false", "0,true", "3599,true", "3600,false"})
    void enforcesProposalWindowWithInclusiveStartExclusiveEnd(long offset, boolean allowed) {
        DepartmentReviewService timed = serviceAt(now.plusSeconds(offset));
        assertEquals(allowed, timed.getReviewPage(10L).canReview());
        if (allowed) {
            assertDoesNotThrow(() -> timed.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
        } else {
            assertThrows(BusinessException.class,
                    () -> timed.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
            verifyNoInteractions(audit);
        }
    }

    @ParameterizedTest @EnumSource(value = RoundStatus.class, names = {"DRAFT", "CLOSED"})
    void nonOpenRoundCannotBeReviewed(RoundStatus status) {
        round.setStatus(status);
        assertFalse(service.getReviewPage(10L).canReview());
        assertThrows(BusinessException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
    }

    @Test void departmentCannotBeProvidedByCallerAndCrossDepartmentAccessIsDenied() {
        topic.setDepartmentId(99L);
        assertThrows(AccessDeniedException.class, () -> service.getReviewPage(10L));
        assertThrows(AccessDeniedException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
        verify(rounds, never()).findByIdForUpdate(any());
        verifyNoInteractions(audit);
    }

    @Test void scopeAndStateAreCheckedAgainAfterLocking() {
        Topic changed = new Topic();
        changed.setDepartmentId(99L);
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(changed));
        assertThrows(AccessDeniedException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(audit);
    }

    @ParameterizedTest @NullSource @ValueSource(longs = {0, -1})
    void invalidIdCannotReadOrReview(Long id) {
        assertThrows(BusinessException.class, () -> service.getReviewPage(id));
        assertThrows(BusinessException.class,
                () -> service.review(id, form(DepartmentReviewForm.Decision.APPROVED, null)));
    }

    @Test void missingTopicOrRoundCannotWrite() {
        when(topics.findById(10L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.getReviewPage(10L));
        when(topics.findById(10L)).thenReturn(Optional.of(topic));
        when(rounds.findByIdForUpdate(7L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class,
                () -> service.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(audit);
    }

    @Test void disabledWrongRoleOrMissingProfileCannotAccessQueue() {
        when(user.getEnabled()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.getPendingTopics(0));
        when(user.getEnabled()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of());
        assertThrows(AccessDeniedException.class, () -> service.getPendingTopics(0));
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn("DEPARTMENT_HEAD");
        when(user.getRoles()).thenReturn(Set.of(role));
        when(lecturers.findById(20L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> service.getPendingTopics(0));
        when(lecturers.findById(20L)).thenReturn(Optional.of(profile));
        when(profile.getDepartment()).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> service.getPendingTopics(0));
        verifyNoInteractions(topics);
    }

    @Test void queueAlwaysUsesCurrentDepartmentAndDatabasePagination() {
        PageRequest pageable = PageRequest.of(2, 10, Sort.by("id").ascending());
        Page<Topic> expected = new PageImpl<>(List.of(topic), pageable, 21);
        when(topics.findByDepartmentIdAndStatusAndDepartmentDecisionIsNull(9L, TopicStatus.SUBMITTED, pageable))
                .thenReturn(expected);
        assertSame(expected, service.getPendingTopics(2));
        assertThrows(BusinessException.class, () -> service.getPendingTopics(-1));
    }

    @Test void auditFailureCausesSpringTransactionRollback() {
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(manager.getTransaction(any())).thenReturn(status);
        doThrow(new IllegalStateException("audit unavailable")).when(audit).record(any(), any(), any());
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        ProxyFactory factory = new ProxyFactory(service);
        factory.addAdvice(interceptor);
        DepartmentReviewService proxy = (DepartmentReviewService) factory.getProxy();
        assertThrows(IllegalStateException.class,
                () -> proxy.review(10L, form(DepartmentReviewForm.Decision.APPROVED, null)));
        verify(manager).rollback(status);
        verify(manager, never()).commit(any());
    }
}