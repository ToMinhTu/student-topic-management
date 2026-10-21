package com.example.student_topic_management.b04;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.entity.*;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.*;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.repository.*;
import com.example.student_topic_management.service.SupervisorService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.AopTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.example.student_topic_management.b04.B04Fixtures.*;

@SpringJUnitConfig(SupervisorServiceB04Test.Config.class)
class SupervisorServiceB04Test {
    @Configuration
    @EnableMethodSecurity
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Config {
        @Bean TopicRepository topics() { return mock(TopicRepository.class); }
        @Bean TopicSupervisorRepository supervisors() { return mock(TopicSupervisorRepository.class); }
        @Bean LecturerProfileRepository lecturers() { return mock(LecturerProfileRepository.class); }
        @Bean CurrentUserService currentUser() { return mock(CurrentUserService.class); }
        @Bean RecordingTransactions transactionManager() { return new RecordingTransactions(); }
        @Bean SupervisorService supervisorService(TopicRepository topics, TopicSupervisorRepository supervisors,
                LecturerProfileRepository lecturers, CurrentUserService currentUser) {
            return new SupervisorService(topics, supervisors, lecturers, currentUser);
        }
    }

    // Tests real Spring transaction interception; no database or persistence engine is simulated.
    static class RecordingTransactions extends AbstractPlatformTransactionManager {
        static class State implements SmartTransactionObject {
            boolean active;
            boolean rollbackOnly;
            public boolean isRollbackOnly() { return rollbackOnly; }
            public void flush() {}
        }
        private final ThreadLocal<State> current = ThreadLocal.withInitial(State::new);
        int begins, commits, rollbacks;
        boolean lastReadOnly;
        void resetCounts() { begins = commits = rollbacks = 0; }
        @Override protected Object doGetTransaction() { return current.get(); }
        @Override protected boolean isExistingTransaction(Object value) { return ((State) value).active; }
        @Override protected void doBegin(Object value, TransactionDefinition definition) {
            ((State) value).active = true;
            lastReadOnly = definition.isReadOnly();
            begins++;
        }
        @Override protected void doCommit(DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(DefaultTransactionStatus status) { rollbacks++; }
        @Override protected void doSetRollbackOnly(DefaultTransactionStatus status) {
            ((State) status.getTransaction()).rollbackOnly = true;
        }
        @Override protected void doCleanupAfterCompletion(Object value) { current.remove(); }
    }

    @Autowired ApplicationContext context;
    @Autowired SupervisorService service;
    @Autowired RecordingTransactions transactions;
    private TopicRepository topics;
    private TopicSupervisorRepository supervisors;
    private LecturerProfileRepository lecturers;
    private CurrentUserService current;
    private Topic topic;
    private LecturerProfile lecturer;
    private AppUser dean;

    @BeforeEach void setup() {
        topics = AopTestUtils.getUltimateTargetObject(context.getBean(TopicRepository.class));
        supervisors = AopTestUtils.getUltimateTargetObject(context.getBean(TopicSupervisorRepository.class));
        lecturers = AopTestUtils.getUltimateTargetObject(context.getBean(LecturerProfileRepository.class));
        current = AopTestUtils.getUltimateTargetObject(context.getBean(CurrentUserService.class));
        reset(topics, supervisors, lecturers, current);
        transactions.resetCounts();
        authenticate("DEAN");
        topic = topic(TopicStatus.DRAFT);
        dean = user(1L, "Dean", true, "DEAN");
        lecturer = lecturer(20L, "Giảng viên", true, "LECTURER");
        when(current.getCurrentUser()).thenReturn(dean);
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(lecturers.findById(20L)).thenReturn(Optional.of(lecturer));
        when(supervisors.saveAndFlush(any(TopicSupervisor.class))).thenAnswer(call -> call.getArgument(0));
    }

    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "caller", "unused", AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }

