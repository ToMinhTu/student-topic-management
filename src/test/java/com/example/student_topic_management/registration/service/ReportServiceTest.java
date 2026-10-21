
package com.example.student_topic_management.registration.service;

import com.example.student_topic_management.registration.repository.ReportRepository;
import com.example.student_topic_management.registration.repository.ReportRepository.RegistrationInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private ReportRepository repository;
    private StudentAccess studentAccess;
    private JdbcTemplate jdbc;
    private PlatformTransactionManager transactionManager;
    private ReportService reportService;

    @TempDir
    Path tempDir;

    private static final Long REGISTRATION_ID = 1L;
    private static final Long GROUP_ID = 10L;
    private static final Long STUDENT_ID = 20L;

    @BeforeEach
    void setUp() {

        repository = mock(ReportRepository.class);
        studentAccess = mock(StudentAccess.class);
        jdbc = mock(JdbcTemplate.class);

        transactionManager =
                mock(PlatformTransactionManager.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-10-21T10:00:00Z"),
                ZoneOffset.UTC
        );

        reportService = new ReportService(
                repository,
                studentAccess,
                jdbc,
                transactionManager,
                clock,
                tempDir.toString()
        );
    }

    private MockMultipartFile validPdf() {

        byte[] content = (
                "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<<>>\n" +
                "endobj\n" +
                "trailer\n" +
                "<<>>\n" +
                "%%EOF\n"
        ).getBytes(StandardCharsets.US_ASCII);

        return new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                content
        );
    }

    // TEST 1 - File không phải PDF bị từ chối
    @Test
    void upload_rejectsNonPdfFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.txt",
                "text/plain",
                "Hello".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.upload(REGISTRATION_ID, file)
        );

        verify(repository, never()).insertReport(
                any(), anyInt(), anyString(),
                anyString(), anyString(),
                anyLong(), anyString(), any()
        );
    }

    // TEST 2 - File vượt quá 10 MB bị từ chối
    @Test
    void upload_rejectsFileOver10MB() {

        byte[] content = new byte[10 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                content
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.upload(REGISTRATION_ID, file)
        );

        verify(repository, never()).insertReport(
                any(), anyInt(), anyString(),
                anyString(), anyString(),
                anyLong(), anyString(), any()
        );
    }

    // TEST 3 - PDF không có header hợp lệ
    @Test
    void upload_rejectsInvalidPdfContent() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.pdf",
                "application/pdf",
                "Not a real PDF".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.upload(REGISTRATION_ID, file)
        );
    }

    // TEST 4 - Thành viên không có quyền upload
    @Test
    void upload_rejectsRegularMember() throws Exception {

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(STUDENT_ID);

        when(transactionManager.getTransaction(
                any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());

        when(jdbc.queryForList(
                anyString(),
                eq(Long.class),
                eq(REGISTRATION_ID)
        )).thenReturn(java.util.List.of(REGISTRATION_ID));

        RegistrationInfo registration =
                mock(RegistrationInfo.class);

        when(repository.findRegistration(REGISTRATION_ID))
                .thenReturn(Optional.of(registration));

        when(registration.registrationStatus())
                .thenReturn("APPROVED");

        when(registration.groupStatus())
                .thenReturn("LOCKED");

        when(registration.groupId())
                .thenReturn(GROUP_ID);

        when(repository.isActiveLeader(
                GROUP_ID,
                STUDENT_ID
        )).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> reportService.upload(
                        REGISTRATION_ID,
                        validPdf()
                )
        );

        verify(repository, never()).insertReport(
                any(), anyInt(), anyString(),
                anyString(), anyString(),
                anyLong(), anyString(), any()
        );

        // File tạm phải được xóa khi upload thất bại
        try (var files = Files.list(tempDir)) {
            assertEquals(0L, files.count());
        }
    }

    // TEST 5 - Trưởng nhóm được phép thấy chức năng upload
    @Test
    void canUpload_returnsTrueForLeader() {

        when(studentAccess.requireCurrentStudentId())
                .thenReturn(STUDENT_ID);

        RegistrationInfo registration =
                mock(RegistrationInfo.class);

        when(repository.findRegistration(REGISTRATION_ID))
                .thenReturn(Optional.of(registration));

        when(registration.groupId())
                .thenReturn(GROUP_ID);

        when(registration.registrationStatus())
                .thenReturn("APPROVED");

        when(registration.groupStatus())
                .thenReturn("LOCKED");

        when(repository.isActiveMember(
                GROUP_ID,
                STUDENT_ID
        )).thenReturn(true);

        when(repository.isActiveLeader(
                GROUP_ID,
                STUDENT_ID
        )).thenReturn(true);

        assertTrue(
                reportService.canUpload(REGISTRATION_ID)
        );
    }
}
