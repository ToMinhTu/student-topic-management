package com.example.student_topic_management.registration.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import com.example.student_topic_management.common.config.SecurityConfig;
import com.example.student_topic_management.registration.repository.DeanRegistrationRepository.RegistrationRow;
import com.example.student_topic_management.registration.service.DeanRegistrationService;
import com.example.student_topic_management.registration.service.LecturerRegistrationService;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(LecturerRegistrationControllerTest.Config.class)
class LecturerRegistrationControllerTest {
    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, LecturerRegistrationController.class, SidebarPreviewController.class})
    static class Config {
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean LecturerRegistrationService queries() { return mock(LecturerRegistrationService.class); }
        @Bean DeanRegistrationService decisions() { return mock(DeanRegistrationService.class); }
        @Bean SpringTemplateEngine templateEngine() {
            ClassLoaderTemplateResolver templates = new ClassLoaderTemplateResolver();
            templates.setPrefix("templates/");
            templates.setSuffix(".html");
            templates.setCharacterEncoding("UTF-8");
            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(templates);
            engine.addDialect(new nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect());
            engine.addDialect(new org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect());
            return engine;
        }
        @Bean ThymeleafViewResolver viewResolver(SpringTemplateEngine engine) {
            ThymeleafViewResolver views = new ThymeleafViewResolver();
            views.setTemplateEngine(engine);
            views.setCharacterEncoding("UTF-8");
            return views;
        }
    }

    // Test-only endpoint to render the real sidebar for every role combination.
    @org.springframework.stereotype.Controller
    static class SidebarPreviewController {
        @org.springframework.web.bind.annotation.GetMapping("/test/sidebar")
        String sidebar() {
            return "fragments/sidebar";
        }
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "LECTURER, true, false",
        "LECTURER+DEPARTMENT_HEAD, true, true",
        "LECTURER+DEAN, true, false",
        "LECTURER+DEAN+DEPARTMENT_HEAD, true, true",
        "DEPARTMENT_HEAD, false, true",
        "DEAN, false, false",
        "STUDENT, false, false",
        "ADMIN, false, false",
        "LECTURER+ADMIN, false, false"
    })
    void sidebarShowsOnlyApplicableNewMenus(String roles, boolean showLecturerGroups, boolean showDepartmentReview)
            throws Exception {
        String html = mvc.perform(get("/test/sidebar").with(user("sidebar-user").roles(roles.split("\\+"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(showLecturerGroups, html.contains("Xem danh sách nhóm"));
        org.junit.jupiter.api.Assertions.assertEquals(showDepartmentReview, html.contains("href=\"/department/topics\""));
        if (roles.contains("LECTURER")) {
            org.junit.jupiter.api.Assertions.assertEquals(1,
                    html.split("href=\"/groups\"", -1).length - 1, "Avoid duplicate group links");
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired LecturerRegistrationService queries;
    @Autowired DeanRegistrationService decisions;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        queries = org.springframework.test.util.AopTestUtils.getUltimateTargetObject(queries);
        decisions = org.springframework.test.util.AopTestUtils.getUltimateTargetObject(decisions);
        reset(queries, decisions);
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        when(queries.findRegistrations(any(), any(), anyInt())).thenReturn(Page.empty(PageRequest.of(0, 10)));
        when(queries.findRounds()).thenReturn(List.of(new LecturerRegistrationService.RoundOption(7L, "Đợt kiểm thử")));
    }

    @Test
    void lecturerGetsRealPageInsteadOf404() throws Exception {
        mvc.perform(get("/lecturer/registrations").with(user("lecturer").roles("LECTURER")))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(LecturerRegistrationController.class))
                .andExpect(view().name("lecturer/registrations/list"))
                .andExpect(content().string(containsString("Không có hồ sơ phù hợp")))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")));
    }

    @Test
    void populatedPageRendersActionsCsrfAndPagination() throws Exception {
        var row = new RegistrationRow(1L, 2L, "Nhóm kiểm thử", 3L, "DT-1", "<script>alert(1)</script>",
                7L, "Đợt kiểm thử", "PENDING", LocalDateTime.of(2026, 10, 22, 8, 0));
        when(queries.findRegistrations(7L, "PENDING", 0))
                .thenReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 10), 11));
        mvc.perform(get("/lecturer/registrations").param("roundId", "7").param("status", "pending")
                        .with(user("lecturer").roles("LECTURER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nhóm kiểm thử")))
                .andExpect(content().string(containsString("action=\"/lecturer/registrations/1/approve\"")))
                .andExpect(content().string(containsString("action=\"/lecturer/registrations/1/reject\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("Trang sau")))
                .andExpect(content().string(containsString("status=PENDING")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void decidedRowDoesNotShowDecisionButtons() throws Exception {
        var row = new RegistrationRow(1L, 2L, "Nhóm", 3L, "DT-1", "Đề tài",
                7L, "Đợt", "APPROVED", LocalDateTime.of(2026, 10, 22, 8, 0));
        when(queries.findRegistrations(any(), any(), anyInt())).thenReturn(new PageImpl<>(List.of(row)));
        mvc.perform(get("/lecturer/registrations").with(user("lecturer").roles("LECTURER")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("/1/approve"))))
                .andExpect(content().string(not(containsString("/1/reject"))));
    }

    @Test
    void anonymousIsSentToLogin() throws Exception {
        mvc.perform(get("/lecturer/registrations")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(queries, decisions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STUDENT", "DEAN", "ADMIN"})
    void otherRolesCannotReadOrDecide(String role) throws Exception {
        mvc.perform(get("/lecturer/registrations").with(user("other").roles(role)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/lecturer/registrations/1/approve").with(user("other").roles(role)).with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(queries, decisions);
    }

    @Test
    void studentWithLecturerRoleIsStillDenied() throws Exception {
        mvc.perform(get("/lecturer/registrations").with(user("mixed").roles("STUDENT", "LECTURER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(queries, decisions);
    }

    @Test
    void decisionRequiresCsrfToken() throws Exception {
        mvc.perform(post("/lecturer/registrations/1/approve").with(user("lecturer").roles("LECTURER")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(decisions);
    }

    @Test
    void approveUsesScopedServiceEntryPoint() throws Exception {
        mvc.perform(post("/lecturer/registrations/1/approve").with(user("lecturer").roles("LECTURER")).with(csrf()))
                .andExpect(redirectedUrl("/lecturer/registrations"))
                .andExpect(flash().attributeExists("success"));
        verify(decisions).approveAsSupervisor(1L);
        verify(decisions, never()).approveRegistration(any());
    }

    @Test
    void rejectUsesSubmittedReason() throws Exception {
        mvc.perform(post("/lecturer/registrations/1/reject").param("reason", "Cần bổ sung")
                        .with(user("lecturer").roles("LECTURER")).with(csrf()))
                .andExpect(redirectedUrl("/lecturer/registrations"))
                .andExpect(flash().attributeExists("success"));
        verify(decisions).rejectAsSupervisor(1L, "Cần bổ sung");
    }

    @Test
    void ownershipDenialIsNotSwallowed() throws Exception {
        doThrow(new AccessDeniedException("Not assigned")).when(decisions).approveAsSupervisor(2L);
        mvc.perform(post("/lecturer/registrations/2/approve").with(user("lecturer").roles("LECTURER")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void businessErrorReturnsReadableFeedback() throws Exception {
        doThrow(new IllegalStateException("Hồ sơ đã được xử lý")).when(decisions).approveAsSupervisor(1L);
        mvc.perform(post("/lecturer/registrations/1/approve").with(user("lecturer").roles("LECTURER")).with(csrf()))
                .andExpect(redirectedUrl("/lecturer/registrations"))
                .andExpect(flash().attribute("error", "Hồ sơ đã được xử lý"));
    }
}
