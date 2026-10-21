
package com.example.student_topic_management.registration.service;

import com.example.student_topic_management.registration.repository.ReportRepository;
import com.example.student_topic_management.registration.repository.ReportRepository.RegistrationInfo;
import com.example.student_topic_management.registration.repository.ReportRepository.ReportInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReportService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final ReportRepository repository;
    private final StudentAccess studentAccess;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private final Path storageDirectory;
    private final Clock clock;

    // Ket qua upload
    public record UploadResult(
            Long registrationId,
            int versionNo,
            String originalName,
            long sizeBytes,
            String sha256
    ) {}

    // Ket qua download
    public record DownloadResult(
            String fileName,
            byte[] content
    ) {}

    public ReportService(
            ReportRepository repository,
            StudentAccess studentAccess,
            JdbcTemplate jdbc,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${app.reports.storage-dir:uploads/reports}")
            String storageDir) {

        this.repository = repository;
        this.studentAccess = studentAccess;
        this.jdbc = jdbc;
        this.transactionTemplate =
                new TransactionTemplate(transactionManager);
        this.clock = clock;

        this.storageDirectory = Path.of(storageDir)
                .toAbsolutePath()
                .normalize();
    }

    // =====================================================
    // C05.1 - UPLOAD BAO CAO PDF
    // =====================================================

    public UploadResult upload(
            Long registrationId,
            MultipartFile file) {

        requirePositiveId(registrationId, "Ma ho so dang ky");

        Long studentId =
                studentAccess.requireCurrentStudentId();

        byte[] content = validatePdf(file);

        String originalName =
                sanitizeFileName(file.getOriginalFilename());

        String storageKey = UUID.randomUUID() + ".pdf";

        Path destination = storageDirectory
                .resolve(storageKey)
                .normalize();

        if (!destination.startsWith(storageDirectory)) {
            throw new IllegalStateException(
                    "Duong dan luu file khong hop le");
        }

        String sha256 = calculateSha256(content);

        // Ghi file tam truoc khi luu metadata.
        // Neu transaction that bai, se xoa file.
        try {
            Files.createDirectories(storageDirectory);

            if (Files.isSymbolicLink(storageDirectory)) {
                throw new IllegalStateException(
                        "Thu muc luu tru khong hop le");
            }

            Files.write(
                    destination,
                    content,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
            );

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Khong the luu file bao cao", ex);
        }

        try {
            return transactionTemplate.execute(status -> {

                // Khoa ho so dang ky de tranh trung version.
                List<Long> lockedIds = jdbc.queryForList(
                        """
                        SELECT id
                        FROM topic_registrations
                        WHERE id = ?
                        FOR UPDATE
                        """,
                        Long.class,
                        registrationId
                );

                if (lockedIds.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Ho so dang ky khong ton tai");
                }

                RegistrationInfo registration = repository
                        .findRegistration(registrationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ho so dang ky khong ton tai"));

                // Ho so phai duoc duyet
                if (!"APPROVED".equals(
                        registration.registrationStatus())) {

                    throw new IllegalStateException(
                            "Ho so dang ky chua duoc duyet");
                }

                // Nhom phai LOCKED
                if (!"LOCKED".equals(
                        registration.groupStatus())) {

                    throw new IllegalStateException(
                            "Nhom chua duoc khoa");
                }

                // Chi truong nhom duoc nop
                if (!repository.isActiveLeader(
                        registration.groupId(),
                        studentId)) {

                    throw new AccessDeniedException(
                            "Chi truong nhom duoc nop bao cao");
                }

                // Kiem tra han nop theo UTC
                LocalDateTime nowUtc =
                        LocalDateTime.ofInstant(
                                clock.instant(),
                                ZoneOffset.UTC
                        );

                LocalDateTime dueAt =
                        registration.reportDueAt();

                // NULL: chua thiet lap han nop
                if (dueAt != null && nowUtc.isAfter(dueAt)) {
                    throw new IllegalStateException(
                            "Da qua han nop bao cao");
                }

                // Lay version hien tai
                int latestVersion =
                        repository.findLatestVersion(registrationId);

                if (latestVersion == Integer.MAX_VALUE) {
                    throw new IllegalStateException(
                            "Da dat gioi han phien ban");
                }

                int newVersion = latestVersion + 1;

                // Ghi metadata vao reports
                int inserted = repository.insertReport(
                        registrationId,
                        newVersion,
                        storageKey,
                        originalName,
                        "application/pdf",
                        content.length,
                        sha256,
                        studentId
                );

                if (inserted != 1) {
                    throw new IllegalStateException(
                            "Khong the luu thong tin bao cao");
                }

                return new UploadResult(
                        registrationId,
                        newVersion,
                        originalName,
                        content.length,
                        sha256
                );
            });

        } catch (RuntimeException ex) {

            // Xoa file neu giao dich DB bi loi.
            try {
                Files.deleteIfExists(destination);
            } catch (IOException cleanupError) {
                ex.addSuppressed(cleanupError);
            }

            throw ex;
        }
    }

    // =====================================================
    // C05.2 - KIEM TRA QUYEN XEM BAO CAO
    // =====================================================

    private void requireReportAccess(Long registrationId) {

        requirePositiveId(registrationId, "Ma ho so dang ky");

        Long studentId =
                studentAccess.requireCurrentStudentId();

        RegistrationInfo registration = repository
                .findRegistration(registrationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ho so dang ky khong ton tai"));

        boolean isMember = repository.isActiveMember(
                registration.groupId(),
                studentId
        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "Ban khong co quyen xem bao cao cua nhom nay");
        }
    }
    // C05 - Kiem tra nguoi dang nhap co phai truong nhom
    public boolean canUpload(Long registrationId) {

        // Dam bao sinh vien co quyen xem bao cao
        requireReportAccess(registrationId);

        Long studentId =
                studentAccess.requireCurrentStudentId();

        RegistrationInfo registration = repository
                .findRegistration(registrationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ho so dang ky khong ton tai"));

        return "APPROVED".equals(registration.registrationStatus())
                && "LOCKED".equals(registration.groupStatus())
                && repository.isActiveLeader(
                        registration.groupId(),
                        studentId);
    }
    // =====================================================
    // C05.3 - DANH SACH PHIEN BAN BAO CAO
    // =====================================================

    public List<ReportInfo> listReports(Long registrationId) {

        requireReportAccess(registrationId);

        return repository.findByRegistrationId(
                registrationId
        );
    }

    // =====================================================
    // C05.4 - DOWNLOAD BAO CAO PDF
    // =====================================================

    public DownloadResult download(Long reportId) {

        requirePositiveId(reportId, "Ma bao cao");

        ReportInfo report = repository.findById(reportId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bao cao khong ton tai"));

        // Bat buoc kiem tra quyen truoc khi doc file
        requireReportAccess(report.registrationId());

        // storageKey chi duoc la UUID.pdf
        if (report.storageKey() == null
                || !report.storageKey().matches(
                        "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.pdf")) {

            throw new IllegalStateException(
                    "Khoa luu tru khong hop le");
        }

        Path filePath = storageDirectory
                .resolve(report.storageKey())
                .normalize();

        if (!filePath.startsWith(storageDirectory)) {
            throw new IllegalStateException(
                    "Duong dan file khong hop le");
        }

        try {
            if (!Files.isRegularFile(
                    filePath, LinkOption.NOFOLLOW_LINKS)
                    || Files.isSymbolicLink(filePath)) {

                throw new IllegalStateException(
                        "File bao cao khong ton tai");
            }

            byte[] content = Files.readAllBytes(filePath);

            // Kiem tra dung luong
            if (content.length != report.sizeBytes()) {
                throw new IllegalStateException(
                        "Dung luong file bao cao khong khop");
            }

            // Kiem tra SHA-256
            String currentHash =
                    calculateSha256(content);

            if (!MessageDigest.isEqual(
                    currentHash.getBytes(StandardCharsets.US_ASCII),
                    report.sha256().toLowerCase(Locale.ROOT)
                            .getBytes(StandardCharsets.US_ASCII))) {

                throw new IllegalStateException(
                        "File bao cao da bi thay doi");
            }

            return new DownloadResult(
                    report.originalName(),
                    content
            );

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Khong the doc file bao cao", ex);
        }
    }

    // =====================================================
    // C05.5 - KIEM TRA FILE PDF
    // =====================================================

    private byte[] validatePdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui long chon file PDF");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File PDF khong duoc vuot qua 10 MB");
        }

        String name = file.getOriginalFilename();

        if (name == null
                || !name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {

            throw new IllegalArgumentException(
                    "Chi chap nhan file co duoi .pdf");
        }

        byte[] content;

        try {
            content = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Khong the doc file PDF", ex);
        }

        if (content.length == 0
                || content.length > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Dung luong file PDF khong hop le");
        }

        // Header PDF
        String header = new String(
                content,
                0,
                Math.min(content.length, 5),
                StandardCharsets.US_ASCII
        );

        if (!"%PDF-".equals(header)) {
            throw new IllegalArgumentException(
                    "Noi dung file khong phai PDF hop le");
        }

        // EOF marker
        int tailLength = Math.min(content.length, 1024);

        String tail = new String(
                content,
                content.length - tailLength,
                tailLength,
                StandardCharsets.ISO_8859_1
        );

        if (!tail.contains("%%EOF")) {
            throw new IllegalArgumentException(
                    "File PDF thieu dau ket thuc");
        }

        return content;
    }

    // =====================================================
    // C05.6 - LAM SACH TEN FILE
    // =====================================================

    private String sanitizeFileName(String originalName) {

        String safeName = originalName == null
                ? "report.pdf"
                : originalName.replace('\\', '/');

        safeName = safeName.substring(
                safeName.lastIndexOf('/') + 1
        );

        safeName = safeName
                .replaceAll("[\\p{Cntrl}]", "_")
                .strip();

        if (safeName.isEmpty()) {
            safeName = "report.pdf";
        }

        if (safeName.length() > 255) {
            safeName = safeName.substring(0, 251)
                    + ".pdf";
        }

        return safeName;
    }

    // =====================================================
    // C05.7 - TINH SHA-256
    // =====================================================

    private String calculateSha256(byte[] content) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(content);

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "He thong khong ho tro SHA-256", ex);
        }
    }

    // =====================================================
    // C05.8 - KIEM TRA ID
    // =====================================================

    private void requirePositiveId(Long id, String fieldName) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " khong hop le");
        }
    }
}
