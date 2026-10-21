
package com.example.student_topic_management.registration.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.registration.service.ReportService;
import org.springframework.security.access.AccessDeniedException;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('STUDENT')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // C05.1 - Trang danh sach bao cao
    @GetMapping("/{registrationId}")
    public String listReports(
            @PathVariable Long registrationId,
            Model model) {

        model.addAttribute(
                "registrationId",
                registrationId
        );

        model.addAttribute(
                "reports",
                reportService.listReports(registrationId)
        );

        model.addAttribute(
                "canUpload",
                reportService.canUpload(registrationId)
        );

        return "reports/list";
    }

    // C05.2 - Upload PDF
    @PostMapping("/{registrationId}/upload")
    public String uploadReport(
            @PathVariable Long registrationId,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {
            var result = reportService.upload(
                    registrationId,
                    file
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Nop bao cao phien ban "
                            + result.versionNo()
                            + " thanh cong!"
            );

        } catch (IllegalArgumentException
            | IllegalStateException
            | AccessDeniedException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/reports/" + registrationId;
    }

    // C05.3 - Download PDF
    @GetMapping("/files/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long reportId) {

        var result = reportService.download(reportId);

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(
                                result.fileName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store"
                )
                .body(result.content());
    }
}
