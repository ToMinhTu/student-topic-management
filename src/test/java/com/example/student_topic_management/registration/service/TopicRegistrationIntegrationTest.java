
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("c04test")
@WithMockUser(
        username = "student.c04.01",
        roles = "STUDENT"
)
class TopicRegistrationIntegrationTest {

    private static final String TEST_DATABASE =
            "student_topic_management_c04_test";

    private static final Long REGISTRATION_ID = 1L;
    private static final Long GROUP_ID = 1L;
    private static final Long TOPIC_ID = 1L;
    private static final Long STUDENT_ID = 2L;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TopicRegistrationService service;

    @Autowired
    private org.springframework.transaction.PlatformTransactionManager
            transactionManager;

    // =====================================================
    // KIEM TRA AN TOAN DATABASE
    // =====================================================

    private void requireTestDatabase() {

        String database = jdbc.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals(
                TEST_DATABASE,
                database,
                "SAI DATABASE! DUNG KIEM THU NGAY."
        );

        try (var connection = dataSource.getConnection()) {

            assertEquals(
                    TEST_DATABASE,
                    connection.getCatalog(),
                    "DataSource khong tro den C04 TEST."
            );

        } catch (Exception ex) {
            fail("Khong kiem tra duoc DataSource", ex);
        }
    }

    // =====================================================
    // HAM DOC DU LIEU
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

    private Long registrationTopicId() {

        return jdbc.queryForObject(
                """
                SELECT topic_id
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                REGISTRATION_ID
        );
    }

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

    private Long decidedBy() {

        return jdbc.queryForObject(
                """
                SELECT decided_by
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                REGISTRATION_ID
        );
    }

    private String decisionNote() {

        return jdbc.queryForObject(
                """
                SELECT decision_note
                FROM topic_registrations
                WHERE id = ?
                """,
                String.class,
                REGISTRATION_ID
        );
    }

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

    private Integer auditCount() {

        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM audit_logs
                WHERE entity_type = 'TOPIC_REGISTRATION'
                  AND entity_id = ?
                  AND action = 'REGISTRATION_RESUBMITTED'
                """,
                Integer.class,
                REGISTRATION_ID
        );
    }

    // =====================================================
    // KIEM TRA DU LIEU BAN DAU
    // =====================================================

    @BeforeEach
    void setUp() {

        requireTestDatabase();

        assertEquals(
                "REJECTED",
                registrationStatus(),
                "Ho so 1 phai dang REJECTED."
        );

        assertEquals(
                TOPIC_ID,
                registrationTopicId()
        );

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

        assertEquals(STUDENT_ID, leaderId);

        Integer dependentCount = jdbc.queryForObject(
                """
                SELECT
                    (SELECT COUNT(*)
                     FROM reports
                     WHERE registration_id = ?)
                  + (SELECT COUNT(*)
                     FROM defenses
                     WHERE registration_id = ?)
                  + (SELECT COUNT(*)
                     FROM reviewer_assignments
                     WHERE registration_id = ?)
                  + (SELECT COUNT(*)
                     FROM results
                     WHERE registration_id = ?)
                """,
                Integer.class,
                REGISTRATION_ID,
                REGISTRATION_ID,
                REGISTRATION_ID,
                REGISTRATION_ID
        );

        assertEquals(
                0,
                dependentCount,
                "Ho so co du lieu phu thuoc."
        );

        assertEquals(
                List.of(
                        new MemberSnapshot(2L, 1),
                        new MemberSnapshot(3L, 2)
                ),
                snapshot()
        );
    }

    // =====================================================
    // TEST 1 - DANG KY LAI THANH CONG
    //
    // Test transaction se rollback sau khi ket thuc.
    // =====================================================

    @Test
    @Transactional
    @Rollback
    void rejectedRegistrationCanBeResubmitted() {

        requireTestDatabase();

        int beforeAudit = auditCount();

        service.registerTopic(GROUP_ID, TOPIC_ID);

        // Ho so cu duoc cap nhat thanh PENDING
        assertEquals(
                "PENDING",
                registrationStatus()
        );

        // Van giu nguyen registration ID
        Integer totalRegistrations = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM topic_registrations
                WHERE group_id = ?
                  AND id = ?
                """,
                Integer.class,
                GROUP_ID,
                REGISTRATION_ID
        );

        assertEquals(1, totalRegistrations);

        // De tai duoc giu cho lai
        assertEquals(
                TOPIC_ID,
                reservedTopicId()
        );

        // Thong tin xu ly cu duoc xoa
        assertNull(decidedBy());
        assertNull(decisionNote());

        // Snapshot duoc lam moi dung 2 thanh vien
        assertEquals(
                List.of(
                        new MemberSnapshot(2L, 1),
                        new MemberSnapshot(3L, 2)
                ),
                snapshot()
        );

        // Audit duoc ghi them 1 lan
        assertEquals(
                beforeAudit + 1,
                auditCount()
        );
    }

    // =====================================================
    // TEST 2 - ROLLBACK KHI TRANSACTION THAT BAI
    //
    // Chu dong gay exception sau khi dang ky lai
    // de xac minh transaction duoc rollback.
    // =====================================================

    @Test
    void rollbackWhenTransactionFails() {

        requireTestDatabase();

        int beforeAudit = auditCount();

        List<MemberSnapshot> beforeSnapshot = snapshot();

        Long beforeReservedTopic = reservedTopicId();

        Long beforeDecidedBy = decidedBy();

        String beforeDecisionNote = decisionNote();

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        transaction.setPropagationBehavior(
                Propagation.REQUIRES_NEW.value()
        );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> transaction.execute(status -> {

                    service.registerTopic(
                            GROUP_ID,
                            TOPIC_ID
                    );

                    // Xac nhan da update trong transaction
                    assertEquals(
                            "PENDING",
                            registrationStatus()
                    );

                    // Gia lap loi de buoc rollback
                    throw new IllegalStateException(
                            "C04_TEST_FORCE_ROLLBACK"
                    );
                })
        );

        assertEquals(
                "C04_TEST_FORCE_ROLLBACK",
                ex.getMessage()
        );

        // Du lieu van nhu truoc khi test
        assertEquals(
                "REJECTED",
                registrationStatus()
        );

        assertEquals(
                TOPIC_ID,
                registrationTopicId()
        );

        assertEquals(
                beforeReservedTopic,
                reservedTopicId()
        );

        assertEquals(
                beforeDecidedBy,
                decidedBy()
        );

        assertEquals(
                beforeDecisionNote,
                decisionNote()
        );

        assertEquals(
                beforeSnapshot,
                snapshot()
        );

        assertEquals(
                beforeAudit,
                auditCount()
        );
    }
}
