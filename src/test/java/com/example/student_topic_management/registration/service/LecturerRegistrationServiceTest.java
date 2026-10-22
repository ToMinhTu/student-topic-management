package com.example.student_topic_management.registration.service;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.AccessDeniedException;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.service.CurrentUserService;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class LecturerRegistrationServiceTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CurrentUserService current = mock(CurrentUserService.class);
    private final AppUser actor = mock(AppUser.class);
    private final LecturerRegistrationService service = new LecturerRegistrationService(jdbc, current);

    @BeforeEach
    void setup() {
        Role lecturer = mock(Role.class);
        when(lecturer.getCode()).thenReturn("LECTURER");
        when(actor.getRoles()).thenReturn(Set.of(lecturer));
        when(actor.getId()).thenReturn(21L);
        when(actor.getEnabled()).thenReturn(true);
        when(current.getCurrentUser()).thenReturn(actor);
    }

    @Test
    void countAndRowsAreBothRestrictedToCurrentSupervisorAndPageIsClamped() throws Exception {
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(21L);
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class)))
                .thenAnswer(invocation -> {
                    ResultSet row = mock(ResultSet.class);
                    when(row.getLong("id")).thenReturn(5L);
                    when(row.getString("group_name")).thenReturn("Nhóm A");
                    when(row.getTimestamp("submitted_at"))
                            .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 10, 22, 8, 0)));
                    return List.of(((RowMapper<?>) invocation.getArgument(1)).mapRow(row, 0));
                });
        var page = service.findRegistrations(7L, " pending ", Integer.MAX_VALUE);
        assertEquals(2, page.getNumber());
        assertEquals(21, page.getTotalElements());
        assertEquals("Nhóm A", page.getContent().get(0).groupName());
        verify(jdbc).queryForObject(argThat(sql -> sql.contains("EXISTS") && sql.contains("ts.lecturer_id = ?")),
                eq(Long.class), eq(21L), eq(7L), eq(7L), eq("PENDING"), eq("PENDING"));
        verify(jdbc).query(argThat(sql -> sql.contains("ts.lecturer_id = ?") && sql.contains("LIMIT ? OFFSET ?")),
                org.mockito.ArgumentMatchers.<RowMapper<Object>>any(),
                eq(21L), eq(7L), eq(7L), eq("PENDING"), eq("PENDING"), eq(10), eq(20L));
    }

    @Test
    void emptyDataDoesNotQueryUnscopedRows() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(0L);
        var page = service.findRegistrations(null, "invalid", -1);
        assertTrue(page.isEmpty());
        assertEquals(0, page.getNumber());
        verify(jdbc, never()).query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class));
    }

    @Test
    void roundOptionsAreRestrictedToCurrentSupervisor() {
        service.findRounds();
        verify(jdbc).query(contains("WHERE ts.lecturer_id = ?"),
                org.mockito.ArgumentMatchers.<RowMapper<Object>>any(), eq(21L));
    }

    @Test
    void disabledUserCannotReadData() {
        when(actor.getEnabled()).thenReturn(false);
        assertThrows(AccessDeniedException.class, () -> service.findRegistrations(null, null, 0));
        assertThrows(AccessDeniedException.class, service::findRounds);
        verifyNoInteractions(jdbc);
    }

    @Test
    void mixedStudentLecturerAccountIsDenied() {
        Role lecturer = mock(Role.class);
        Role student = mock(Role.class);
        when(lecturer.getCode()).thenReturn("LECTURER");
        when(student.getCode()).thenReturn("STUDENT");
        when(actor.getRoles()).thenReturn(Set.of(lecturer, student));
        assertThrows(AccessDeniedException.class, () -> service.findRegistrations(null, null, 0));
        verifyNoInteractions(jdbc);
    }
}
