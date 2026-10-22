package com.example.student_topic_management.assessment.controller;

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
import com.example.student_topic_management.assessment.entity.Defense;
import com.example.student_topic_management.assessment.service.DefenseService;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Uses real controller and templates, without a database or application startup. */
class DefenseTemplateRenderTest {
    private final DefenseService service = mock(DefenseService.class);
    private MockMvc mvc;
    private Defense defense;

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
        mvc = MockMvcBuilders.standaloneSetup(new DefenseController(service))
                .setViewResolvers(views).build();
        defense = new Defense();
        ReflectionTestUtils.setField(defense, "id", 1L);
        defense.setRegistrationId(2L);
        defense.setRoundId(3L);
        defense.setCouncilId(4L);
        defense.setScheduledAt(LocalDateTime.of(2026, 11, 1, 9, 30));
        defense.setLocation("Phòng A101");
        defense.setStatus("SCHEDULED");
    }

    @Test
    void populatedListRendersThroughEndOfLayout() throws Exception {
        when(service.findAll()).thenReturn(List.of(defense));
        mvc.perform(get("/defenses")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Phòng A101")))
                .andExpect(content().string(containsString("01/11/2026 09:30")))
                .andExpect(content().string(containsString("href=\"/defenses/1\"")))
                .andExpect(content().string(not(containsString("action=\"/defenses/1/complete\""))))
                .andExpect(content().string(containsString("DOMContentLoaded")))
                .andExpect(content().string(containsString("</html>")));
    }

    @Test
    void emptyListRenders() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        mvc.perform(get("/defenses")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Chưa có lịch bảo vệ")));
    }

    @Test
    void detailRendersInSharedLayout() throws Exception {
        when(service.findById(1L)).thenReturn(defense);
        mvc.perform(get("/defenses/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"wrapper\"")))
                .andExpect(content().string(containsString("Phòng A101")));
    }

    @Test
    void createFormRenders() throws Exception {
        mvc.perform(get("/defenses/new")).andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/defenses/save\"")));
    }

    @Test
    void invalidFormRendersValidationErrors() throws Exception {
        mvc.perform(post("/defenses/save")).andExpect(status().isOk())
                .andExpect(view().name("defenses/form"))
                .andExpect(model().attributeHasErrors("defenseForm"));
        verify(service, never()).saveDefense(any());
    }
}