    @ParameterizedTest @ValueSource(ints = {1, 2})
    void assignsEitherSlotAndLocksBeforeWriting(int slot) {
        TopicSupervisor saved = service.addSupervisor(10L, 20L, slot);
        assertEquals(new TopicSupervisorId(10L, 20L), saved.getId());
        assertEquals((byte) slot, saved.getSlotNo());
        InOrder order = inOrder(topics, supervisors);
        order.verify(topics).findByIdForUpdate(10L);
        order.verify(supervisors).countApprovedRegistrationsByTopicId(10L);
        order.verify(supervisors).saveAndFlush(any(TopicSupervisor.class));
        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);
    }

    @Test void rejectsDuplicateLecturer() {
        when(supervisors.existsByIdTopicIdAndIdLecturerId(10L, 20L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        verify(supervisors, never()).saveAndFlush(any());
    }

    @Test void rejectsDuplicateSlot() {
        when(supervisors.existsByIdTopicIdAndSlotNo(10L, (byte) 1)).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        verify(supervisors, never()).saveAndFlush(any());
    }

    @Test void rejectsThirdSupervisor() {
        when(supervisors.countByIdTopicId(10L)).thenReturn(2L);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        verify(supervisors, never()).saveAndFlush(any());
    }

    @ParameterizedTest @NullSource @ValueSource(ints = {-1, 0, 3, 256})
    void rejectsInvalidSlot(Integer slot) {
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, slot));
        verifyNoInteractions(topics);
    }

    @ParameterizedTest @NullSource @ValueSource(longs = {-1L, 0L})
    void rejectsInvalidIds(Long id) {
        assertThrows(BusinessException.class, () -> service.addSupervisor(id, 20L, 1));
        assertThrows(BusinessException.class, () -> service.removeSupervisor(10L, id));
        assertThrows(BusinessException.class, () -> service.isSupervisor(id, 20L));
    }

    @Test void rejectsMissingTopicOrProfile() {
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        when(topics.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(lecturers.findById(20L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        verify(supervisors, never()).saveAndFlush(any());
    }

    @Test void rejectsDisabledLecturerOrMissingUser() {
        lecturer.getUser().setEnabled(false);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        lecturer.setUser(null);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
    }

    @Test void rejectsMissingLecturerRole() {
        lecturer.getUser().setRoles(java.util.Set.of(new Role(2L, "DEAN")));
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
    }

    @Test void acceptsLecturerWithMultipleRoles() {
        lecturer = lecturer(20L, "Dean lecturer", true, "LECTURER", "DEAN", "DEPARTMENT_HEAD");
        when(lecturers.findById(20L)).thenReturn(Optional.of(lecturer));
        assertNotNull(service.addSupervisor(10L, 20L, 2));
    }

    @ParameterizedTest @EnumSource(value = TopicStatus.class, names = "DRAFT", mode = EnumSource.Mode.EXCLUDE)
    void blocksAddAndRemoveOutsideDraft(TopicStatus status) {
        topic.setStatus(status);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        assertThrows(BusinessException.class, () -> service.removeSupervisor(10L, 20L));
        verify(supervisors, never()).saveAndFlush(any());
        verify(supervisors, never()).delete(any());
    }

    @Test void blocksAddAndRemoveWithApprovedRegistration() {
        when(supervisors.countApprovedRegistrationsByTopicId(10L)).thenReturn(1L);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        assertThrows(BusinessException.class, () -> service.removeSupervisor(10L, 20L));
        verify(supervisors, never()).delete(any());
        verify(supervisors, never()).saveAndFlush(any());
    }

    @ParameterizedTest @ValueSource(strings = {"ADMIN", "LECTURER", "STUDENT", "DEPARTMENT_HEAD"})
    void methodSecurityRejectsNonDeanEvenWhenCalledDirectly(String role) {
        authenticate(role);
        assertThrows(AccessDeniedException.class, () -> service.addSupervisor(10L, 20L, 1));
        assertThrows(AccessDeniedException.class, () -> service.removeSupervisor(10L, 20L));
        verifyNoInteractions(topics, supervisors, lecturers, current);
    }

    @Test void methodSecurityRejectsUnauthenticatedCaller() {
        SecurityContextHolder.clearContext();
        assertThrows(org.springframework.security.core.AuthenticationException.class,
                () -> service.addSupervisor(10L, 20L, 1));
        assertThrows(org.springframework.security.core.AuthenticationException.class,
                () -> service.removeSupervisor(10L, 20L));
    }

    @Test void rechecksDeanAccountEnabledAndRole() {
        dean.setEnabled(false);
        assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        dean.setEnabled(true);
        dean.setRoles(java.util.Set.of());
        assertThrows(BusinessException.class, () -> service.removeSupervisor(10L, 20L));
        verifyNoInteractions(topics);
    }

    @Test void removesSpecificAssignmentAfterLocking() {
        TopicSupervisor assigned = new TopicSupervisor(10L, 20L, (byte) 2);
        when(supervisors.findById(new TopicSupervisorId(10L, 20L))).thenReturn(Optional.of(assigned));
        service.removeSupervisor(10L, 20L);
        InOrder order = inOrder(topics, supervisors);
        order.verify(topics).findByIdForUpdate(10L);
        order.verify(supervisors).delete(assigned);
        order.verify(supervisors).flush();
        assertEquals(1, transactions.commits);
    }

    @Test void removingMissingAssignmentFails() {
        assertThrows(BusinessException.class, () -> service.removeSupervisor(10L, 20L));
        verify(supervisors, never()).delete(any());
    }

    @ParameterizedTest @ValueSource(booleans = {true, false})
    void supervisorLookupOnlyChecksPairWithoutCurrentUserOrProfile(boolean exists) {
        SecurityContextHolder.clearContext();
        when(supervisors.existsByIdTopicIdAndIdLecturerId(10L, 20L)).thenReturn(exists);
        assertEquals(exists, service.isSupervisor(10L, 20L));
        verifyNoInteractions(current, lecturers, topics);
        assertTrue(transactions.lastReadOnly);
    }

    @ParameterizedTest @ValueSource(longs = {1, 2})
    void approvalAcceptsOneOrTwoInSubmittedStateAndJoinsB03Transaction(long count) {
        topic.setStatus(TopicStatus.SUBMITTED);
        when(supervisors.countByIdTopicId(10L)).thenReturn(count);
        new TransactionTemplate(transactions).executeWithoutResult(status -> service.requireSupervisorCountForApproval(10L));
        assertEquals(1, transactions.begins);
        assertEquals(1, transactions.commits);
        verify(topics).findByIdForUpdate(10L);
    }

    @ParameterizedTest @ValueSource(longs = {0, 3})
    void approvalRejectsInvalidCount(long count) {
        when(supervisors.countByIdTopicId(10L)).thenReturn(count);
        assertThrows(BusinessException.class, () -> service.requireSupervisorCountForApproval(10L));
    }

    @Test void readListIsOrderedAndReadOnly() {
        when(topics.existsById(10L)).thenReturn(true);
        List<TopicSupervisor> ordered = List.of(new TopicSupervisor(10L, 20L, (byte) 1));
        when(supervisors.findByIdTopicIdOrderBySlotNoAsc(10L)).thenReturn(ordered);
        assertEquals(ordered, service.getSupervisors(10L));
        assertTrue(transactions.lastReadOnly);
    }

    @Test void writeFailureRollsBackThroughSpringProxyAndHidesSql() {
        when(supervisors.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("SECRET_SQL"));
        BusinessException failure = assertThrows(BusinessException.class, () -> service.addSupervisor(10L, 20L, 1));
        assertFalse(failure.getMessage().contains("SECRET_SQL"));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }

    @Test void removeFlushFailureRollsBackThroughSpringProxy() {
        when(supervisors.findById(new TopicSupervisorId(10L, 20L)))
                .thenReturn(Optional.of(new TopicSupervisor(10L, 20L, (byte) 1)));
        doThrow(new DataIntegrityViolationException("SECRET_SQL")).when(supervisors).flush();
        assertThrows(BusinessException.class, () -> service.removeSupervisor(10L, 20L));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }
}
