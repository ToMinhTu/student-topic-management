
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

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

    private static final Long GROUP_ID = 3L;
    private static final Long TOPIC_ID = 1L;
    private static final Long ROUND_ID = 1L;
    private static final Long STUDENT_ID = 11L;
    private static final Long REGISTRATION_ID = 100L;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-19T07:00:00Z"),
            ZoneOffset.UTC
    );

    // =====================================================
    // KHOI TAO MOCK
    // =====================================================

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

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(STUDENT_ID);

        // Nhom 3 thuoc dot 1
        when(jdbc.queryForList(
                contains("SELECT round_id"),
                eq(Long.class),
                eq(GROUP_ID)
        )).thenReturn(List.of(ROUND_ID));

        // Nhom dang FORMING
        when(jdbc.queryForList(
                contains("FROM student_groups"),
                eq(String.class),
                eq(GROUP_ID),
                eq(ROUND_ID)
        )).thenReturn(List.of("FORMING"));

        // Sinh vien 11 la truong nhom
        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(GROUP_ID),
                eq(STUDENT_ID)
        )).thenReturn(1);

        // Nhom co 2 thanh vien
        when(jdbc.queryForObject(
                contains("FROM group_members"),
                eq(Integer.class),
                eq(GROUP_ID)
        )).thenReturn(2);
    }

    // =====================================================
    // CAC HAM HO TRO MOCK
    // =====================================================

    private void mockValidConditions() {

        when(jdbc.query(
                contains("FROM registration_rounds"),
                ArgumentMatchers.<RowMapper<int[]>>any(),
                eq(ROUND_ID)
        )).thenReturn(
                List.of(new int[]{2, 3})
        );

        when(jdbc.queryForObject(
                contains("FROM topics"),
                eq(Integer.class),
                eq(TOPIC_ID),
                eq(ROUND_ID)
        )).thenReturn(1);

        // De tai chua bi giu cho
        when(jdbc.queryForObject(
                contains("status IN ('PENDING', 'APPROVED')"),
                eq(Integer.class),
                eq(TOPIC_ID)
        )).thenReturn(0);
    }

    private void mockNoExistingRegistration() {

        when(jdbc.query(
                contains("FROM topic_registrations"),
                ArgumentMatchers.<RowMapper<Object>>any(),
                eq(GROUP_ID)
        )).thenReturn(List.of());
    }

    private void mockExistingRegistration(String status) {

        when(jdbc.query(
                contains("FROM topic_registrations"),
                ArgumentMatchers.<RowMapper<Object>>any(),
                eq(GROUP_ID)
        )).thenAnswer(invocation -> {

            RowMapper<?> mapper = invocation.getArgument(1);

            ResultSet rs = mock(ResultSet.class);

            when(rs.getLong("id"))
                    .thenReturn(REGISTRATION_ID);

            when(rs.getLong("topic_id"))
                    .thenReturn(2L);

            when(rs.getLong("round_id"))
                    .thenReturn(ROUND_ID);

            when(rs.getString("status"))
                    .thenReturn(status);

            return List.of(mapper.mapRow(rs, 0));
        });
    }

    private void mockSuccessfulInsert() {

        when(jdbc.update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        )).thenAnswer(invocation -> {

            PreparedStatementCreator creator =
                    invocation.getArgument(0);

            KeyHolder keyHolder =
                    invocation.getArgument(1);

            Connection connection = mock(Connection.class);

            PreparedStatement statement =
                    mock(PreparedStatement.class);

            when(connection.prepareStatement(
                    contains("INSERT INTO topic_registrations"),
                    eq(java.sql.Statement.RETURN_GENERATED_KEYS)
            )).thenReturn(statement);

            creator.createPreparedStatement(connection);

            verify(statement).setLong(1, GROUP_ID);
            verify(statement).setLong(2, TOPIC_ID);
            verify(statement).setLong(3, ROUND_ID);
            verify(statement).setLong(4, STUDENT_ID);

            keyHolder.getKeyList().add(
                    Map.of("GENERATED_KEY", REGISTRATION_ID)
            );

            return 1;
        });
    }

    private void mockSnapshot(int count) {

        when(jdbc.update(
                contains("INSERT INTO registration_members"),
                eq(REGISTRATION_ID),
                eq(GROUP_ID)
        )).thenReturn(count);
    }

    private void mockAudit(String action, int result) {

        when(jdbc.update(
                contains("INSERT INTO audit_logs"),
                eq(STUDENT_ID),
                eq(action),
                eq("TOPIC_REGISTRATION"),
                eq(REGISTRATION_ID),
                anyString()
        )).thenReturn(result);
    }

    private void mockSuccessfulResubmission(String status) {

        mockExistingRegistration(status);

        // Ho so khong co du lieu phu thuoc
        when(jdbc.queryForObject(
                contains("FROM reports"),
                eq(Integer.class),
                eq(REGISTRATION_ID),
                eq(REGISTRATION_ID),
                eq(REGISTRATION_ID),
                eq(REGISTRATION_ID)
        )).thenReturn(0);

        // Cap nhat ho so cu thanh PENDING
        when(jdbc.update(
                contains("UPDATE topic_registrations"),
                eq(TOPIC_ID),
                eq(STUDENT_ID),
                any(LocalDateTime.class),
                eq(REGISTRATION_ID),
                eq(GROUP_ID),
                eq(ROUND_ID)
        )).thenReturn(1);

        // Xoa snapshot cu
        when(jdbc.update(
                contains("DELETE FROM registration_members"),
                eq(REGISTRATION_ID)
        )).thenReturn(2);
    }

    // =====================================================
    // CAC HAM MOCK CHO C04.4 - HUY DANG KY
    // =====================================================

    private void mockCancellation(String status) {

        // Tim nhom va dot dang ky cua ho so
        when(jdbc.query(
                contains("SELECT group_id, round_id"),
                ArgumentMatchers.<RowMapper<long[]>>any(),
                eq(REGISTRATION_ID)
        )).thenReturn(
                List.of(new long[]{GROUP_ID, ROUND_ID})
        );

        // Kiem tra trang thai ho so duoc khoa
        when(jdbc.queryForList(
                contains("FROM topic_registrations"),
                eq(String.class),
                eq(REGISTRATION_ID),
                eq(GROUP_ID),
                eq(ROUND_ID)
        )).thenReturn(List.of(status));
    }

    private void mockSuccessfulCancellation() {

        mockCancellation("PENDING");

        when(jdbc.update(
                contains("SET status = 'CANCELLED'"),
                eq(REGISTRATION_ID),
                eq(GROUP_ID),
                eq(ROUND_ID)
        )).thenReturn(1);

        mockAudit("REGISTRATION_CANCELLED", 1);
    }

    // =====================================================
    // TEST 1: KHONG PHAI TRUONG NHOM KHONG DUOC DANG KY
    // =====================================================

    @Test
    void nonLeaderCannotRegister() {

        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(GROUP_ID),
                eq(STUDENT_ID)
        )).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
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
                () -> service.registerTopic(0L, TOPIC_ID)
        );

        verifyNoInteractions(
                jdbc,
                studentAccess,
                roundAccess
        );
    }

    // =====================================================
    // TEST 3: NHOM DA CO HO SO PENDING
    // =====================================================

    @Test
    void groupCannotRegisterTwice() {

        mockValidConditions();
        mockExistingRegistration("PENDING");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "Ho so hien tai khong cho phep dang ky lai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );
    }

    // =====================================================
    // TEST 4: DE TAI DA BI GIU CHO
    // =====================================================

    @Test
    void reservedTopicCannotBeRegisteredAgain() {

        mockValidConditions();
        mockNoExistingRegistration();

        when(jdbc.queryForObject(
                contains("status IN ('PENDING', 'APPROVED')"),
                eq(Integer.class),
                eq(TOPIC_ID)
        )).thenReturn(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
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
    // TEST 5: DANG KY MOI THANH CONG
    // =====================================================

    @Test
    void validLeaderCanRegisterTopicSuccessfully() {

        mockValidConditions();
        mockNoExistingRegistration();
        mockSuccessfulInsert();
        mockSnapshot(2);
        mockAudit("REGISTRATION_SUBMITTED", 1);

        assertDoesNotThrow(
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        verify(jdbc).update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );

        verify(jdbc).update(
                contains("INSERT INTO registration_members"),
                eq(REGISTRATION_ID),
                eq(GROUP_ID)
        );

        verify(jdbc).update(
                contains("INSERT INTO audit_logs"),
                eq(STUDENT_ID),
                eq("REGISTRATION_SUBMITTED"),
                eq("TOPIC_REGISTRATION"),
                eq(REGISTRATION_ID),
                anyString()
        );

        verify(roundAccess).lockRound(ROUND_ID);

        verify(roundAccess).requireStudentWindow(
                eq(ROUND_ID),
                eq(clock.instant())
        );

        verify(studentAccess).lockEnabledStudent(STUDENT_ID);
    }

    // =====================================================
    // TEST 6: LUU SNAPSHOT THAT BAI
    // =====================================================

    @Test
    void snapshotMemberCountMismatchIsRejected() {

        mockValidConditions();
        mockNoExistingRegistration();
        mockSuccessfulInsert();
        mockSnapshot(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "Luu danh sach thanh vien dang ky that bai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("INSERT INTO audit_logs"),
                any(), any(), any(), any(), any()
        );
    }

    // =====================================================
    // TEST 7: DANG KY LAI SAU KHI BI TU CHOI
    // =====================================================

    @Test
    void rejectedGroupCanRegisterAgain() {

        mockValidConditions();
        mockSuccessfulResubmission("REJECTED");
        mockSnapshot(2);
        mockAudit("REGISTRATION_RESUBMITTED", 1);

        assertDoesNotThrow(
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        verify(jdbc, never()).update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );

        verify(jdbc).update(
                contains("UPDATE topic_registrations"),
                eq(TOPIC_ID),
                eq(STUDENT_ID),
                any(LocalDateTime.class),
                eq(REGISTRATION_ID),
                eq(GROUP_ID),
                eq(ROUND_ID)
        );

        verify(jdbc).update(
                contains("DELETE FROM registration_members"),
                eq(REGISTRATION_ID)
        );

        verify(jdbc).update(
                contains("INSERT INTO registration_members"),
                eq(REGISTRATION_ID),
                eq(GROUP_ID)
        );

        verify(jdbc).update(
                contains("INSERT INTO audit_logs"),
                eq(STUDENT_ID),
                eq("REGISTRATION_RESUBMITTED"),
                eq("TOPIC_REGISTRATION"),
                eq(REGISTRATION_ID),
                contains("Previous Topic ID: 2")
        );
    }

    // =====================================================
    // TEST 8: KHONG DANG KY LAI KHI APPROVED
    // =====================================================

    @Test
    void approvedGroupCannotRegisterAgain() {

        mockValidConditions();
        mockExistingRegistration("APPROVED");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "Ho so hien tai khong cho phep dang ky lai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(), any(), any(), any(), any(), any()
        );

        verify(jdbc, never()).update(
                contains("DELETE FROM registration_members"),
                eq(REGISTRATION_ID)
        );
    }

    // =====================================================
    // TEST 9: DANG KY LAI SAU KHI HUY
    // =====================================================

    @Test
    void cancelledGroupCanRegisterAgain() {

        mockValidConditions();
        mockSuccessfulResubmission("CANCELLED");
        mockSnapshot(2);
        mockAudit("REGISTRATION_RESUBMITTED", 1);

        assertDoesNotThrow(
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        // Phai su dung lai ho so cu
        verify(jdbc).update(
                contains("UPDATE topic_registrations"),
                eq(TOPIC_ID),
                eq(STUDENT_ID),
                any(LocalDateTime.class),
                eq(REGISTRATION_ID),
                eq(GROUP_ID),
                eq(ROUND_ID)
        );

        // Khong INSERT ho so moi
        verify(jdbc, never()).update(
                any(PreparedStatementCreator.class),
                any(KeyHolder.class)
        );

        // Lam moi snapshot
        verify(jdbc).update(
                contains("DELETE FROM registration_members"),
                eq(REGISTRATION_ID)
        );

        verify(jdbc).update(
                contains("INSERT INTO registration_members"),
                eq(REGISTRATION_ID),
                eq(GROUP_ID)
        );

        // Ghi nhat ky dang ky lai
        verify(jdbc).update(
                contains("INSERT INTO audit_logs"),
                eq(STUDENT_ID),
                eq("REGISTRATION_RESUBMITTED"),
                eq("TOPIC_REGISTRATION"),
                eq(REGISTRATION_ID),
                anyString()
        );
    }

    // =====================================================
    // TEST 10: DANG KY LAI DE TAI DA BI GIU CHO
    // =====================================================

    @Test
    void reRegisterReservedTopicFails() {

        mockValidConditions();
        mockSuccessfulResubmission("REJECTED");

        when(jdbc.queryForObject(
                contains("status IN ('PENDING', 'APPROVED')"),
                eq(Integer.class),
                eq(TOPIC_ID)
        )).thenReturn(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "De tai da duoc nhom khac dang ky",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(), any(), any(), any(), any(), any()
        );
    }

    // =====================================================
    // TEST 11: SNAPSHOT DANG KY LAI THAT BAI
    // =====================================================

    @Test
    void reRegisterSnapshotFailure() {

        mockValidConditions();
        mockSuccessfulResubmission("REJECTED");
        mockSnapshot(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "Luu danh sach thanh vien dang ky that bai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("INSERT INTO audit_logs"),
                any(), any(), any(), any(), any()
        );
    }

    // =====================================================
    // TEST 12: HO SO CO DU LIEU PHU THUOC
    // =====================================================

    @Test
    void reRegisterWithDependentDataFails() {

        mockValidConditions();
        mockSuccessfulResubmission("REJECTED");

        when(jdbc.queryForObject(
                contains("FROM reports"),
                eq(Integer.class),
                eq(REGISTRATION_ID),
                eq(REGISTRATION_ID),
                eq(REGISTRATION_ID),
                eq(REGISTRATION_ID)
        )).thenReturn(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "Ho so da co du lieu xu ly, khong the dang ky lai",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(), any(), any(), any(), any(), any()
        );
    }

    // =====================================================
    // TEST 13: AUDIT DANG KY LAI THAT BAI
    // =====================================================

    @Test
    void reRegisterAuditFailure() {

        mockValidConditions();
        mockSuccessfulResubmission("REJECTED");
        mockSnapshot(2);
        mockAudit("REGISTRATION_RESUBMITTED", 0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.registerTopic(GROUP_ID, TOPIC_ID)
        );

        assertEquals(
                "Ghi nhat ky dang ky that bai",
                ex.getMessage()
        );
    }

    // =====================================================
    // C04.4 - TEST 14: HUY HO SO PENDING THANH CONG
    // =====================================================

    @Test
    void leaderCanCancelPendingRegistration() {

        mockSuccessfulCancellation();

        assertDoesNotThrow(
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        verify(roundAccess).lockRound(ROUND_ID);

        verify(roundAccess).requireStudentWindow(
                ROUND_ID,
                clock.instant()
        );

        verify(studentAccess).lockEnabledStudent(STUDENT_ID);

        verify(jdbc).update(
                contains("SET status = 'CANCELLED'"),
                eq(REGISTRATION_ID),
                eq(GROUP_ID),
                eq(ROUND_ID)
        );

        verify(jdbc).update(
                contains("INSERT INTO audit_logs"),
                eq(STUDENT_ID),
                eq("REGISTRATION_CANCELLED"),
                eq("TOPIC_REGISTRATION"),
                eq(REGISTRATION_ID),
                anyString()
        );

        // Khong xoa ho so hay snapshot
        verify(jdbc, never()).update(
                contains("DELETE FROM registration_members"),
                anyLong()
        );
    }

    // =====================================================
    // C04.4 - TEST 15: THANH VIEN THUONG KHONG DUOC HUY
    // =====================================================

    @Test
    void nonLeaderCannotCancelRegistration() {

        mockCancellation("PENDING");

        when(jdbc.queryForObject(
                contains("slot_no = 1"),
                eq(Integer.class),
                eq(GROUP_ID),
                eq(STUDENT_ID)
        )).thenReturn(0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        assertEquals(
                "Chi truong nhom moi duoc huy dang ky",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("SET status = 'CANCELLED'"),
                anyLong(), anyLong(), anyLong()
        );
    }

    // =====================================================
    // C04.4 - TEST 16: KHONG HUY HO SO APPROVED
    // =====================================================

    @Test
    void approvedRegistrationCannotBeCancelled() {

        mockCancellation("APPROVED");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        assertEquals(
                "Chi duoc huy ho so dang cho duyet",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("SET status = 'CANCELLED'"),
                anyLong(), anyLong(), anyLong()
        );
    }

    // =====================================================
    // C04.4 - TEST 17: KHONG HUY HO SO REJECTED
    // =====================================================

    @Test
    void rejectedRegistrationCannotBeCancelled() {

        mockCancellation("REJECTED");

        assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        verify(jdbc, never()).update(
                contains("SET status = 'CANCELLED'"),
                anyLong(), anyLong(), anyLong()
        );
    }

    // =====================================================
    // C04.4 - TEST 18: KHONG HUY LAN HAI
    // =====================================================

    @Test
    void cancelledRegistrationCannotBeCancelledAgain() {

        mockCancellation("CANCELLED");

        assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        verify(jdbc, never()).update(
                contains("SET status = 'CANCELLED'"),
                anyLong(), anyLong(), anyLong()
        );
    }

    // =====================================================
    // C04.4 - TEST 19: KHONG HUY NGOAI THOI HAN
    // =====================================================

    @Test
    void cannotCancelOutsideStudentWindow() {

        mockCancellation("PENDING");

        doThrow(new IllegalStateException(
                "Ngoai thoi gian dang ky sinh vien"
        )).when(roundAccess).requireStudentWindow(
                eq(ROUND_ID),
                eq(clock.instant())
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        assertEquals(
                "Ngoai thoi gian dang ky sinh vien",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("SET status = 'CANCELLED'"),
                anyLong(), anyLong(), anyLong()
        );
    }

    // =====================================================
    // C04.4 - TEST 20: AUDIT HUY THAT BAI
    // =====================================================

    @Test
    void cancelAuditFailure() {

        mockSuccessfulCancellation();

        mockAudit("REGISTRATION_CANCELLED", 0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(REGISTRATION_ID)
        );

        assertEquals(
                "Ghi nhat ky huy dang ky that bai",
                ex.getMessage()
        );
    }

    // =====================================================
    // C04.4 - TEST 21: MA HO SO KHONG HOP LE
    // =====================================================

    @Test
    void invalidRegistrationIdCannotBeCancelled() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelRegistration(0L)
        );

        verifyNoInteractions(
                jdbc,
                studentAccess,
                roundAccess
        );
    }
}
