
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("c04test")
@WithMockUser(
        username = "student.c04.01",
        roles = "STUDENT"
)
class TopicRegistrationCancelIntegrationTest {

    private static final String TEST_DATABASE =
            "student_topic_management_c04_test";

    private static final Long REGISTRATION_ID = 1L;
    private static final Long GROUP_ID = 1L;
    private static final Long TOPIC_ID = 1L;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TopicRegistrationService service;

    // =====================================================
    // DOC TRANG THAI HO SO
    // =====================================================

    private String registrationStatus() {

        return jdbc.queryForObject(
                """
                SELECT status
                FROM topic_registrations
                WHERE id = ?
                """,
                String.class,
                REGISTRATION_ID
        );
    }

    // =====================================================
    // DOC DE TAI DUOC GIU CHO
    // =====================================================

    private Long reservedTopicId() {

        return jdbc.queryForObject(
                """
                SELECT reserved_topic_id
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                REGISTRATION_ID
        );
    }

    // =====================================================
    // DOC SNAPSHOT THANH VIEN
    // =====================================================

    private record MemberSnapshot(
            Long studentId,
            int slotNo
    ) {}

    private List<MemberSnapshot> snapshot() {

        return jdbc.query(
                """
                SELECT student_id, slot_no
                FROM registration_members
                WHERE registration_id = ?
                ORDER BY slot_no
                """,
                (rs, rowNum) -> new MemberSnapshot(
                        rs.getLong("student_id"),
                        rs.getInt("slot_no")
                ),
                REGISTRATION_ID
        );
    }

    // =====================================================
    // DEM AUDIT THEO ACTION
    // =====================================================

    private int auditCount(String action) {

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM audit_logs
                WHERE entity_type = 'TOPIC_REGISTRATION'
                  AND entity_id = ?
                  AND action = ?
                """,
                Integer.class,
                REGISTRATION_ID,
                action
        );

        return count == null ? 0 : count;
    }

    // =====================================================
    // KIEM TRA AN TOAN DATABASE
    // =====================================================

    @BeforeEach
    void setUp() {

        String database = jdbc.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals(
                TEST_DATABASE,
                database,
                "SAI DATABASE! DUNG KIEM THU."
        );

        // Ho so 1 phai dang PENDING
        assertEquals(
                "PENDING",
                registrationStatus(),
                "Ho so 1 phai dang PENDING."
        );

        assertEquals(
                TOPIC_ID,
                reservedTopicId()
        );

        // Nhom phai dang FORMING
        String groupStatus = jdbc.queryForObject(
                """
                SELECT status
                FROM student_groups
                WHERE id = ?
                """,
                String.class,
                GROUP_ID
        );

        assertEquals("FORMING", groupStatus);

        // Kiem tra truong nhom
        Long leaderId = jdbc.queryForObject(
                """
                SELECT student_id
                FROM group_members
                WHERE group_id = ?
                  AND slot_no = 1
                  AND left_at IS NULL
                """,
                Long.class,
                GROUP_ID
        );

        assertEquals(2L, leaderId);

        // Kiem tra snapshot
        assertEquals(
                List.of(
                        new MemberSnapshot(2L, 1),
                        new MemberSnapshot(3L, 2)
                ),
                snapshot()
        );
    }

    // =====================================================
    // TEST 1: HUY DANG KY THANH CONG
    // =====================================================

    @Test
    @Transactional
    @Rollback
    void pendingRegistrationCanBeCancelled() {

        int beforeAudit =
                auditCount("REGISTRATION_CANCELLED");

        List<MemberSnapshot> beforeSnapshot =
                snapshot();

        service.cancelRegistration(REGISTRATION_ID);

        // PENDING -> CANCELLED
        assertEquals(
                "CANCELLED",
                registrationStatus()
        );

        // De tai duoc giai phong
        assertNull(reservedTopicId());

        // Snapshot van duoc giu nguyen
        assertEquals(
                beforeSnapshot,
                snapshot()
        );

        // Co them 1 audit
        assertEquals(
                beforeAudit + 1,
                auditCount("REGISTRATION_CANCELLED")
        );
    }

    // =====================================================
    // TEST 2: HUY ROI DANG KY LAI
    // =====================================================

    @Test
    @Transactional
    @Rollback
    void cancelledRegistrationCanBeResubmitted() {

        int beforeCancelAudit =
                auditCount("REGISTRATION_CANCELLED");

        int beforeResubmitAudit =
                auditCount("REGISTRATION_RESUBMITTED");

        // Buoc 1: Huy dang ky
        service.cancelRegistration(REGISTRATION_ID);

        assertEquals(
                "CANCELLED",
                registrationStatus()
        );

        assertNull(reservedTopicId());

        // Buoc 2: Dang ky lai
        service.registerTopic(
                GROUP_ID,
                TOPIC_ID
        );

        // CANCELLED -> PENDING
        assertEquals(
                "PENDING",
                registrationStatus()
        );

        // De tai duoc giu cho lai
        assertEquals(
                TOPIC_ID,
                reservedTopicId()
        );

        // Khong tao them ho so cho nhom
        Integer registrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM topic_registrations
                        WHERE group_id = ?
                        """,
                        Integer.class,
                        GROUP_ID
                );

        assertEquals(1, registrationCount);

        // Snapshot dung thanh vien
        assertEquals(
                List.of(
                        new MemberSnapshot(2L, 1),
                        new MemberSnapshot(3L, 2)
                ),
                snapshot()
        );

        // Co audit HUY va DANG KY LAI
        assertEquals(
                beforeCancelAudit + 1,
                auditCount("REGISTRATION_CANCELLED")
        );

        assertEquals(
                beforeResubmitAudit + 1,
                auditCount("REGISTRATION_RESUBMITTED")
        );
    }

    // =====================================================
    // TEST 3: KHONG DUOC HUY LAN HAI
    // =====================================================

    @Test
    @Transactional
    @Rollback
    void cancelledRegistrationCannotBeCancelledAgain() {

        int beforeAudit =
                auditCount("REGISTRATION_CANCELLED");

        service.cancelRegistration(REGISTRATION_ID);

        assertEquals(
                "CANCELLED",
                registrationStatus()
        );

        // Huy lan hai phai bi tu choi
        assertThrows(
                IllegalStateException.class,
                () -> service.cancelRegistration(
                        REGISTRATION_ID
                )
        );

        // Khong co audit huy lan hai
        assertEquals(
                beforeAudit + 1,
                auditCount("REGISTRATION_CANCELLED")
        );
    }
}
