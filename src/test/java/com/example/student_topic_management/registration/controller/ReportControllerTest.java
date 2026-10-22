package com.example.student_topic_management.registration.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.example.student_topic_management.common.config.GlobalControllerAdvice;
import com.example.student_topic_management.registration.repository.ReportRepository.ReportInfo;
import com.example.student_topic_management.registration.service.ReportService;

class ReportControllerTest {

    private StubReportService reportService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reportService = new StubReportService();
        mvc = MockMvcBuilders
                .standaloneSetup(new ReportController(reportService))
                .build();
    }

    @Test
    void validRegistrationShowsReportPage() throws Exception {
        mvc.perform(get("/reports/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/list"))
                .andExpect(model().attribute("registrationId", 42L))
                .andExpect(model().attribute("canUpload", false));
    }

    @Test
    void invalidRegistrationIdRedirectsInsteadOfReturningBadRequest() throws Exception {
        mvc.perform(get("/reports/not-a-number"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups"))
                .andExpect(flash().attribute(
                        "error",
                        "Mã hồ sơ hoặc mã báo cáo không hợp lệ"));
    }

    @Test
    void missingFileReturnsFriendlyMessage() throws Exception {
        reportService.uploadError =
                new IllegalArgumentException("Vui lòng chọn file PDF");

        mvc.perform(multipart("/reports/42/upload"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/42"))
                .andExpect(flash().attribute("error", is("Vui lòng chọn file PDF")));

        assertNull(reportService.uploadedFile);
    }

    @Test
    void oversizedFileReturnsToCurrentReportPage() {
        GlobalControllerAdvice advice = new GlobalControllerAdvice(null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        request.setRequestURI("/reports/42/upload");

        String view = advice.handleOversizedReport(request, redirect);

        assertEquals("redirect:/reports/42", view);
        assertEquals(
                "File PDF không được vượt quá 10 MB",
                redirect.getFlashAttributes().get("error")
        );
    }

    private static final class StubReportService extends ReportService {
        private RuntimeException uploadError;
        private MultipartFile uploadedFile;

        private StubReportService() {
            super(null, null, null, noOpTransactionManager(),
                    Clock.systemUTC(), "uploads/reports-test");
        }

        @Override
        public List<ReportInfo> listReports(Long registrationId) {
            return List.of();
        }

        @Override
        public boolean canUpload(Long registrationId) {
            return false;
        }

        @Override
        public UploadResult upload(Long registrationId, MultipartFile file) {
            uploadedFile = file;

            if (uploadError != null) {
                throw uploadError;
            }

            return new UploadResult(registrationId, 1, "report.pdf", 1, "hash");
        }

        private static PlatformTransactionManager noOpTransactionManager() {
            return new PlatformTransactionManager() {
                @Override
                public TransactionStatus getTransaction(TransactionDefinition definition) {
                    return new SimpleTransactionStatus();
                }

                @Override
                public void commit(TransactionStatus status) {
                }

                @Override
                public void rollback(TransactionStatus status) {
                }
            };
        }
    }
}
