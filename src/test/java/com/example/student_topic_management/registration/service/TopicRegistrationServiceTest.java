
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class TopicRegistrationServiceTest {

    private JdbcTemplate jdbc;
    private StudentAccess studentAccess;
    private RoundAccess roundAccess;
    private TopicRegistrationService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-19T07:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        studentAccess = mock(StudentAccess.class);
        roundAccess = mock(RoundAccess.class);

        service = new TopicRegistrationService(
                jdbc,
                studentAccess,
                roundAccess,
                clock
        );

        // Sinh vien 11, nhom 3, dot 1
        when(studentAccess.requireCurrentStudentId())
                .thenReturn(11L);

        when(jdbc.queryForList(
                contains("SELECT round_id"),
                eq(Long.class),
                eq(3L)
        )).thenReturn(List.of(1L));

        when(jdbc.queryForList(
                contains("SELECT status"),
                eq(String.class),
                eq(3L),
                eq(1L)
        )).thenReturn(List.of("FORMING"));

        // Truong nhom hop le
        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(3L),
                eq(11L)
        )).thenReturn(1);

        // Nhom co 2 thanh vien
        when(jdbc.queryForObject(
                contains("FROM group_members"),
                eq(Integer.class),
                eq(3L)
        )).thenReturn(2);
    }

    @Test
    void nonLeaderCannotRegister() {

        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(3L),
                eq(11L)
        )).thenReturn(0);

        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.registerTopic(3L, 1L)
                );

        assertEquals(
                "Chi truong nhom moi duoc dang ky de tai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                anyString(),
                any(Object[].class)
        );
    }

    @Test
    void invalidGroupIdIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerTopic(0L, 1L)
        );

        verifyNoInteractions(
                jdbc,
                studentAccess,
                roundAccess
        );
    }

private void mockValidRegistrationConditions() {

    // Gioi han nhom: 2 den 3 thanh vien
    when(jdbc.query(
            contains("SELECT group_min_size, group_max_size"),
            any(org.springframework.jdbc.core.RowMapper.class),
            eq(1L)
    )).thenReturn(List.of(new int[] {2, 3}));

    // De tai da duoc cong bo
    when(jdbc.queryForObject(
            contains("FROM topics"),
            eq(Integer.class),
            eq(1L),
            eq(1L)
    )).thenReturn(1);
}

@Test
void groupCannotRegisterTwice() {

    mockValidRegistrationConditions();

    // Nhom 3 da co mot dang ky
    when(jdbc.queryForObject(
            contains("FROM topic_registrations"),
            eq(Integer.class),
            eq(3L)
    )).thenReturn(1);

    IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.registerTopic(3L, 1L)
    );

    assertEquals(
            "Nhom da tung dang ky de tai",
            ex.getMessage()
    );

    verify(jdbc, never()).update(
            anyString(),
            any(Object[].class)
    );
}

@Test
void reservedTopicCannotBeRegisteredAgain() {

    mockValidRegistrationConditions();

    // Nhom 3 chua dang ky de tai nao
    when(jdbc.queryForObject(
            contains("FROM topic_registrations"),
            eq(Integer.class),
            eq(3L)
    )).thenReturn(0);

    // De tai 1 da duoc nhom khac giu cho
    when(jdbc.queryForObject(
            contains("status IN ('PENDING', 'APPROVED')"),
            eq(Integer.class),
            eq(1L)
    )).thenReturn(1);

    IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.registerTopic(3L, 1L)
    );

    assertEquals(
            "De tai da duoc nhom khac dang ky",
            ex.getMessage()
    );

    verify(jdbc, never()).update(
            anyString(),
            any(Object[].class)
    );
}

    @Test
    void validLeaderCanRegisterTopicSuccessfully() {

        // Gia lap nhom va de tai du dieu kien
        mockValidRegistrationConditions();

        // Nhom chua tung dang ky
        when(jdbc.queryForObject(
                contains("FROM topic_registrations"),
                eq(Integer.class),
                eq(3L)
        )).thenReturn(0);

        // De tai chua bi giu cho
        when(jdbc.queryForObject(
                contains("status IN ('PENDING', 'APPROVED')"),
                eq(Integer.class),
                eq(1L)
        )).thenReturn(0);

        // Gia lap INSERT thanh cong
        when(jdbc.update(
                contains("INSERT INTO topic_registrations"),
                eq(3L),
                eq(1L),
                eq(1L),
                eq(11L)
        )).thenReturn(1);

        // Kiem tra khong phat sinh ngoai le
        assertDoesNotThrow(
                () -> service.registerTopic(3L, 1L)
        );

        // Kiem tra INSERT duoc goi dung mot lan
        verify(jdbc, times(1)).update(
                contains("INSERT INTO topic_registrations"),
                eq(3L),
                eq(1L),
                eq(1L),
                eq(11L)
        );

        // Kiem tra thoi gian dang ky
        verify(roundAccess).requireStudentWindow(
                eq(1L),
                eq(clock.instant())
        );
    }

}
