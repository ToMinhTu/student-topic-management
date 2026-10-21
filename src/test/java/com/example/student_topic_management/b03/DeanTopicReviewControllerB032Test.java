package com.example.student_topic_management.b03;

import com.example.student_topic_management.common.config.SecurityConfig;
import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.controller.*;
import com.example.student_topic_management.dto.DeanTopicReviewForm;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.service.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(DeanTopicReviewControllerB032Test.Config.class)
class DeanTopicReviewControllerB032Test {
    @Configuration @EnableWebMvc @EnableWebSecurity
    @Import({SecurityConfig.class, DeanTopicReviewController.class, TopicSupervisorController.class})
    static class Config {
        @Bean DeanTopicReviewService reviewService() { return mock(DeanTopicReviewService.class); }
        @Bean SupervisorService supervisorService() { return mock(SupervisorService.class); }
        @Bean SupervisorViewService supervisorViews() { return mock(SupervisorViewService.class); }
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
    static final String BASE = "/dean/topics/review";
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    DeanTopicReviewService service;
    SupervisorViewService supervisorViews;
    Topic topic;

    @BeforeEach void setup() {
        service = AopTestUtils.getUltimateTargetObject(context.getBean(DeanTopicReviewService.class));
        supervisorViews = AopTestUtils.getUltimateTargetObject(context.getBean(SupervisorViewService.class));
        reset(service, supervisorViews);
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setRoundId(7L);
        topic.setDepartmentId(9L);
        topic.setStatus(TopicStatus.SUBMITTED);
        topic.setDepartmentDecision("APPROVED");
        topic.setDepartmentComment("<script>unsafe</script>");
        topic.setTitle("Đề tài B03.2");
        topic.setCode("B032-01");
        topic.setDescription("Mô tả B03.2");
        when(service.getPendingTopics(0)).thenReturn(new PageImpl<>(List.of(topic), PageRequest.of(0, 10), 11));
        when(service.getReviewPage(10L)).thenReturn(new DeanTopicReviewService.ReviewPage(topic, true));
    }

    @Test void deanRendersQueueLayoutPaginationAndCorrectHandler() throws Exception {
        mvc.perform(get(BASE).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(DeanTopicReviewController.class))
                .andExpect(view().name("dean/topics/review-list"))
                .andExpect(model().attributeExists("topics"))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")))
                .andExpect(content().string(containsString(BASE + "/10")))
                .andExpect(content().string(containsString(BASE + "?page=1")))
                .andExpect(content().string(containsString("B032-01")));
        verify(service).getPendingTopics(0);
    }

