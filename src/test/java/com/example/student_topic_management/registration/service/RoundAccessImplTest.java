package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class RoundAccessImplTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private ResultSet resultSet;

    private RoundAccessImpl roundAccess;

    @BeforeEach
    void setup() {
        roundAccess = new RoundAccessImpl(jdbc);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void readsUtcDatetimeWithoutApplyingTheSystemTimezoneAgain() throws Exception {
        when(resultSet.getString("status")).thenReturn("OPEN");
        when(resultSet.getObject("student_starts_at", LocalDateTime.class))
                .thenReturn(LocalDateTime.of(2026, 10, 21, 2, 30));
        when(resultSet.getObject("student_ends_at", LocalDateTime.class))
                .thenReturn(LocalDateTime.of(2026, 10, 23, 20, 0));

        when(jdbc.query(anyString(), any(RowMapper.class), eq(36L)))
                .thenAnswer(invocation -> {
                    RowMapper mapper = invocation.getArgument(1);
                    return List.of(mapper.mapRow(resultSet, 0));
                });

        assertDoesNotThrow(() -> roundAccess.requireStudentWindow(
                36L,
                Instant.parse("2026-10-21T02:31:00Z")));
    }
}
