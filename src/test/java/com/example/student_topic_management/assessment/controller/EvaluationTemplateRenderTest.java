package com.example.student_topic_management.assessment.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.example.student_topic_management.assessment.entity.Evaluation;
import com.example.student_topic_management.assessment.service.EvaluationService;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tests layout composition and bindings without opening a database. */
class EvaluationTemplateRenderTest {
    private final EvaluationService service = mock(EvaluationService.class);
    private MockMvc mvc;
    private Evaluation evaluation;

    @BeforeEach
    void setup() {
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
        mvc = MockMvcBuilders.standaloneSetup(new EvaluationController(service))
                .setViewResolvers(views).build();
        evaluation = new Evaluation();
        ReflectionTestUtils.setField(evaluation, "id", 1L);
        evaluation.setAssignmentId(7L);
        evaluation.setScore(new BigDecimal("8.50"));
    }

    @Test
    void emptyListUsesSharedLayout() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        mvc.perform(get("/evaluations")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"wrapper\"")))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")))
                .andExpect(content().string(containsString("Chưa có phiếu chấm")));
    }

    @Test
    void listFormatsDateAndEscapesComments() throws Exception {
        evaluation.setComments("<script>alert(1)</script>");
        evaluation.setSubmittedAt(LocalDateTime.of(2026, 10, 22, 9, 30));
        when(service.findAll()).thenReturn(List.of(evaluation));
        mvc.perform(get("/evaluations")).andExpect(status().isOk())
                .andExpect(content().string(containsString("8.50 / 10")))
                .andExpect(content().string(containsString("22/10/2026 09:30")))
                .andExpect(content().string(containsString("href=\"/evaluations/1\"")))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void detailHandlesMissingCommentsAndSubmissionDate() throws Exception {
        when(service.findById(1L)).thenReturn(evaluation);
        mvc.perform(get("/evaluations/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"wrapper\"")))
                .andExpect(content().string(containsString("Chưa có nhận xét")))
                .andExpect(content().string(containsString("Chưa nộp")));
    }

    @Test
    void submittedDetailFormatsDate() throws Exception {
        evaluation.setSubmittedAt(LocalDateTime.of(2026, 10, 22, 9, 30));
        when(service.findById(1L)).thenReturn(evaluation);
        mvc.perform(get("/evaluations/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Đã nộp")))
                .andExpect(content().string(containsString("22/10/2026 09:30")));
    }

    @Test
    void formKeepsBindingsAndSaveAction() throws Exception {
        mvc.perform(get("/evaluations/new")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"wrapper\"")))
                .andExpect(content().string(containsString("action=\"/evaluations/save\"")))
                .andExpect(content().string(containsString("name=\"assignmentId\"")))
                .andExpect(content().string(containsString("name=\"score\"")))
                .andExpect(content().string(containsString("name=\"comments\"")));
    }

    @Test
    void invalidFormShowsErrorsAndPreservesInput() throws Exception {
        mvc.perform(post("/evaluations/save").param("score", "11").param("comments", "Giữ nhận xét"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("evaluationForm", "assignmentId", "score"))
                .andExpect(content().string(containsString("is-invalid")))
                .andExpect(content().string(containsString("Mã phân công không được để trống")))
                .andExpect(content().string(containsString("Điểm lớn nhất là 10")))
                .andExpect(result -> org.hamcrest.MatcherAssert.assertThat(
                        org.springframework.web.util.HtmlUtils.htmlUnescape(
                                result.getResponse().getContentAsString()),
                        containsString("Giữ nhận xét")));
        verify(service, never()).saveEvaluation(any());
    }
}
