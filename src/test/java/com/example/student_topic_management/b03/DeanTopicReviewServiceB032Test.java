package com.example.student_topic_management.b03;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.DeanTopicReviewForm;
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
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeanTopicReviewServiceB032Test {
    TopicRepository topics;
    RegistrationRoundRepository rounds;
    TopicSupervisorRepository assignments;
    CurrentUserService users;
    TopicAuditService audit;
    SupervisorService supervisors;
    DeanTopicReviewService service;
    AppUser dean;
    Topic topic;
    RegistrationRound round;
    final Instant now = Instant.parse("2026-10-20T10:00:00Z");

    @BeforeEach void setup() {
        topics = mock(TopicRepository.class);
        rounds = mock(RegistrationRoundRepository.class);
        assignments = mock(TopicSupervisorRepository.class);
        users = mock(CurrentUserService.class);
        audit = mock(TopicAuditService.class);
        supervisors = spy(new SupervisorService(topics, assignments, mock(LecturerProfileRepository.class), users));
        service = serviceAt(now, supervisors);
        dean = mock(AppUser.class);
        when(dean.getId()).thenReturn(20L);
        when(dean.getEnabled()).thenReturn(true);
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn("DEAN");
        when(dean.getRoles()).thenReturn(Set.of(role));
        when(users.getCurrentUser()).thenReturn(dean);
        topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setRoundId(7L);
        topic.setDepartmentId(9L);
        topic.setStatus(TopicStatus.SUBMITTED);
        topic.setDepartmentDecision("APPROVED");
        topic.setDepartmentReviewedBy(30L);
        topic.setDepartmentReviewedAt(LocalDateTime.ofInstant(now.minusSeconds(60), ZoneOffset.UTC));
        topic.setDepartmentComment("Đồng ý sơ duyệt");
        round = new RegistrationRound();
        ReflectionTestUtils.setField(round, "id", 7L);
        round.setStatus(RoundStatus.OPEN);
        round.setProposalStartsAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        round.setProposalEndsAt(LocalDateTime.ofInstant(now.plusSeconds(3600), ZoneOffset.UTC));
        when(topics.findById(10L)).thenReturn(Optional.of(topic));
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(rounds.findById(7L)).thenReturn(Optional.of(round));
        when(rounds.findByIdForUpdate(7L)).thenReturn(Optional.of(round));
        when(assignments.countByIdTopicId(10L)).thenReturn(1L);
        when(topics.saveAndFlush(topic)).thenReturn(topic);
    }

    DeanTopicReviewService serviceAt(Instant time, SupervisorService supervisorService) {
        return new DeanTopicReviewService(topics, rounds, users, supervisorService, audit,
                new RoundPolicy(), Clock.fixed(time, ZoneOffset.UTC));
    }

    DeanTopicReviewForm form(DeanTopicReviewForm.Decision decision, String reason) {
        DeanTopicReviewForm form = new DeanTopicReviewForm();
        form.setDecision(decision);
        form.setRejectionReason(reason);
        return form;
    }

    @ParameterizedTest @ValueSource(longs = {1, 2})
    void approvalChecksActualSupervisorServiceAndRecordsFinalDecisionWithoutPublishing(long count) {
        when(assignments.countByIdTopicId(10L)).thenReturn(count);
        service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null));
        assertEquals(TopicStatus.APPROVED, topic.getStatus());
        assertEquals(20L, topic.getApprovedBy());
        assertEquals(LocalDateTime.ofInstant(now, ZoneOffset.UTC), topic.getApprovedAt());
        assertNull(topic.getPublishedAt());
        assertNull(topic.getRejectionReason());
        assertEquals("APPROVED", topic.getDepartmentDecision());
        assertEquals(30L, topic.getDepartmentReviewedBy());
        var order = inOrder(topics, rounds, supervisors, assignments, audit);
        order.verify(topics).findByIdForUpdate(10L);
        order.verify(rounds).findByIdForUpdate(7L);
        order.verify(supervisors).requireSupervisorCountForApproval(10L);
        order.verify(topics).findByIdForUpdate(10L);
        order.verify(assignments).countByIdTopicId(10L);
        order.verify(topics).saveAndFlush(topic);
        order.verify(audit).record(topic, 20L, "TOPIC_DEAN_APPROVED");
        order.verify(audit).snapshotReview(topic, 20L);
    }

    @ParameterizedTest @ValueSource(longs = {0, 3})
    void zeroOrTooManySupervisorsCannotApprove(long count) {
        when(assignments.countByIdTopicId(10L)).thenReturn(count);
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        assertEquals(TopicStatus.SUBMITTED, topic.getStatus());
        assertNull(topic.getApprovedBy());
        verify(topics, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void rejectionRequiresNoSupervisorAndPreservesDepartmentDecision() {
        service.review(10L, form(DeanTopicReviewForm.Decision.REJECTED, "  Cần sửa phạm vi  "));
        assertEquals(TopicStatus.REJECTED, topic.getStatus());
        assertEquals("Cần sửa phạm vi", topic.getRejectionReason());
        assertEquals("APPROVED", topic.getDepartmentDecision());
        assertEquals("Đồng ý sơ duyệt", topic.getDepartmentComment());
        assertNull(topic.getApprovedBy());
        assertNull(topic.getApprovedAt());
        assertNull(topic.getPublishedAt());
        verifyNoInteractions(assignments);
        verify(audit).record(topic, 20L, "TOPIC_DEAN_REJECTED");
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t\n"})
    void rejectionRequiresNonblankReason(String reason) {
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.REJECTED, reason)));
        verify(topics, never()).findByIdForUpdate(any());
        verifyNoInteractions(audit);
    }

    @Test void invalidFormDoesNotAcquireLocks() {
        assertThrows(BusinessException.class, () -> service.review(10L, null));
        assertThrows(BusinessException.class, () -> service.review(10L, form(null, "Reason")));
        assertThrows(BusinessException.class, () -> service.review(10L,
                form(DeanTopicReviewForm.Decision.REJECTED, "x".repeat(4001))));
        verify(topics, never()).findByIdForUpdate(any());
    }

    @ParameterizedTest @EnumSource(value = TopicStatus.class, names = {"DRAFT", "APPROVED", "REJECTED", "PUBLISHED"})
    void nonSubmittedTopicsCannotBeProcessed(TopicStatus state) {
        topic.setStatus(state);
        assertFalse(service.getReviewPage(10L).canReview());
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(assignments, audit);
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {"REJECTED"})
    void departmentMustHaveApproved(String decision) {
        topic.setDepartmentDecision(decision);
        assertFalse(service.getReviewPage(10L).canReview());
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(assignments, audit);
    }

    @ParameterizedTest @ValueSource(strings = {"approvedBy", "approvedAt", "rejectionReason", "publishedAt"})
    void existingFinalDecisionMetadataBlocksEvenMalformedSubmittedRows(String field) {
        Object value = field.equals("approvedBy") ? 99L : field.equals("rejectionReason") ? "Already rejected"
                : LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        ReflectionTestUtils.setField(topic, field, value);
        assertFalse(service.getReviewPage(10L).canReview());
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.REJECTED, "Reason")));
        verifyNoInteractions(audit);
    }

    @Test void secondAttemptRechecksLockedStateAndCannotOverwriteApproval() {
        service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null));
        clearInvocations(audit, assignments);
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.REJECTED, "Replace")));
        assertEquals(TopicStatus.APPROVED, topic.getStatus());
        assertNull(topic.getRejectionReason());
        verifyNoInteractions(assignments, audit);
    }

    @Test void lockedValueWinsOverStaleUnlockedValue() {
        Topic stale = new Topic();
        stale.setStatus(TopicStatus.SUBMITTED);
        stale.setDepartmentDecision("APPROVED");
        when(topics.findById(10L)).thenReturn(Optional.of(stale));
        topic.setStatus(TopicStatus.REJECTED);
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        verify(topics, never()).findById(any());
        verifyNoInteractions(audit);
    }

    @ParameterizedTest @CsvSource({"-1,false", "0,true", "3599,true", "3600,false"})
    void openWindowUsesInclusiveStartExclusiveEnd(long offset, boolean allowed) {
        DeanTopicReviewService timed = serviceAt(now.plusSeconds(offset), supervisors);
        assertEquals(allowed, timed.getReviewPage(10L).canReview());
        if (allowed) assertDoesNotThrow(() -> timed.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        else {
            assertThrows(BusinessException.class, () -> timed.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
            verifyNoInteractions(assignments, audit);
        }
    }

    @ParameterizedTest @EnumSource(value = RoundStatus.class, names = {"DRAFT", "CLOSED"})
    void nonOpenRoundsBlockBothDecisions(RoundStatus state) {
        round.setStatus(state);
        assertFalse(service.getReviewPage(10L).canReview());
        for (var decision : DeanTopicReviewForm.Decision.values()) {
            assertThrows(BusinessException.class, () -> service.review(10L, form(decision, "Reason")));
        }
        verifyNoInteractions(assignments, audit);
    }

    @ParameterizedTest @NullSource @ValueSource(longs = {0, -1})
    void invalidIdsAreRejected(Long id) {
        assertThrows(BusinessException.class, () -> service.getReviewPage(id));
        assertThrows(BusinessException.class, () -> service.review(id, form(DeanTopicReviewForm.Decision.APPROVED, null)));
    }

    @Test void missingTopicOrRoundCannotWrite() {
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(rounds.findByIdForUpdate(7L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(audit);
    }

    @Test void inactiveOrWrongRoleCannotReadOrWrite() {
        when(dean.getEnabled()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.getPendingTopics(0));
        assertThrows(AccessDeniedException.class, () -> service.getReviewPage(10L));
        assertThrows(AccessDeniedException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        when(dean.getEnabled()).thenReturn(true);
        when(dean.getRoles()).thenReturn(Set.of());
        assertThrows(AccessDeniedException.class, () -> service.getPendingTopics(0));
        assertThrows(AccessDeniedException.class, () -> service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        verifyNoInteractions(topics, rounds, assignments, audit);
    }

    @ParameterizedTest @ValueSource(longs = {9, 99})
    void singleFacultyDeanScopeCoversEveryDepartment(long departmentId) {
        topic.setDepartmentId(departmentId);
        assertTrue(service.getReviewPage(10L).canReview());
        service.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null));
        assertEquals(TopicStatus.APPROVED, topic.getStatus());
    }

    @Test void queuePaginatesAndDeclaresOnlyEligibleRows() throws Exception {
        PageRequest request = PageRequest.of(1, 10, Sort.by("id").ascending());
        Page<Topic> expected = new PageImpl<>(List.of(topic), request, 11);
        when(topics.findPendingDeanReview(request)).thenReturn(expected);
        assertSame(expected, service.getPendingTopics(1));
        assertThrows(BusinessException.class, () -> service.getPendingTopics(-1));
        // Declaration check only, not a JPQL execution test.
        Query query = TopicRepository.class.getMethod("findPendingDeanReview", Pageable.class).getAnnotation(Query.class);
        for (String predicate : List.of("TopicStatus.SUBMITTED", "t.departmentDecision = 'APPROVED'",
                "t.approvedBy IS NULL", "t.approvedAt IS NULL", "t.rejectionReason IS NULL", "t.publishedAt IS NULL")) {
            assertTrue(query.value().contains(predicate));
        }
    }

    @Test void supervisorAndAuditShareOneRequiredTransaction() {
        RecordingTransactions transactions = new RecordingTransactions();
        SupervisorService supervisorProxy = transactional(supervisors, transactions);
        DeanTopicReviewService proxy = transactional(serviceAt(now, supervisorProxy), transactions);
        when(assignments.countByIdTopicId(10L)).thenAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            return 1L;
        });
        doAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            assertEquals(TopicStatus.APPROVED, topic.getStatus());
            return null;
        }).when(audit).record(any(), any(), any());
        proxy.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null));
        assertEquals(1, transactions.begins);
        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);
    }

    @ParameterizedTest @ValueSource(strings = {"supervisor", "save", "audit", "snapshot"})
    void failuresRollBackTheOuterTransaction(String stage) {
        RecordingTransactions transactions = new RecordingTransactions();
        SupervisorService supervisorProxy = transactional(supervisors, transactions);
        DeanTopicReviewService proxy = transactional(serviceAt(now, supervisorProxy), transactions);
        if (stage.equals("supervisor")) when(assignments.countByIdTopicId(10L)).thenReturn(0L);
        if (stage.equals("save")) when(topics.saveAndFlush(topic)).thenThrow(new IllegalStateException("save failed"));
        if (stage.equals("audit")) doThrow(new IllegalStateException("audit failed")).when(audit).record(any(), any(), any());
        if (stage.equals("snapshot")) doThrow(new IllegalStateException("snapshot failed")).when(audit).snapshotReview(any(), any());
        assertThrows(RuntimeException.class, () -> proxy.review(10L, form(DeanTopicReviewForm.Decision.APPROVED, null)));
        assertEquals(1, transactions.begins);
        assertEquals(0, transactions.commits);
        assertEquals(1, transactions.rollbacks);
    }

    @Test void readsUseReadOnlyTransaction() {
        RecordingTransactions transactions = new RecordingTransactions();
        DeanTopicReviewService proxy = transactional(service, transactions);
        when(topics.findPendingDeanReview(any())).thenAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            return Page.empty();
        });
        proxy.getPendingTopics(0);
        assertEquals(1, transactions.commits);
    }

    @SuppressWarnings("unchecked")
    <T> T transactional(T target, PlatformTransactionManager manager) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        ProxyFactory proxy = new ProxyFactory(target);
        proxy.addAdvice(interceptor);
        return (T) proxy.getProxy();
    }

    // Exercises Spring propagation/rollback only; no database resources or physical rollback are simulated.
    static class RecordingTransactions extends AbstractPlatformTransactionManager {
        static class State { boolean active; }
        final ThreadLocal<State> current = new ThreadLocal<>();
        int begins, commits, rollbacks;
        @Override protected Object doGetTransaction() {
            return current.get() == null ? new State() : current.get();
        }
        @Override protected boolean isExistingTransaction(Object transaction) { return ((State) transaction).active; }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) {
            ((State) transaction).active = true;
            current.set((State) transaction);
            begins++;
        }
        @Override protected void doCommit(DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(DefaultTransactionStatus status) { rollbacks++; }
        @Override protected void doSetRollbackOnly(DefaultTransactionStatus status) { }
        @Override protected void doCleanupAfterCompletion(Object transaction) {
            ((State) transaction).active = false;
            current.remove();
        }
    }
}