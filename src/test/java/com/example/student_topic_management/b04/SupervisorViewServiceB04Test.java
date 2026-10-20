package com.example.student_topic_management.b04;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.entity.*;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.LecturerProfile;
import com.example.student_topic_management.identity.repository.LecturerProfileRepository;
import com.example.student_topic_management.repository.*;
import com.example.student_topic_management.service.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.example.student_topic_management.b04.B04Fixtures.*;

class SupervisorViewServiceB04Test {
    final TopicRepository topics = mock(TopicRepository.class);
    final TopicSupervisorRepository supervisors = mock(TopicSupervisorRepository.class);
    final LecturerProfileRepository lecturers = mock(LecturerProfileRepository.class);
    final SupervisorService writes = mock(SupervisorService.class);
    final SupervisorViewService service = new SupervisorViewService(topics, supervisors, lecturers, writes);
    Topic topic;

    @BeforeEach void setup() {
        topic = topic(TopicStatus.DRAFT);
        when(topics.findById(10L)).thenReturn(Optional.of(topic));
        when(writes.getSupervisors(10L)).thenReturn(List.of());
        when(lecturers.findAllById(any())).thenReturn(List.of());
        when(lecturers.findAll()).thenReturn(List.of());
    }

    @Test void filtersCandidatesAndKeepsMultipleRolesAndDisabledHistoricalSupervisor() {
        LecturerProfile assigned = lecturer(20L, "Old lecturer", false, "LECTURER");
        LecturerProfile regular = lecturer(21L, "Beta", true, "LECTURER");
        LecturerProfile dean = lecturer(22L, "Alpha", true, "LECTURER", "DEAN");
        LecturerProfile head = lecturer(23L, "Gamma", true, "DEPARTMENT_HEAD", "LECTURER");
        LecturerProfile disabled = lecturer(24L, "Disabled", false, "LECTURER");
        LecturerProfile noRole = lecturer(25L, "No role", true, "DEAN");
        when(writes.getSupervisors(10L)).thenReturn(List.of(new TopicSupervisor(10L, 20L, (byte) 1)));
        when(lecturers.findAllById(any())).thenReturn(List.of(assigned));
        when(lecturers.findAll()).thenReturn(List.of(assigned, regular, dean, head, disabled, noRole));
        var page = service.getPage(10L);
        assertTrue(page.canEdit());
        assertEquals(List.of(22L, 21L, 23L), page.candidates().stream().map(SupervisorViewService.LecturerOption::id).toList());
        assertEquals(List.of(2), page.availableSlots());
        assertEquals(20L, page.slots().get(0).lecturerId());
        assertEquals("Old lecturer", page.slots().get(0).fullName());
        assertNull(page.slots().get(1).lecturerId());
    }

    @Test void emptyDraftHasBothSlots() {
        var page = service.getPage(10L);
        assertEquals(List.of(1, 2), page.availableSlots());
        assertTrue(page.slots().stream().allMatch(slot -> slot.lecturerId() == null));
    }

    @ParameterizedTest @EnumSource(value = TopicStatus.class, names = "DRAFT", mode = EnumSource.Mode.EXCLUDE)
    void nonDraftIsReadOnly(TopicStatus status) {
        topic.setStatus(status);
        assertFalse(service.getPage(10L).canEdit());
        verify(lecturers, never()).findAll();
    }

    @Test void approvedRegistrationLocksDraftAndHidesCandidates() {
        when(supervisors.countApprovedRegistrationsByTopicId(10L)).thenReturn(1L);
        var page = service.getPage(10L);
        assertFalse(page.canEdit());
        assertTrue(page.approvedRegistration());
        assertTrue(page.candidates().isEmpty());
        verify(lecturers, never()).findAll();
    }

    @Test void missingOrInvalidTopicFailsClearly() {
        assertThrows(BusinessException.class, () -> service.getPage(null));
        assertThrows(BusinessException.class, () -> service.getPage(0L));
        assertThrows(BusinessException.class, () -> service.getPage(999L));
    }

    @Test void topicChooserUsesActualIdsAndClampsNegativePage() {
        Page<Topic> page = new PageImpl<>(List.of(topic));
        when(topics.findAll(any(Pageable.class))).thenReturn(page);
        assertEquals(10L, service.getTopics(-1).getContent().get(0).getId());
        verify(topics).findAll(PageRequest.of(0, 10, Sort.by("id").descending()));
    }
}