    @Test void emptyQueueRenders() throws Exception {
        when(service.getPendingTopics(0)).thenReturn(Page.empty(PageRequest.of(0, 10)));
        mvc.perform(get(BASE).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Không có đề tài chờ phê duyệt")));
    }

    @Test void detailRendersDepartmentDecisionEscapedCommentsAndCsrf() throws Exception {
        mvc.perform(get(BASE + "/10").with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(view().name("dean/topics/review-detail"))
                .andExpect(model().attributeExists("topic", "reviewForm", "canReview"))
                .andExpect(content().string(containsString("action=\"" + BASE + "/10\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("&lt;script&gt;unsafe&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>unsafe</script>"))));
    }

    @Test void ineligibleTopicHasNoDecisionForm() throws Exception {
        when(service.getReviewPage(10L)).thenReturn(new DeanTopicReviewService.ReviewPage(topic, false));
        mvc.perform(get(BASE + "/10").with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("action=\"" + BASE + "/10\""))))
                .andExpect(content().string(containsString("Đề tài chưa đủ điều kiện")));
    }

    @Test void anonymousCannotReadOrSubmit() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(post(BASE + "/10").with(csrf()).param("decision", "APPROVED"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest @ValueSource(strings = {"ADMIN", "DEPARTMENT_HEAD", "LECTURER", "STUDENT"})
    void nonDeanRolesCannotReadDetailOrSubmit(String role) throws Exception {
        mvc.perform(get(BASE).with(user("other").roles(role))).andExpect(status().isForbidden());
        mvc.perform(get(BASE + "/10").with(user("other").roles(role))).andExpect(status().isForbidden());
        mvc.perform(post(BASE + "/10").with(user("other").roles(role)).with(csrf()).param("decision", "APPROVED"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void serviceProxyDeniesAnonymousDirectCalls() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> context.getBean(DeanTopicReviewService.class).getPendingTopics(0));
        verifyNoInteractions(service);
    }

    @Test @WithMockUser(roles = "DEPARTMENT_HEAD")
    void serviceProxyDeniesNonDeanDirectCalls() {
        assertThrows(AccessDeniedException.class,
                () -> context.getBean(DeanTopicReviewService.class).getPendingTopics(0));
        verifyNoInteractions(service);
    }

    @Test void csrfIsMandatory() throws Exception {
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).param("decision", "APPROVED"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @ParameterizedTest @ValueSource(strings = {"APPROVED", "REJECTED"})
    void validPostBindsOnlyDecisionAndReasonAndRedirects(String decision) throws Exception {
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf())
                        .param("decision", decision).param("rejectionReason", "Lý do")
                        .param("approvedBy", "999").param("approvedAt", "2000-01-01T00:00:00")
                        .param("publishedAt", "2000-01-01T00:00:00").param("status", "PUBLISHED")
                        .param("departmentId", "999"))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("success"));
        verify(service).review(eq(10L), argThat(form ->
                form.getDecision().name().equals(decision) && "Lý do".equals(form.getRejectionReason())));
        assertEquals(2, DeanTopicReviewForm.class.getDeclaredFields().length);
    }

    @ParameterizedTest @ValueSource(strings = {"", "PUBLISHED", "approved"})
    void invalidDecisionCannotCallService(String decision) throws Exception {
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()).param("decision", decision))
                .andExpect(redirectedUrl(BASE + "/10")).andExpect(flash().attributeExists("error", "reviewForm"));
        verifyNoInteractions(service);
    }

    @Test void oversizedReasonCannotCallService() throws Exception {
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf())
                        .param("decision", "REJECTED").param("rejectionReason", "x".repeat(4001)))
                .andExpect(redirectedUrl(BASE + "/10")).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }

    @Test void businessFailurePreservesFormAndNeverClaimsSuccess() throws Exception {
        doThrow(new BusinessException("Đề tài phải có từ 1 đến 2 giảng viên hướng dẫn"))
                .when(service).review(eq(10L), any());
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()).param("decision", "APPROVED"))
                .andExpect(redirectedUrl(BASE + "/10"))
                .andExpect(flash().attributeExists("error", "reviewForm")).andExpect(flash().attributeCount(2));
    }

    @Test void accessDeniedIsNotConvertedToSuccessOrBusinessRedirect() throws Exception {
        doThrow(new AccessDeniedException("Disabled dean")).when(service).review(eq(10L), any());
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()).param("decision", "APPROVED"))
                .andExpect(status().isForbidden());
    }

    @Test void databaseFailureRendersSafeMessageWithoutExceptionDetailsOrRedirectLoop() throws Exception {
        doThrow(new DataIntegrityViolationException("PRIVATE SQL DETAILS")).when(service).review(eq(10L), any());
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()).param("decision", "APPROVED"))
                .andExpect(status().isOk()).andExpect(view().name("dean/topics/review-list"))
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(not(containsString("PRIVATE SQL DETAILS"))));
    }

    @Test void invalidPageTypeReturnsToSafeDefault() throws Exception {
        mvc.perform(get(BASE).with(user("dean").roles("DEAN")).param("page", "bad"))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }

    @Test void oldSupervisorEndpointKeepsItsOriginalHandler() throws Exception {
        when(supervisorViews.getTopics(0)).thenReturn(Page.empty(PageRequest.of(0, 10)));
        mvc.perform(get("/dean/topics/supervisors").with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TopicSupervisorController.class))
                .andExpect(view().name("supervisors/topics"));
        verifyNoInteractions(service);
    }
}