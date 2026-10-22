package com.example.student_topic_management.assessment.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.example.student_topic_management.assessment.dto.ReviewerAssignmentForm;
import com.example.student_topic_management.assessment.entity.ReviewerAssignment;
import com.example.student_topic_management.assessment.service.ReviewerAssignmentService;
import com.example.student_topic_management.assessment.service.ReviewerAssignmentService.AssignmentRow;
import com.example.student_topic_management.assessment.service.ReviewerAssignmentService.LecturerOption;
import com.example.student_topic_management.assessment.service.ReviewerAssignmentService.RegistrationOption;

class ReviewerAssignmentControllerTest {

    private StubAssignmentService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        engine.addDialect(new nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect());

        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");

        service = new StubAssignmentService();
        mvc = MockMvcBuilders
                .standaloneSetup(new ReviewerAssignmentController(service))
                .setViewResolvers(views)
                .build();
    }

    @Test
    void listRendersAssignmentsAndCouncilSync() throws Exception {
        mvc.perform(get("/dean/assignments"))
                .andExpect(status().isOk())
                .andExpect(view().name("assignments/list"))
                .andExpect(content().string(containsString("Phân công chấm điểm")))
                .andExpect(content().string(containsString("GV001")))
                .andExpect(content().string(containsString("Đồng bộ hội đồng")));
    }

    @Test
    void createFormRendersSelectOptions() throws Exception {
        mvc.perform(get("/dean/assignments/new")
                        .param("registrationId", "42"))
                .andExpect(status().isOk())
                .andExpect(view().name("assignments/form"))
                .andExpect(content().string(containsString("Nhóm 01")))
                .andExpect(content().string(containsString("Giảng viên Một")));
    }

    @Test
    void validAssignmentRedirectsToList() throws Exception {
        mvc.perform(post("/dean/assignments")
                        .param("registrationId", "42")
                        .param("lecturerId", "7")
                        .param("assignmentType", "REVIEWER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dean/assignments"))
                .andExpect(flash().attribute(
                        "success", "Phân công chấm điểm thành công"));

        assertNotNull(service.createdForm);
        assertEquals(42L, service.createdForm.getRegistrationId());
    }

    @Test
    void invalidAssignmentTypeReturnsFormError() throws Exception {
        mvc.perform(post("/dean/assignments")
                        .param("registrationId", "42")
                        .param("lecturerId", "7")
                        .param("assignmentType", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(view().name("assignments/form"))
                .andExpect(model().attributeHasFieldErrors(
                        "assignmentForm", "assignmentType"));
    }

    @Test
    void councilSyncReportsCreatedCount() throws Exception {
        mvc.perform(post("/dean/assignments/sync-council")
                        .param("registrationId", "42"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dean/assignments"))
                .andExpect(flash().attribute(
                        "success", "Đã tạo 3 phân công cho thành viên hội đồng"));
    }

    private static final class StubAssignmentService
            extends ReviewerAssignmentService {

        private ReviewerAssignmentForm createdForm;

        private StubAssignmentService() {
            super(null, null, null, null);
        }

        @Override
        public List<AssignmentRow> findAll() {
            return List.of(new AssignmentRow(
                    100L,
                    42L,
                    "Nhóm 01",
                    "DT-01",
                    "Đề tài kiểm thử",
                    "Đợt 01",
                    7L,
                    "GV001",
                    "Giảng viên Một",
                    "REVIEWER",
                    LocalDateTime.of(2026, 10, 22, 9, 0),
                    false
            ));
        }

        @Override
        public List<RegistrationOption> findEligibleRegistrations() {
            return List.of(new RegistrationOption(
                    42L,
                    "Nhóm 01",
                    "DT-01",
                    "Đề tài kiểm thử",
                    "Đợt 01",
                    "Hội đồng 01"
            ));
        }

        @Override
        public List<LecturerOption> findActiveLecturers() {
            return List.of(new LecturerOption(
                    7L,
                    "GV001",
                    "Giảng viên Một"
            ));
        }

        @Override
        public ReviewerAssignment create(ReviewerAssignmentForm form) {
            createdForm = form;
            return new ReviewerAssignment();
        }

        @Override
        public int assignCouncilMembers(Long registrationId) {
            return 3;
        }
    }
}
