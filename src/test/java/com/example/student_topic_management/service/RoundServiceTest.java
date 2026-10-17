package com.example.student_topic_management.service;

import com.example.student_topic_management.dto.RoundForm;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.RoundStatus;
import com.example.student_topic_management.enums.RoundType;
import com.example.student_topic_management.repository.RegistrationRoundRepository;
import java.time.*;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoundServiceTest {
    private final RegistrationRoundRepository repository = mock(RegistrationRoundRepository.class);
    private final CurrentUserProvider user = mock(CurrentUserProvider.class);
    private final Instant now = Instant.parse("2030-01-10T00:00:00Z");
    private final RoundService service = new RoundService(repository, user,
            Clock.fixed(now, ZoneOffset.UTC));

    private RoundForm validForm(RoundType type) {
        RoundForm form = new RoundForm();
        form.setName("  Đợt học kỳ 1  ");
        form.setRoundType(type);
        form.setProposalStartsAt(LocalDateTime.of(2030, 1, 1, 8, 0));
        form.setProposalEndsAt(LocalDateTime.of(2030, 1, 2, 8, 0));
        form.setStudentStartsAt(form.getProposalEndsAt());
        form.setStudentEndsAt(LocalDateTime.of(2030, 1, 3, 8, 0));
        if (type == RoundType.SPECIALIZATION || type == RoundType.THESIS) {
            form.setReviewerDueAt(form.getStudentEndsAt().plusDays(1));
        }
        if (type == RoundType.THESIS) {
            form.setCouncilDate(LocalDate.of(2030, 1, 5));
        }
        return form;
    }

    private RegistrationRound round(RoundStatus status, Instant end) {
        RegistrationRound round = new RegistrationRound();
        round.setStatus(status);
        round.setStudentEndsAt(LocalDateTime.ofInstant(end, ZoneOffset.UTC));
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(round));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return round;
    }

    @ParameterizedTest
    @EnumSource(RoundType.class)
    void createsDraftAndConvertsVietnamTimeWithoutChangingInput(RoundType type) {
        when(user.requireDeanUserId()).thenReturn(42L);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RoundForm form = validForm(type);
        RegistrationRound saved = service.createRound(form);
        assertEquals(RoundStatus.DRAFT, saved.getStatus());
        assertEquals(42L, saved.getCreatedBy());
        assertEquals("Đợt học kỳ 1", saved.getName());
        assertEquals(form.getProposalStartsAt().minusHours(7), saved.getProposalStartsAt());
        RoundForm display = service.toDisplayForm(saved);
        assertEquals(form.getProposalStartsAt(), display.getProposalStartsAt());
        assertEquals(form.getStudentEndsAt(), display.getStudentEndsAt());
        assertEquals(form.getReviewerDueAt(), display.getReviewerDueAt());
        assertEquals(form.getCouncilDate(), display.getCouncilDate());
    }

    @Test
    void rejectsInvalidDatesAndGroupSizes() {
        RoundForm form = validForm(RoundType.COURSE);
        form.setProposalEndsAt(form.getProposalStartsAt());
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
        RoundForm overlapping = validForm(RoundType.COURSE);
        overlapping.setStudentStartsAt(overlapping.getProposalEndsAt().minusMinutes(1));
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(overlapping));
    }

    @Test
    void rejectsInvalidGroupAndOptionalDeadlines() {
        RoundForm form = validForm(RoundType.COURSE);
        form.setGroupMinSize(4);
        form.setGroupMaxSize(3);
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
        form.setGroupMinSize(3);
        form.setReviewerDueAt(form.getStudentEndsAt().plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
        form.setReviewerDueAt(null);
        form.setReportDueAt(form.getStudentEndsAt());
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
    }

    @ParameterizedTest
    @EnumSource(value = RoundType.class, names = {"SPECIALIZATION", "THESIS"})
    void reviewerMustBeAfterRegistration(RoundType type) {
        RoundForm form = validForm(type);
        form.setReviewerDueAt(null);
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
        form.setReviewerDueAt(form.getStudentEndsAt());
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
    }

    @Test
    void thesisRequiresCouncilDate() {
        RoundForm form = validForm(RoundType.THESIS);
        form.setCouncilDate(null);
        assertThrows(IllegalArgumentException.class, () -> service.validateRound(form));
    }

    @Test
    void opensOnlyBeforeRegistrationEnds() {
        round(RoundStatus.DRAFT, now.plusSeconds(1));
        assertEquals(RoundStatus.OPEN, service.openRound(1L).getStatus());
        round(RoundStatus.DRAFT, now);
        assertThrows(IllegalStateException.class, () -> service.openRound(1L));
    }

    @Test
    void closesAtDeadlineButNotBefore() {
        round(RoundStatus.OPEN, now.plusSeconds(1));
        assertThrows(IllegalStateException.class, () -> service.closeRound(1L));
        round(RoundStatus.OPEN, now);
        assertEquals(RoundStatus.CLOSED, service.closeRound(1L).getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = RoundStatus.class, names = {"OPEN", "CLOSED"})
    void rejectsEditingDeletingAndOpeningNonDraft(RoundStatus status) {
        round(status, now.plusSeconds(1));
        assertThrows(IllegalStateException.class, () -> service.updateRound(1L, validForm(RoundType.COURSE)));
        assertThrows(IllegalStateException.class, () -> service.deleteDraftRound(1L));
        assertThrows(IllegalStateException.class, () -> service.openRound(1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void deletesDraftAndTranslatesForeignKeyConflict() {
        RegistrationRound draft = round(RoundStatus.DRAFT, now);
        service.deleteDraftRound(1L);
        verify(repository).delete(draft);
        doThrow(new DataIntegrityViolationException("FK")).when(repository).flush();
        assertThrows(IllegalStateException.class, () -> service.deleteDraftRound(1L));
    }

    @Test
    void updatesDraftAndLoadsEditForm() {
        RegistrationRound draft = round(RoundStatus.DRAFT, now);
        service.updateRound(1L, validForm(RoundType.THESIS));
        when(repository.findById(1L)).thenReturn(Optional.of(draft));
        assertEquals(validForm(RoundType.THESIS).getStudentEndsAt(), service.getEditForm(1L).getStudentEndsAt());
    }

    @Test
    void rejectsMissingIdsAndNegativePages() {
        assertThrows(IllegalArgumentException.class, () -> service.getAllRounds(-1));
        assertThrows(IllegalArgumentException.class, () -> service.getRoundById(0L));
        assertThrows(NoSuchElementException.class, () -> service.getRoundById(1L));
        assertThrows(NoSuchElementException.class, () -> service.openRound(1L));
    }

    @Test
    void authorizationRunsBeforeReadingOrWritingData() {
        when(user.requireDeanUserId()).thenThrow(new AccessDeniedException("Forbidden"));
        assertThrows(AccessDeniedException.class, () -> service.getAllRounds(0));
        assertThrows(AccessDeniedException.class, () -> service.getRoundById(1L));
        assertThrows(AccessDeniedException.class, () -> service.createRound(validForm(RoundType.COURSE)));
        assertThrows(AccessDeniedException.class, () -> service.updateRound(1L, validForm(RoundType.COURSE)));
        assertThrows(AccessDeniedException.class, () -> service.openRound(1L));
        assertThrows(AccessDeniedException.class, () -> service.closeRound(1L));
        assertThrows(AccessDeniedException.class, () -> service.deleteDraftRound(1L));
        verifyNoInteractions(repository);
    }
}