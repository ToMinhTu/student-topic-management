
package com.example.student_topic_management.registration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("c04test")
@WithMockUser(
        username = "dean.c04.test",
        roles = "DEAN"
)
class DeanRegistrationIntegrationTest {

    private static final String TEST_DATABASE =
            "student_topic_management_c04_test";

    private static final String TEST_TOPIC =
            "C04-TEST-001";

    private static final String AUDIT_TRIGGER =
            "c04_test_reject_approval_audit";

    private static final String REJECT_AUDIT_TRIGGER =
            "c04_test_reject_audit_failure";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DeanRegistrationService service;

    private Long registrationId;
    private Long groupId;

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
                "SAI DATABASE! DUNG TEST NGAY."
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
    // CAC HAM DOC DU LIEU
    // =====================================================

    private String registrationStatus() {
        return jdbc.queryForObject(
                """
                SELECT status
                FROM topic_registrations
                WHERE id = ?
                """,
                String.class,
                registrationId
        );
    }

    private String groupStatus() {
        return jdbc.queryForObject(
                """
                SELECT status
                FROM student_groups
                WHERE id = ?
                """,
                String.class,
                groupId
        );
    }

    private Integer approvalAuditCount() {
        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM audit_logs
                WHERE entity_type = 'TOPIC_REGISTRATION'
                  AND entity_id = ?
                  AND action = 'REGISTRATION_APPROVED'
                """,
                Integer.class,
                registrationId
        );
    }

    private Integer rejectionAuditCount() {
        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM audit_logs
                WHERE entity_type = 'TOPIC_REGISTRATION'
                  AND entity_id = ?
                  AND action = 'REGISTRATION_REJECTED'
                """,
                Integer.class,
                registrationId
        );
    }

    // =====================================================
    // CHUAN BI DU LIEU TRUOC MOI TEST
    // =====================================================

    @BeforeEach
    void setUp() {

        requireTestDatabase();

        var rows = jdbc.query(
                """
                SELECT tr.id, tr.group_id
                FROM topic_registrations tr
                JOIN topics t ON t.id = tr.topic_id
                WHERE t.code = ?
                """,
                (rs, rowNum) -> new Long[]{
                        rs.getLong("id"),
                        rs.getLong("group_id")
                },
                TEST_TOPIC
        );

        assertEquals(
                1,
                rows.size(),
                "Phai co dung 1 ho so C04 test."
        );

        registrationId = rows.get(0)[0];
        groupId = rows.get(0)[1];

        assertNotNull(registrationId);
        assertNotNull(groupId);

        assertEquals("PENDING", registrationStatus());
        assertEquals("FORMING", groupStatus());

        assertEquals(0, approvalAuditCount());
        assertEquals(0, rejectionAuditCount());
    }

    // =====================================================
    // DON DU LIEU SAU MOI TEST
    // =====================================================

    @AfterEach
    void tearDown() {

        requireTestDatabase();

        jdbc.execute(
                "DROP TRIGGER IF EXISTS " + AUDIT_TRIGGER
        );

        jdbc.execute(
                "DROP TRIGGER IF EXISTS " + REJECT_AUDIT_TRIGGER
        );

        if (registrationId == null || groupId == null) {
            return;
        }

        jdbc.update(
                """
                DELETE FROM audit_logs
                WHERE entity_type = 'TOPIC_REGISTRATION'
                  AND entity_id = ?
                  AND action IN (
                      'REGISTRATION_APPROVED',
                      'REGISTRATION_REJECTED'
                  )
                """,
                registrationId
        );

        jdbc.update(
                """
                UPDATE topic_registrations
                SET status = 'PENDING',
                    decided_by = NULL,
                    decided_at = NULL,
                    decision_note = NULL
                WHERE id = ?
                """,
                registrationId
        );

        jdbc.update(
                """
                UPDATE student_groups
                SET status = 'FORMING'
                WHERE id = ?
                """,
                groupId
        );
    }

    // =====================================================
    // C04.2 - INTEGRATION TEST DUYET DANG KY
    // =====================================================

    // TEST 1: Duyet thanh cong
    @Test
    void approveSuccessfully() {

        service.approveRegistration(registrationId);

        assertEquals("APPROVED", registrationStatus());
        assertEquals("LOCKED", groupStatus());
        assertEquals(1, approvalAuditCount());

        Long decidedBy = jdbc.queryForObject(
                """
                SELECT decided_by
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                registrationId
        );

        Long deanId = jdbc.queryForObject(
                """
                SELECT id
                FROM users
                WHERE username = 'dean.c04.test'
                """,
                Long.class
        );

        assertEquals(deanId, decidedBy);
    }

    // TEST 2: Khong duoc duyet hai lan
    @Test
    void cannotApproveTwice() {

        service.approveRegistration(registrationId);

        assertThrows(
                IllegalStateException.class,
                () -> service.approveRegistration(
                        registrationId
                )
        );

        assertEquals("APPROVED", registrationStatus());
        assertEquals("LOCKED", groupStatus());
        assertEquals(1, approvalAuditCount());
    }

    // TEST 3: Rollback khi ghi audit duyet that bai
    @Test
    void rollbackWhenAuditInsertFails() {

        requireTestDatabase();

        jdbc.execute(
                """
                CREATE TRIGGER c04_test_reject_approval_audit
                BEFORE INSERT ON audit_logs
                FOR EACH ROW
                BEGIN
                    IF NEW.action = 'REGISTRATION_APPROVED' THEN
                        SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT =
                            'C04_TEST_AUDIT_FAILURE';
                    END IF;
                END
                """
        );

        assertThrows(
                RuntimeException.class,
                () -> service.approveRegistration(
                        registrationId
                )
        );

        assertEquals("PENDING", registrationStatus());
        assertEquals("FORMING", groupStatus());
        assertEquals(0, approvalAuditCount());
    }

    // =====================================================
    // C04.3 - INTEGRATION TEST TU CHOI DANG KY
    // =====================================================

    // TEST 4: Tu choi thanh cong
    @Test
    void rejectSuccessfully() {

        String reason = "De tai chua dap ung yeu cau";

        service.rejectRegistration(
                registrationId,
                reason
        );

        assertEquals("REJECTED", registrationStatus());
        assertEquals("FORMING", groupStatus());
        assertEquals(1, rejectionAuditCount());

        String decisionNote = jdbc.queryForObject(
                """
                SELECT decision_note
                FROM topic_registrations
                WHERE id = ?
                """,
                String.class,
                registrationId
        );

        assertEquals(reason, decisionNote);

        Long reservedTopicId = jdbc.queryForObject(
                """
                SELECT reserved_topic_id
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                registrationId
        );

        assertNull(reservedTopicId);

        Long decidedBy = jdbc.queryForObject(
                """
                SELECT decided_by
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                registrationId
        );

        Long deanId = jdbc.queryForObject(
                """
                SELECT id
                FROM users
                WHERE username = 'dean.c04.test'
                """,
                Long.class
        );

        assertEquals(deanId, decidedBy);
    }

    // TEST 5: Khong duoc tu choi hai lan
    @Test
    void cannotRejectTwice() {

        service.rejectRegistration(
                registrationId,
                "Khong dat yeu cau"
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.rejectRegistration(
                        registrationId,
                        "Tu choi lan hai"
                )
        );

        assertEquals("REJECTED", registrationStatus());
        assertEquals("FORMING", groupStatus());
        assertEquals(1, rejectionAuditCount());
    }

    // TEST 6: Rollback khi ghi audit tu choi that bai
    @Test
    void rejectRollbackWhenAuditFails() {

        requireTestDatabase();

        jdbc.execute(
                """
                CREATE TRIGGER c04_test_reject_audit_failure
                BEFORE INSERT ON audit_logs
                FOR EACH ROW
                BEGIN
                    IF NEW.action = 'REGISTRATION_REJECTED' THEN
                        SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT =
                            'C04_TEST_REJECT_AUDIT_FAILURE';
                    END IF;
                END
                """
        );

        assertThrows(
                RuntimeException.class,
                () -> service.rejectRegistration(
                        registrationId,
                        "Khong dat yeu cau"
                )
        );

        assertEquals("PENDING", registrationStatus());
        assertEquals("FORMING", groupStatus());
        assertEquals(0, rejectionAuditCount());

        Long reservedTopicId = jdbc.queryForObject(
                """
                SELECT reserved_topic_id
                FROM topic_registrations
                WHERE id = ?
                """,
                Long.class,
                registrationId
        );

        assertNotNull(reservedTopicId);
    }
}
