package com.example.student_topic_management.service;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.dto.TopicProposalForm;
import com.example.student_topic_management.entity.*;
import com.example.student_topic_management.enums.*;
import com.example.student_topic_management.identity.entity.*;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.policy.RoundPolicy;
import com.example.student_topic_management.repository.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopicProposalServiceTest {
    private final TopicRepository topics = mock(TopicRepository.class);
    private final RegistrationRoundRepository rounds = mock(RegistrationRoundRepository.class);
    private final DepartmentRepository departments = mock(DepartmentRepository.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final Instant now = Instant.parse("2026-10-22T05:00:00Z");
    private TopicProposalService service;
    private AppUser user;
    private RegistrationRound round;
    private Topic topic;

    @BeforeEach
    void setup() {
        service = new TopicProposalService(topics, rounds, departments, current,
                new RoundPolicy(), Clock.fixed(now, ZoneOffset.UTC));
        user = new AppUser();
        user.setId(7L);
        user.setRoles(Set.of(new Role(1L, "LECTURER")));
        when(current.getCurrentUser()).thenReturn(user);
        when(departments.existsById(2L)).thenReturn(true);
        round = new RegistrationRound();
        round.setStatus(RoundStatus.OPEN);
        round.setProposalStartsAt(LocalDateTime.ofInstant(now.minusSeconds(60), ZoneOffset.UTC));
        round.setProposalEndsAt(LocalDateTime.ofInstant(now.plusSeconds(60), ZoneOffset.UTC));
        when(rounds.findByIdForUpdate(1L)).thenReturn(Optional.of(round));
        topic = new Topic();
        topic.setRoundId(1L);
        topic.setDepartmentId(2L);
        topic.setProposedBy(7L);
        topic.setCode("OLD");
        topic.setTitle("Original");
        when(topics.findByIdForUpdate(9L)).thenReturn(Optional.of(topic));
        when(topics.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
    }

    private TopicProposalForm form() {
        TopicProposalForm form = new TopicProposalForm();
        form.setRoundId(1L);
        form.setDepartmentId(2L);
        form.setCode(" CODE ");
        form.setTitle(" New title ");
        form.setDescription(" Description ");
        return form;
    }

    @ParameterizedTest
    @ValueSource(strings = {"LECTURER", "STUDENT"})
    void createsDraftForAllowedRolesUsingCurrentUser(String role) {
        user.setRoles(Set.of(new Role(1L, role)));
        Topic saved = service.createDraft(form());
        assertEquals(7L, saved.getProposedBy());
        assertEquals(TopicStatus.DRAFT, saved.getStatus());
        assertEquals("CODE", saved.getCode());
        assertEquals("New title", saved.getTitle());
        verify(rounds).findByIdForUpdate(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "DEAN", "DEPARTMENT_HEAD"})
    void rejectsOtherRoles(String role) {
        user.setRoles(Set.of(new Role(1L, role)));
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
        verifyNoInteractions(topics);
    }

    @Test void rejectsDisabledUser() {
        user.setEnabled(false);
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
    }

    @Test void propagatesUnauthenticatedError() {
        when(current.getCurrentUser()).thenThrow(new BusinessException("Chưa đăng nhập"));
        assertThrows(BusinessException.class, () -> service.getMyTopics(PageRequest.of(0, 10)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DRAFT", "CLOSED"})
    void rejectsNonOpenRounds(String status) {
        round.setStatus(RoundStatus.valueOf(status));
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
        verify(topics, never()).saveAndFlush(any());
    }

    @Test void rejectsBeforeStart() {
        round.setProposalStartsAt(LocalDateTime.ofInstant(now.plusSeconds(1), ZoneOffset.UTC));
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
    }

    @Test void acceptsExactStart() {
        round.setProposalStartsAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        assertNotNull(service.createDraft(form()));
    }

    @Test void rejectsExactEndForCreateUpdateAndSubmit() {
        round.setProposalEndsAt(LocalDateTime.ofInstant(now, ZoneOffset.UTC));
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
        assertThrows(BusinessException.class, () -> service.updateDraft(9L, form()));
        assertThrows(BusinessException.class, () -> service.submitProposal(9L));
        assertEquals(TopicStatus.DRAFT, topic.getStatus());
        assertEquals("Original", topic.getTitle());
        verify(topics, never()).saveAndFlush(any());
    }

    @Test void rejectsMissingRoundAndDepartment() {
        when(rounds.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
        when(departments.existsById(2L)).thenReturn(false);
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
    }

    @Test void rejectsMalformedForms() {
        assertThrows(BusinessException.class, () -> service.createDraft(null));
        TopicProposalForm f = form();
        f.setTitle("  ");
        assertThrows(BusinessException.class, () -> service.createDraft(f));
        f.setTitle("x".repeat(256));
        assertThrows(BusinessException.class, () -> service.createDraft(f));
        f.setTitle("Valid");
        f.setRoundId(0L);
        assertThrows(BusinessException.class, () -> service.createDraft(f));
        f.setRoundId(1L);
        f.setDepartmentId(-1L);
        assertThrows(BusinessException.class, () -> service.createDraft(f));
    }

    @Test void rejectsTextExceedingMysqlTextByteLimit() {
        TopicProposalForm f = form();
        f.setDescription("界".repeat(21846));
        assertThrows(BusinessException.class, () -> service.createDraft(f));
        f.setDescription("Valid");
        f.setRequirements("界".repeat(21846));
        assertThrows(BusinessException.class, () -> service.createDraft(f));
    }

    @Test void rejectsDuplicateCodeOnCreateAndUpdate() {
        when(topics.existsByRoundIdAndCode(1L, "CODE")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.createDraft(form()));
        when(topics.existsByRoundIdAndCodeAndIdNot(1L, "CODE", 9L)).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.updateDraft(9L, form()));
        verify(topics, never()).saveAndFlush(any());
    }

    @Test void updatesOwnedDraftWithoutChangingOwner() {
        assertSame(topic, service.updateDraft(9L, form()));
        assertEquals("CODE", topic.getCode());
        assertEquals(7L, topic.getProposedBy());
        assertEquals(TopicStatus.DRAFT, topic.getStatus());
    }

    @Test void cannotChangeRound() {
        TopicProposalForm f = form();
        f.setRoundId(3L);
        assertThrows(BusinessException.class, () -> service.updateDraft(9L, f));
        verify(topics, never()).saveAndFlush(any());
    }

    @Test void rejectsOtherOwnerForEditAndSubmit() {
        topic.setProposedBy(8L);
        assertThrows(BusinessException.class, () -> service.updateDraft(9L, form()));
        assertThrows(BusinessException.class, () -> service.submitProposal(9L));
        verify(topics, never()).saveAndFlush(any());
    }

    @Test void submitsOnlyOnceAndPreventsFurtherEditing() {
        assertEquals(TopicStatus.SUBMITTED, service.submitProposal(9L).getStatus());
        assertThrows(BusinessException.class, () -> service.submitProposal(9L));
        assertThrows(BusinessException.class, () -> service.updateDraft(9L, form()));
        verify(topics, times(1)).saveAndFlush(any());
    }

    @Test void rejectsMissingTopic() {
        when(topics.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.updateDraft(9L, form()));
        assertThrows(BusinessException.class, () -> service.submitProposal(9L));
    }

    @Test void queriesOnlyCurrentUsersTopics() {
        Pageable page = PageRequest.of(0, 10);
        when(topics.findByProposedBy(7L, page)).thenReturn(Page.empty());
        assertTrue(service.getMyTopics(page).isEmpty());
        when(topics.findByIdAndProposedBy(9L, 7L)).thenReturn(Optional.of(topic));
        assertSame(topic, service.getMyTopicDetail(9L));
        when(topics.findByIdAndProposedBy(9L, 7L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.getMyTopicDetail(9L));
    }
}
