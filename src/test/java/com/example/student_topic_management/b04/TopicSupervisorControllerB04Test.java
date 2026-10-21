package com.example.student_topic_management.b04;

import com.example.student_topic_management.common.config.SecurityConfig;
import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.controller.TopicSupervisorController;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.service.SupervisorService;
import com.example.student_topic_management.service.SupervisorViewService;
import com.example.student_topic_management.service.SupervisorViewService.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import static com.example.student_topic_management.b04.B04Fixtures.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(TopicSupervisorControllerB04Test.Config.class)
class TopicSupervisorControllerB04Test {
    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, TopicSupervisorController.class})
    static class Config {
        @Bean SupervisorService supervisorService() { return mock(SupervisorService.class); }
        @Bean SupervisorViewService supervisorViewService() { return mock(SupervisorViewService.class); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean SpringTemplateEngine templateEngine() {
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix("templates/");
            resolver.setSuffix(".html");
            resolver.setCharacterEncoding("UTF-8");
            resolver.setCacheable(false);
            SpringTemplateEngine engine = new SpringTemplateEngine();
            engine.setTemplateResolver(resolver);
            engine.addDialect(new nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect());
            engine.addDialect(new SpringSecurityDialect());
            return engine;
        }
        @Bean ThymeleafViewResolver viewResolver(SpringTemplateEngine engine) {
            ThymeleafViewResolver resolver = new ThymeleafViewResolver();
            resolver.setTemplateEngine(engine);
            resolver.setCharacterEncoding("UTF-8");
            return resolver;
        }
    }

    private static final String CHOOSE = "/dean/topics/supervisors";
    private static final String DETAIL = "/dean/topics/10/supervisors";
    private static final String DELETE = DETAIL + "/20/delete";
    @Autowired WebApplicationContext context;
    private MockMvc mvc;
    private SupervisorService service;
    private SupervisorViewService views;

    @BeforeEach void setup() {
        service = AopTestUtils.getUltimateTargetObject(context.getBean(SupervisorService.class));
        views = AopTestUtils.getUltimateTargetObject(context.getBean(SupervisorViewService.class));
        reset(service, views);
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        when(views.getTopics(0)).thenReturn(new PageImpl<>(List.of(topic(TopicStatus.DRAFT)), PageRequest.of(0, 10), 11));
        when(views.getPage(10L)).thenReturn(page(true, false));
    }

    private PageData page(boolean editable, boolean approvedRegistration) {
        return new PageData(topic(editable ? TopicStatus.DRAFT : TopicStatus.SUBMITTED),
                List.of(new SupervisorSlot(1, 20L, "GV-20", "GV đã phân công"),
                        new SupervisorSlot(2, null, "", "Chưa phân công")),
                List.of(new LecturerOption(21L, "GV-21", "GV nhiều vai trò")),
                List.of(2), editable, approvedRegistration);
    }

    @Test void rendersTopicChooserWithRealIdPaginationAndSidebar() throws Exception {
        mvc.perform(get(CHOOSE).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk()).andExpect(view().name("supervisors/topics"))
                .andExpect(content().string(containsString("Đề tài B04")))
                .andExpect(content().string(containsString(DETAIL)))
                .andExpect(content().string(containsString("Trang sau")))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")));
    }

    @Test void rendersAssignedSlotsCandidatesDeleteAndCsrfWithSidebar() throws Exception {
        mvc.perform(get(DETAIL).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk()).andExpect(view().name("supervisors/list"))
                .andExpect(content().string(containsString("GV đã phân công")))
                .andExpect(content().string(containsString("GV nhiều vai trò")))
                .andExpect(content().string(containsString(DELETE)))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")));
    }

    @Test void nonDraftHasNoAddOrDeleteForm() throws Exception {
        when(views.getPage(10L)).thenReturn(page(false, false));
        mvc.perform(get(DETAIL).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Chỉ được xem phân công")))
                .andExpect(content().string(not(containsString("Xóa phân công"))))
                .andExpect(content().string(not(containsString("Thêm giảng viên hướng dẫn"))));
    }

    @Test void approvedRegistrationDisablesDraftForms() throws Exception {
        PageData data = page(false, true);
        data.topic().setStatus(TopicStatus.DRAFT);
        when(views.getPage(10L)).thenReturn(data);
        mvc.perform(get(DETAIL).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("đã có đăng ký APPROVED")))
                .andExpect(content().string(not(containsString("Xóa phân công"))));
    }

    @Test void addAndDeleteRedirectAfterSuccess() throws Exception {
        mvc.perform(post(DETAIL).with(user("dean").roles("DEAN")).with(csrf())
                        .param("lecturerId", "21").param("slotNo", "2"))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attributeExists("success"));
        verify(service).addSupervisor(10L, 21L, 2);
        mvc.perform(post(DELETE).with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attributeExists("success"));
        verify(service).removeSupervisor(10L, 20L);
    }

    @ParameterizedTest @ValueSource(strings = {"0", "3", "bad-number", ""})
    void invalidSlotRedirectsAndDoesNotWrite(String slot) throws Exception {
        MvcResult result = mvc.perform(post(DETAIL).with(user("dean").roles("DEAN")).with(csrf())
                        .param("lecturerId", "21").param("slotNo", slot))
                .andExpect(redirectedUrl(DETAIL))
                .andExpect(flash().attributeExists("formErrors", "supervisorForm")).andReturn();
        mvc.perform(get(DETAIL).with(user("dean").roles("DEAN")).flashAttrs(result.getFlashMap()))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("Failed to convert"))));
        verifyNoInteractions(service);
    }

    @ParameterizedTest @ValueSource(strings = {"0", "-1", "not-an-id", ""})
    void invalidLecturerIsValidatedBeforeService(String lecturerId) throws Exception {
        mvc.perform(post(DETAIL).with(user("dean").roles("DEAN")).with(csrf())
                        .param("lecturerId", lecturerId).param("slotNo", "2"))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attributeExists("formErrors"));
        verifyNoInteractions(service);
    }

    @Test void missingFieldsAreRejected() throws Exception {
        mvc.perform(post(DETAIL).with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attributeExists("formErrors"));
        verifyNoInteractions(service);
    }

    @Test void businessErrorsPreserveFormAndReturnSafeFlash() throws Exception {
        when(service.addSupervisor(10L, 21L, 2)).thenThrow(new BusinessException("Vị trí đã được sử dụng"));
        mvc.perform(post(DETAIL).with(user("dean").roles("DEAN")).with(csrf())
                        .param("lecturerId", "21").param("slotNo", "2"))
                .andExpect(redirectedUrl(DETAIL))
                .andExpect(flash().attribute("error", "Vị trí đã được sử dụng"))
                .andExpect(flash().attributeExists("supervisorForm"));
        doThrow(new BusinessException("Đề tài không còn DRAFT")).when(service).removeSupervisor(10L, 20L);
        mvc.perform(post(DELETE).with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attributeExists("error"));
    }

    @Test void databaseErrorsDoNotExposeSqlForEitherMutation() throws Exception {
        when(service.addSupervisor(10L, 21L, 2)).thenThrow(new DataIntegrityViolationException("SECRET_SQL"));
        doThrow(new DataIntegrityViolationException("SECRET_SQL")).when(service).removeSupervisor(10L, 20L);
        mvc.perform(post(DETAIL).with(user("dean").roles("DEAN")).with(csrf())
                        .param("lecturerId", "21").param("slotNo", "2"))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attribute("error", not(containsString("SECRET_SQL"))));
        mvc.perform(post(DELETE).with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(DETAIL)).andExpect(flash().attribute("error", not(containsString("SECRET_SQL"))));
    }

    @Test void missingTopicOrMalformedPathRedirectsSafely() throws Exception {
        when(views.getPage(999L)).thenThrow(new BusinessException("Không tìm thấy đề tài"));
        mvc.perform(get("/dean/topics/999/supervisors").with(user("dean").roles("DEAN")))
                .andExpect(redirectedUrl(CHOOSE)).andExpect(flash().attributeExists("error"));
        mvc.perform(get("/dean/topics/not-an-id/supervisors").with(user("dean").roles("DEAN")))
                .andExpect(redirectedUrl(CHOOSE)).andExpect(flash().attributeExists("error"));
    }

    @Test void missingAndInvalidCsrfAreRejectedBeforeController() throws Exception {
        for (String path : List.of(DETAIL, DELETE)) {
            mvc.perform(post(path).with(user("dean").roles("DEAN")))
                    .andExpect(status().isForbidden());
            mvc.perform(post(path).with(user("dean").roles("DEAN")).with(csrf().useInvalidToken()))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @ParameterizedTest @ValueSource(strings = {"LECTURER", "STUDENT", "ADMIN", "DEPARTMENT_HEAD"})
    void otherRolesCannotReadOrMutate(String role) throws Exception {
        for (String path : List.of(CHOOSE, DETAIL)) {
            mvc.perform(get(path).with(user("other").roles(role))).andExpect(status().isForbidden());
        }
        for (String path : List.of(DETAIL, DELETE)) {
            mvc.perform(post(path).with(user("other").roles(role)).with(csrf())).andExpect(status().isForbidden());
        }
        verifyNoInteractions(service, views);
    }

    @Test void anonymousIsSentToLogin() throws Exception {
        mvc.perform(get(CHOOSE)).andExpect(redirectedUrl("/login"));
        mvc.perform(get(DETAIL)).andExpect(redirectedUrl("/login"));
        mvc.perform(post(DETAIL).with(csrf())).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service, views);
    }

    @Test void titleAndLecturerNamesAreEscaped() throws Exception {
        PageData data = page(true, false);
        data.topic().setTitle("<script>alert('x')</script>");
        when(views.getPage(10L)).thenReturn(new PageData(data.topic(), data.slots(),
                List.of(new LecturerOption(21L, "GV-21", "<script>bad()</script>")), data.availableSlots(), true, false));
        mvc.perform(get(DETAIL).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert"))))
                .andExpect(content().string(not(containsString("<script>bad"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }
}
