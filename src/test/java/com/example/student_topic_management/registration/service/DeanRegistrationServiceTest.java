
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.service.CurrentUserService;

class DeanRegistrationServiceTest {

    private JdbcTemplate jdbc;
    private RoundAccess roundAccess;
    private CurrentUserService currentUserService;
    private DeanRegistrationService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-10-19T07:00:00Z"),
            ZoneOffset.UTC
    );

    private String registrationStatus;
    private String groupStatus;
    private String topicStatus;

    private int activeCount;
    private int leaderCount;
    private int snapshotCount;
    private int differentMembers;

    private int approveResult;
    private int lockResult;
    private int auditResult;

    @BeforeEach
    void setUp() {

        jdbc = mock(JdbcTemplate.class);
        roundAccess = mock(RoundAccess.class);
        currentUserService = mock(CurrentUserService.class);

        service = new DeanRegistrationService(
                jdbc,
                roundAccess,
                currentUserService,
                clock
        );

        registrationStatus = "PENDING";
        groupStatus = "FORMING";
        topicStatus = "PUBLISHED";

        activeCount = 2;
        leaderCount = 1;
        snapshotCount = 2;
        differentMembers = 0;

        approveResult = 1;
        lockResult = 1;
        auditResult = 1;
    }

    // ==========================================
    // MOCK TAI KHOAN DEAN
    // ==========================================

    private void mockDean() {

        AppUser dean = mock(AppUser.class);
        Role role = mock(Role.class);

        when(dean.getEnabled()).thenReturn(true);
        when(dean.getRoles()).thenReturn(Set.of(role));
        when(dean.getId()).thenReturn(12L);

        when(role.getCode()).thenReturn("DEAN");

        when(currentUserService.getCurrentUser())
                .thenReturn(dean);
    }

    // ==========================================
    // MOCK CAC CAU SELECT DUNG RowMapper
    // ==========================================

    private void mockQueryData() {

        when(jdbc.query(
                anyString(),
                org.mockito.ArgumentMatchers
                        .<RowMapper<Object>>any(),
                any(Object[].class)
        )).thenAnswer(invocation -> {

            String sql = invocation.getArgument(0);

            RowMapper<?> mapper =
                    invocation.getArgument(1);

            ResultSet rs = mock(ResultSet.class);

            // Ho so dang ky
            if (sql.contains("FROM topic_registrations")) {

                when(rs.getLong("group_id"))
                        .thenReturn(3L);

                when(rs.getLong("topic_id"))
                        .thenReturn(1L);

                when(rs.getLong("round_id"))
                        .thenReturn(1L);

                when(rs.getString("status"))
                        .thenReturn(registrationStatus);
            }

            // Nhom sinh vien
            else if (sql.contains("FROM student_groups")) {

                when(rs.getString("status"))
                        .thenReturn(groupStatus);

                when(rs.getLong("round_id"))
                        .thenReturn(1L);
            }

            // De tai
            else if (sql.contains("FROM topics")) {

                when(rs.getString("status"))
                        .thenReturn(topicStatus);

                when(rs.getLong("round_id"))
                        .thenReturn(1L);
            }

            // Gioi han thanh vien
            else if (sql.contains("FROM registration_rounds")) {

                when(rs.getInt("group_min_size"))
                        .thenReturn(2);

                when(rs.getInt("group_max_size"))
                        .thenReturn(3);
            }

            return List.of(mapper.mapRow(rs, 0));
        });
    }

    // ==========================================
    // MOCK CAC CAU COUNT(*)
    // ==========================================

    private void mockCounts() {

        when(jdbc.queryForObject(
                anyString(),
                eq(Integer.class),
                any(Object[].class)
        )).thenAnswer(invocation -> {

            String sql = invocation.getArgument(0);

            if (sql.contains("FROM registration_members rm")) {
                return differentMembers;
            }

            if (sql.contains("FROM registration_members")) {
                return snapshotCount;
            }

            if (sql.contains("slot_no = 1")) {
                return leaderCount;
            }

            if (sql.contains("FROM group_members")) {
                return activeCount;
            }

            return 0;
        });
    }

    // ==========================================
    // MOCK CAC CAU UPDATE VA INSERT
    // ==========================================

    private void mockUpdates() {

        when(jdbc.update(
                anyString(),
                any(Object[].class)
        )).thenAnswer(invocation -> {

            String sql = invocation.getArgument(0);

            if (sql.contains("UPDATE topic_registrations")) {
                return approveResult;
            }

            if (sql.contains("UPDATE student_groups")) {
                return lockResult;
            }

            if (sql.contains("INSERT INTO audit_logs")) {
                return auditResult;
            }

            return 0;
        });
    }

    private void mockSupervisor(boolean assigned) {
        mockValidConditions();
        AppUser lecturer = mock(AppUser.class);
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn("LECTURER");
        when(lecturer.getRoles()).thenReturn(Set.of(role));
        when(lecturer.getEnabled()).thenReturn(true);
        when(lecturer.getId()).thenReturn(21L);
        when(currentUserService.getCurrentUser()).thenReturn(lecturer);
        when(jdbc.queryForList(contains("FROM topic_supervisors"), eq(Long.class), eq(1L), eq(21L)))
                .thenReturn(assigned ? List.of(21L) : List.of());
    }

    @Test
    void assignedLecturerCanApproveAndLockGroup() {
        mockSupervisor(true);
        service.approveAsSupervisor(1L);
        verify(jdbc).update(contains("UPDATE topic_registrations"), eq(21L), any(), eq(1L));
        verify(jdbc).update(contains("UPDATE student_groups"), eq(3L));
        verify(jdbc).update(contains("INSERT INTO audit_logs"), eq(21L),
                eq("REGISTRATION_APPROVED"), eq("TOPIC_REGISTRATION"), eq(1L), anyString());
    }

    @Test
    void assignedLecturerCanRejectWithReasonWithoutLockingGroup() {
        mockSupervisor(true);
        service.rejectAsSupervisor(1L, "  Cần bổ sung  ");
        verify(jdbc).update(contains("UPDATE topic_registrations"), eq(21L), any(),
                eq("Cần bổ sung"), eq(1L));
        verify(jdbc, never()).update(contains("UPDATE student_groups"), any(Object[].class));
    }

    @Test
    void lecturerCannotApproveSomeoneElsesTopic() {
        mockSupervisor(false);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.approveAsSupervisor(1L));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void lecturerCannotRejectSomeoneElsesTopic() {
        mockSupervisor(false);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.rejectAsSupervisor(1L, "Lý do"));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void lecturerCannotUseUnrestrictedDeanEntryPoint() {
        mockSupervisor(true);
        assertThrows(IllegalStateException.class, () -> service.approveRegistration(1L));
        assertThrows(IllegalStateException.class, () -> service.rejectRegistration(1L, "Lý do"));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void supervisorStillNeedsMatchingSnapshot() {
        mockSupervisor(true);
        differentMembers = 1;
        assertThrows(IllegalStateException.class, () -> service.approveAsSupervisor(1L));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void supervisorCannotDecideAlreadyApprovedRegistration() {
        mockSupervisor(true);
        registrationStatus = "APPROVED";
        assertThrows(IllegalStateException.class, () -> service.approveAsSupervisor(1L));
        assertThrows(IllegalStateException.class, () -> service.rejectAsSupervisor(1L, "Lý do"));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void disabledSupervisorCannotDecide() {
        mockSupervisor(true);
        when(currentUserService.getCurrentUser().getEnabled()).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> service.approveAsSupervisor(1L));
        assertThrows(IllegalStateException.class, () -> service.rejectAsSupervisor(1L, "Lý do"));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    private void mockValidConditions() {

        mockDean();
        mockQueryData();
        mockCounts();
        mockUpdates();
    }

    // ==========================================
    // TEST 1: MA HO SO KHONG HOP LE
    // ==========================================

    @Test
    void invalidRegistrationIdIsRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.approveRegistration(0L)
        );

        verifyNoInteractions(
                jdbc,
                roundAccess,
                currentUserService
        );
    }

    // ==========================================
    // TEST 2: HO SO KHONG TON TAI
    // ==========================================

    @Test
    void missingRegistrationIsRejected() {

        mockDean();

        when(jdbc.query(
                contains("FROM topic_registrations"),
                org.mockito.ArgumentMatchers
                        .<RowMapper<Object>>any(),
                eq(1L)
        )).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Ho so dang ky khong ton tai",
                ex.getMessage()
        );

        verify(roundAccess, never()).lockRound(anyLong());
    }

    // ==========================================
    // TEST 3: KHONG CO QUYEN DEAN
    // ==========================================

    @Test
    void nonDeanCannotApprove() {

        AppUser user = mock(AppUser.class);
        Role role = mock(Role.class);

        when(user.getEnabled()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of(role));
        when(role.getCode()).thenReturn("STUDENT");

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Tai khoan khong co quyen duyet dang ky",
                ex.getMessage()
        );

        verifyNoInteractions(jdbc, roundAccess);
    }

    // ==========================================
    // TEST 4: HO SO DA DUOC DUYET
    // ==========================================

    @Test
    void approvedRegistrationCannotBeApprovedAgain() {

        registrationStatus = "APPROVED";
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Chi co the duyet ho so dang cho duyet",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 5: NHOM KHONG O TRANG THAI FORMING
    // ==========================================

    @Test
    void lockedGroupCannotBeApproved() {

        groupStatus = "LOCKED";
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Nhom khong o trang thai cho phep duyet",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 6: DE TAI CHUA DUOC CONG BO
    // ==========================================

    @Test
    void unpublishedTopicCannotBeApproved() {

        topicStatus = "DRAFT";
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "De tai chua duoc cong bo",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 7: NHOM KHONG DU THANH VIEN
    // ==========================================

    @Test
    void invalidMemberCountIsRejected() {

        activeCount = 1;
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "So thanh vien nhom khong hop le",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 8: SNAPSHOT KHONG KHOP
    // ==========================================

    @Test
    void snapshotMismatchIsRejected() {

        differentMembers = 1;
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Thanh vien nhom khong khop snapshot",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE topic_registrations"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 9: DUYET THANH CONG
    // ==========================================

    @Test
    void validRegistrationCanBeApproved() {

        mockValidConditions();

        assertDoesNotThrow(
                () -> service.approveRegistration(1L)
        );

        // Khoa dot dang ky
        verify(roundAccess).lockRound(1L);

        // Duyet ho so
        verify(jdbc, times(1)).update(
                contains("UPDATE topic_registrations"),
                eq(12L),
                any(java.time.LocalDateTime.class),
                eq(1L)
        );

        // Khoa nhom
        verify(jdbc, times(1)).update(
                contains("UPDATE student_groups"),
                eq(3L)
        );

        // Ghi audit
        verify(jdbc, times(1)).update(
                contains("INSERT INTO audit_logs"),
                eq(12L),
                eq("REGISTRATION_APPROVED"),
                eq("TOPIC_REGISTRATION"),
                eq(1L),
                anyString()
        );
    }

    // ==========================================
    // TEST 10: CAP NHAT HO SO THAT BAI
    // ==========================================

    @Test
    void registrationUpdateFailureIsRejected() {

        approveResult = 0;
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Khong the cap nhat trang thai ho so",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("UPDATE student_groups"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 11: KHOA NHOM THAT BAI
    // ==========================================

    @Test
    void groupLockFailureIsRejected() {

        lockResult = 0;
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Khong the khoa nhom sinh vien",
                ex.getMessage()
        );

        verify(jdbc, never()).update(
                contains("INSERT INTO audit_logs"),
                any(Object[].class)
        );
    }

    // ==========================================
    // TEST 12: GHI AUDIT THAT BAI
    // ==========================================

    @Test
    void auditFailureIsRejected() {

        auditResult = 0;
        mockValidConditions();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(1L)
        );

        assertEquals(
                "Khong the ghi nhat ky duyet ho so",
                ex.getMessage()
        );

        verify(jdbc).update(
                contains("UPDATE topic_registrations"),
                any(Object[].class)
        );

        verify(jdbc).update(
                contains("UPDATE student_groups"),
                any(Object[].class)
        );
    }

    // =====================================================
    // C04.3 - UNIT TEST TU CHOI DANG KY DE TAI
    // =====================================================

    // TEST 13: Tu choi dang ky thanh cong
    @Test
    void rejectRegistrationSuccessfully() {

        mockValidConditions();

        service.rejectRegistration(
                1L,
                "De tai chua dap ung yeu cau"
        );

        verify(roundAccess).lockRound(1L);

        verify(jdbc).update(
                argThat(sql ->
                        sql.contains("UPDATE topic_registrations")
                        && sql.contains("REJECTED")
                        && sql.contains("decision_note")
                ),
                eq(12L),
                any(),
                eq("De tai chua dap ung yeu cau"),
                eq(1L)
        );

        verify(jdbc).update(
                argThat(sql ->
                        sql.contains("INSERT INTO audit_logs")
                ),
                eq(12L),
                eq("REGISTRATION_REJECTED"),
                eq("TOPIC_REGISTRATION"),
                eq(1L),
                anyString()
        );

        verify(jdbc, never()).update(
                argThat(sql ->
                        sql.contains("UPDATE student_groups")
                ),
                any(Object[].class)
        );
    }

    // TEST 14: Khong cho phep thieu ly do
    @Test
    void rejectRegistrationWithoutReason() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectRegistration(
                        1L,
                        "   "
                )
        );

        verifyNoInteractions(
                jdbc,
                roundAccess,
                currentUserService
        );
    }

    // TEST 15: Khong cho phep tu choi ho so da duyet
    @Test
    void cannotRejectApprovedRegistration() {

        registrationStatus = "APPROVED";

        mockValidConditions();

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectRegistration(
                        1L,
                        "Khong du dieu kien"
                )
        );

        verify(jdbc, never()).update(
                anyString(),
                any(Object[].class)
        );
    }

    // TEST 16: Khong co quyen DEAN
    @Test
    void rejectRegistrationWithoutDeanRole() {

        AppUser user = mock(AppUser.class);
        Role role = mock(Role.class);

        when(user.getEnabled()).thenReturn(true);
        when(user.getRoles()).thenReturn(Set.of(role));
        when(role.getCode()).thenReturn("STUDENT");

        when(currentUserService.getCurrentUser())
                .thenReturn(user);

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectRegistration(
                        1L,
                        "Khong du dieu kien"
                )
        );

        verifyNoInteractions(jdbc, roundAccess);
    }

    // TEST 17: Loi ghi audit log
    @Test
    void rejectRegistrationFailsWhenAuditFails() {

        mockValidConditions();

        auditResult = 0;

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectRegistration(
                        1L,
                        "De tai khong hop le"
                )
        );

        verify(jdbc).update(
                argThat(sql ->
                        sql.contains("INSERT INTO audit_logs")
                ),
                eq(12L),
                eq("REGISTRATION_REJECTED"),
                eq("TOPIC_REGISTRATION"),
                eq(1L),
                anyString()
        );
    }
}
