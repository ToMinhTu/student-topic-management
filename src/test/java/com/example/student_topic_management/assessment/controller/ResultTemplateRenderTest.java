package com.example.student_topic_management.assessment.controller;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.example.student_topic_management.assessment.repository.EvaluationRepository;
import com.example.student_topic_management.assessment.repository.ResultRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;
import com.example.student_topic_management.assessment.service.ResultService;
import com.example.student_topic_management.assessment.service.ResultService.ResultView;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real controller, read service and templates; SQL rows are mocked, no database is opened. */
class ResultTemplateRenderTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ResultRepository results = mock(ResultRepository.class);
    private final ResultService service = new ResultService(results,
            mock(ReviewerAssignmentRepository.class), mock(EvaluationRepository.class), jdbc);
    private final ResultSet row = mock(ResultSet.class);
    private MockMvc mvc;

    @BeforeEach
    void setup() throws Exception {
        when(row.getLong("id")).thenReturn(1L);
        when(row.getLong("registration_id")).thenReturn(2L);
        when(row.getString("group_name")).thenReturn("Nhóm kiểm thử");
        when(row.getString("topic_code")).thenReturn("DT-001");
        when(row.getString("topic_title")).thenReturn("Đề tài kiểm thử");
        when(row.getBigDecimal("final_score")).thenReturn(new BigDecimal("8.33"));
        when(row.getInt("component_count")).thenReturn(3);
        when(row.getString("conclusion")).thenReturn("Đạt");
        when(row.getString("confirmer_name")).thenReturn("Chủ tịch kiểm thử");
        when(row.getTimestamp("confirmed_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 10, 21, 8, 0)));
        doAnswer(invocation -> {
            RowMapper<ResultView> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(row, 0));
        }).when(jdbc).query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<ResultView>>any(),
                any(Object[].class));

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
        mvc = MockMvcBuilders.standaloneSetup(new ResultController(service))
                .setViewResolvers(views).build();
    }

    @Test
    void rendersNonemptyListWithJoinedFields() throws Exception {
        mvc.perform(get("/results"))
                .andExpect(status().isOk())
                .andExpect(view().name("results/list"))
                .andExpect(content().string(containsString("Nhóm kiểm thử")))
                .andExpect(content().string(containsString("DT-001")))
                .andExpect(content().string(containsString("Đề tài kiểm thử")))
                .andExpect(content().string(containsString("8.33 / 10")));
        verifyNoInteractions(results);
    }

    @Test
    void rendersEmptyList() throws Exception {
        doReturn(List.of()).when(jdbc).query(anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<ResultView>>any(), any(Object[].class));
        mvc.perform(get("/results"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chưa có kết quả phù hợp")));
    }

    @Test
    void rendersUnpublishedDetailAndBindsRequestedId() throws Exception {
        mvc.perform(get("/results/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("results/detail"))
                .andExpect(content().string(containsString("Nhóm kiểm thử")))
                .andExpect(content().string(containsString("Chủ tịch kiểm thử")))
                .andExpect(content().string(containsString("Chưa công bố")));
        verify(jdbc).query(contains("WHERE r.id = ?"),
                org.mockito.ArgumentMatchers.<RowMapper<ResultView>>any(), eq(1L));
        verifyNoInteractions(results);
    }

    @Test
    void rendersPublishedDetailWithDate() throws Exception {
        when(row.getTimestamp("published_at"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.of(2026, 10, 22, 9, 30)));
        mvc.perform(get("/results/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Đã công bố")))
                .andExpect(content().string(containsString("22/10/2026 09:30")));
    }

    @Test
    void missingResultKeepsExistingNotFoundBehavior() {
        doReturn(List.of()).when(jdbc).query(anyString(),
                org.mockito.ArgumentMatchers.<RowMapper<ResultView>>any(), any(Object[].class));
        RuntimeException error = assertThrows(RuntimeException.class, () -> service.findById(999L));
        assertEquals("Không tìm thấy kết quả", error.getMessage());
    }
}
