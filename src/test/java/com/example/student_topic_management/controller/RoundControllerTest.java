package com.example.student_topic_management.controller;

import com.example.student_topic_management.dto.RoundForm;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.RoundStatus;
import com.example.student_topic_management.enums.RoundType;
import com.example.student_topic_management.service.RoundService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

class RoundControllerTest {
    private final RoundService service = mock(RoundService.class);
    private MockMvc mvc;

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
        mvc = MockMvcBuilders.standaloneSetup(new RoundController(service))
                .setViewResolvers(views).build();
    }

    @Test
    void rendersEmptyList() throws Exception {
        when(service.getAllRounds(0)).thenReturn(new PageImpl<>(List.of()));
        mvc.perform(get("/dean/rounds")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Chưa có đợt đăng ký nào")));
    }

    @Test
    void rendersPopulatedListAndPagination() throws Exception {
        RegistrationRound round = new RegistrationRound();
        round.setName("Đợt thử nghiệm");
        round.setRoundType(RoundType.COURSE);
        round.setStatus(RoundStatus.DRAFT);
        when(service.getAllRounds(0)).thenReturn(new PageImpl<>(List.of(round), PageRequest.of(0, 10), 11));
        mvc.perform(get("/dean/rounds")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Đợt thử nghiệm")))
                .andExpect(content().string(containsString("Trang sau")));
    }

    @Test
    void rendersFormAndBindingErrors() throws Exception {
        mvc.perform(get("/dean/rounds/new")).andExpect(status().isOk());
        mvc.perform(post("/dean/rounds").param("name", "")
                        .param("proposalStartsAt", "bad-date").param("groupMinSize", "abc"))
                .andExpect(status().isOk()).andExpect(view().name("rounds/form"))
                .andExpect(model().attributeHasFieldErrors("roundForm", "name", "proposalStartsAt", "groupMinSize"));
        verify(service, never()).createRound(any());
    }

    @Test
    void rendersVietnamTimeAndEmptyOptionalDeadlines() throws Exception {
        RegistrationRound round = new RegistrationRound();
        round.setStatus(RoundStatus.DRAFT);
        RoundForm schedule = new RoundForm();
        schedule.setProposalStartsAt(LocalDateTime.of(2030, 1, 1, 8, 0));
        when(service.getRoundById(1L)).thenReturn(round);
        when(service.toDisplayForm(round)).thenReturn(schedule);
        mvc.perform(get("/dean/rounds/1")).andExpect(status().isOk())
                .andExpect(content().string(containsString("01/01/2030 08:00")));
    }

    @Test
    void deletesDraftThroughPost() throws Exception {
        mvc.perform(post("/dean/rounds/1/delete"))
                .andExpect(redirectedUrl("/dean/rounds"))
                .andExpect(flash().attribute("message", "Xóa đợt nháp thành công"));
        verify(service).deleteDraftRound(1L);
    }

    @Test
    void missingRoundReturnsReadableMessage() throws Exception {
        when(service.getRoundById(1L)).thenThrow(new NoSuchElementException("Không tìm thấy đợt"));
        mvc.perform(get("/dean/rounds/1")).andExpect(redirectedUrl("/dean/rounds"))
                .andExpect(flash().attribute("error", "Không tìm thấy đợt"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validRequest(String path) {
        return post(path).param("name", "  Đợt giữ dữ liệu  ").param("roundType", "COURSE")
                .param("proposalStartsAt", "2030-01-01T08:00")
                .param("proposalEndsAt", "2030-01-02T08:00")
                .param("studentStartsAt", "2030-01-02T08:00")
                .param("studentEndsAt", "2030-01-03T08:00")
                .param("groupMinSize", "3").param("groupMaxSize", "3");
    }

    @Test
    void createConflictPreservesFormAndDoesNotExposeDatabaseError() throws Exception {
        when(service.createRound(any())).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("private SQL details"));
        mvc.perform(validRequest("/dean/rounds")).andExpect(status().isOk())
                .andExpect(view().name("rounds/form"))
                .andExpect(model().attributeHasErrors("roundForm"))
                .andExpect(content().string(containsString("Đợt giữ dữ liệu")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("private SQL details"))));
    }

    @Test
    void updateConflictPreservesEditIdAndInput() throws Exception {
        when(service.updateRound(eq(1L), any())).thenThrow(
                new org.springframework.dao.CannotAcquireLockException("private lock details"));
        mvc.perform(validRequest("/dean/rounds/1/edit")).andExpect(status().isOk())
                .andExpect(model().attribute("editId", 1L))
                .andExpect(model().attributeHasErrors("roundForm"))
                .andExpect(content().string(containsString("Đợt giữ dữ liệu")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("private lock details"))));
    }

    @Test
    void concurrentActionReturnsReadableError() throws Exception {
        when(service.openRound(1L)).thenThrow(
                new org.springframework.dao.CannotAcquireLockException("private lock details"));
        mvc.perform(post("/dean/rounds/1/open")).andExpect(redirectedUrl("/dean/rounds"))
                .andExpect(flash().attribute("error", containsString("thao tác khác")));
    }
}