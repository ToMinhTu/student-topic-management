
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.KeyHolder;

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

        // Sinh vien 11 la truong nhom
        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(3L),
                eq(11L)
        )).thenReturn(1);

        // Nhom co 2 thanh vien dang hoat dong
        when(jdbc.queryForObject(
                contains("FROM group_members"),
                eq(Integer.class),
                eq(3L)
        )).thenReturn(2);
    }

    // =====================================================
    // DU LIEU MOCK DUNG CHO CAC TRUONG HOP HOP LE
    // =====================================================

    private void mockValidRegistrationConditions() {

        // Gioi han so thanh vien: tu 2 den 3
        when(jdbc.query(
                contains("FROM registration_rounds"),
                ArgumentMatchers.<RowMapper<int[]>>any(),
                eq(1L)
        )).thenReturn(List.<int[]>of(
                new int[] {2, 3}
        ));

        // De tai 1 da duoc cong bo
        when(jdbc.queryForObject(
                contains("FROM topics"),
                eq(Integer.class),
                eq(1L),
                eq(1L)
        )).thenReturn(1);
    }

    private void mockNoExistingRegistration() {

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
    }

    // Gia lap INSERT bang GeneratedKeyHolder.
    // Service se nhan duoc registrationId = 100.
    private void mockSuccessfulInsert() {

        when(jdbc.update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        )).thenAnswer(invocation -> {

            PreparedStatementCreator creator =
                    invocation.getArgument(0);

            KeyHolder keyHolder =
                    invocation.getArgument(1);

            // Gia lap JDBC connection de kiem tra SQL INSERT
            Connection connection = mock(Connection.class);
            PreparedStatement statement =
                    mock(PreparedStatement.class);

            when(connection.prepareStatement(
                    contains("INSERT INTO topic_registrations"),
                    eq(java.sql.Statement.RETURN_GENERATED_KEYS)
            )).thenReturn(statement);

            creator.createPreparedStatement(connection);

            verify(statement).setLong(1, 3L);
            verify(statement).setLong(2, 1L);
            verify(statement).setLong(3, 1L);
            verify(statement).setLong(4, 11L);

            // Gia lap generated key tu MySQL
            keyHolder.getKeyList().add(
                    java.util.Map.of(
                            "GENERATED_KEY", 100L
                    )
            );

            return 1;
        });

        // Snapshot ghi thanh cong 2 thanh vien
        when(jdbc.update(
                contains("INSERT INTO registration_members"),
                eq(100L),
                eq(3L)
        )).thenReturn(2);

        // Audit ghi thanh cong
        when(jdbc.update(
                contains("INSERT INTO audit_logs"),
                eq(11L),
                eq("REGISTRATION_SUBMITTED"),
                eq("TOPIC_REGISTRATION"),
                eq(100L),
                anyString()
        )).thenReturn(1);
    }

    // =====================================================
    // TEST 1: KHONG PHAI TRUONG NHOM
    // =====================================================

    @Test
    void nonLeaderCannotRegister() {

        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(3L),
                eq(11L)
        )).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(3L, 1L)
        );

        assertEquals(
                "Chi truong nhom moi duoc dang ky de tai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );
    }

    // =====================================================
    // TEST 2: MA NHOM KHONG HOP LE
    // =====================================================

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

    // =====================================================
    // TEST 3: NHOM DA TUNG DANG KY
    // =====================================================

    @Test
    void groupCannotRegisterTwice() {

        mockValidRegistrationConditions();

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
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );
    }

    // =====================================================
    // TEST 4: DE TAI DA DUOC GIU CHO
    // =====================================================

    @Test
    void reservedTopicCannotBeRegisteredAgain() {

        mockValidRegistrationConditions();

        when(jdbc.queryForObject(
                contains("FROM topic_registrations"),
                eq(Integer.class),
                eq(3L)
        )).thenReturn(0);

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
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );
    }

    // =====================================================
    // TEST 5: DANG KY THANH CONG, LUU SNAPSHOT VA AUDIT
    // =====================================================

    @Test
    void validLeaderCanRegisterTopicSuccessfully() {

        mockValidRegistrationConditions();
        mockNoExistingRegistration();
        mockSuccessfulInsert();

        assertDoesNotThrow(
                () -> service.registerTopic(3L, 1L)
        );

        // Kiem tra co INSERT ho so dang ky
        verify(jdbc, times(1)).update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );

        // Kiem tra luu snapshot 2 thanh vien
        verify(jdbc, times(1)).update(
                contains("INSERT INTO registration_members"),
                eq(100L),
                eq(3L)
        );

        // Kiem tra ghi nhat ky
        verify(jdbc, times(1)).update(
                contains("INSERT INTO audit_logs"),
                eq(11L),
                eq("REGISTRATION_SUBMITTED"),
                eq("TOPIC_REGISTRATION"),
                eq(100L),
                contains("Group ID: 3")
        );

        // Kiem tra khoa dot dang ky
        verify(roundAccess).lockRound(1L);

        // Kiem tra thoi gian dang ky
        verify(roundAccess).requireStudentWindow(
                eq(1L),
                eq(clock.instant())
        );

        // Kiem tra khoa sinh vien
        verify(studentAccess).lockEnabledStudent(11L);
    }

    // =====================================================
    // TEST 6: SNAPSHOT KHONG DU THANH VIEN
    // =====================================================

    @Test
    void snapshotMemberCountMismatchIsRejected() {

        mockValidRegistrationConditions();
        mockNoExistingRegistration();
        mockSuccessfulInsert();

        // DB chi luu 1 nguoi, nhom co 2 nguoi
        when(jdbc.update(
                contains("INSERT INTO registration_members"),
                eq(100L),
                eq(3L)
        )).thenReturn(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(3L, 1L)
        );

        assertEquals(
                "Luu danh sach thanh vien dang ky that bai",
                ex.getMessage()
        );

        // Khong duoc ghi audit khi snapshot that bai
        verify(jdbc, never()).update(
                contains("INSERT INTO audit_logs"),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }
}
