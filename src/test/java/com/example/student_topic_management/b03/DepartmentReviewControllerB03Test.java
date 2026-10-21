package com.example.student_topic_management.b03;

import com.example.student_topic_management.common.config.SecurityConfig;
import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.controller.DepartmentReviewController;
import com.example.student_topic_management.dto.DepartmentReviewForm;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.service.DepartmentReviewService;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
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

@SpringJUnitWebConfig(DepartmentReviewControllerB03Test.Config.class)
class DepartmentReviewControllerB03Test {
    @Configuration @EnableWebMvc @EnableWebSecurity
    @Import({SecurityConfig.class, DepartmentReviewController.class})
    static class Config {
        @Bean DepartmentReviewService reviewService() { return mock(DepartmentReviewService.class); }
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
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    DepartmentReviewService service;
    Topic topic;

    @BeforeEach void setup() {
        service = AopTestUtils.getUltimateTargetObject(context.getBean(DepartmentReviewService.class));
        reset(service);
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setRoundId(7L);
        topic.setDepartmentId(9L);
        topic.setStatus(TopicStatus.SUBMITTED);
        topic.setTitle("<script>unsafe</script>");
        topic.setCode("B03-01");
        topic.setDescription("Mô tả B03");
        when(service.getPendingTopics(0)).thenReturn(new PageImpl<>(List.of(topic), PageRequest.of(0, 10), 11));
        when(service.getReviewPage(10L)).thenReturn(new DepartmentReviewService.ReviewPage(topic, true));
    }

    @Test void headRendersQueueWithRealLayoutAndPagination() throws Exception {
        mvc.perform(get("/department/topics").with(user("head").roles("DEPARTMENT_HEAD")))
                .andExpect(status().isOk())
                .andExpect(view().name("department/topics/list"))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")))
                .andExpect(content().string(containsString("/department/topics/10")))
                .andExpect(content().string(containsString("/department/topics?page=1")))
                .andExpect(content().string(containsString("&lt;script&gt;unsafe&lt;/script&gt;")));
    }

    @Test void detailRendersReviewFormWithCsrfAndEscapedContent() throws Exception {
        mvc.perform(get("/department/topics/10").with(user("head").roles("DEPARTMENT_HEAD")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("topic", "reviewForm", "canReview"))
                .andExpect(content().string(containsString("/department/topics/10/review")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("<script>unsafe</script>"))));
    }

    @Test void processedOrClosedTopicDoesNotRenderDecisionForm() throws Exception {
        when(service.getReviewPage(10L)).thenReturn(new DepartmentReviewService.ReviewPage(topic, false));
        mvc.perform(get("/department/topics/10").with(user("head").roles("DEPARTMENT_HEAD")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("action=\"/department/topics/10/review\""))));
    }

    @Test void emptyQueueRenders() throws Exception {
        when(service.getPendingTopics(0)).thenReturn(Page.empty(PageRequest.of(0, 10)));
        mvc.perform(get("/department/topics").with(user("head").roles("DEPARTMENT_HEAD")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Không có đề tài chờ sơ duyệt")));
    }

    @Test void anonymousIsSentToLogin() throws Exception {
        mvc.perform(get("/department/topics")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
    }

    @ParameterizedTest @ValueSource(strings = {"STUDENT", "LECTURER", "DEAN", "ADMIN"})
    void otherRolesCannotReadOrWrite(String role) throws Exception {
        mvc.perform(get("/department/topics").with(user("other").roles(role)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/department/topics/10/review").with(user("other").roles(role)).with(csrf())
                        .param("decision", "APPROVED"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test void crossDepartmentServiceDenialRemains403() throws Exception {
        when(service.getReviewPage(10L)).thenThrow(new AccessDeniedException("Other department"));
        mvc.perform(get("/department/topics/10").with(user("head").roles("DEPARTMENT_HEAD")))
                .andExpect(status().isForbidden());
        doThrow(new AccessDeniedException("Other department")).when(service).review(eq(10L), any());
        mvc.perform(post("/department/topics/10/review").with(user("head").roles("DEPARTMENT_HEAD"))
                        .with(csrf()).param("decision", "APPROVED"))
                .andExpect(status().isForbidden());
    }

    @Test void reviewRequiresCsrf() throws Exception {
        mvc.perform(post("/department/topics/10/review").with(user("head").roles("DEPARTMENT_HEAD"))
                        .param("decision", "APPROVED"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @ParameterizedTest @ValueSource(strings = {"APPROVED", "REJECTED"})
    void validDecisionsUsePostAndRedirect(String decision) throws Exception {
        mvc.perform(post("/department/topics/10/review").with(user("head").roles("DEPARTMENT_HEAD"))
                        .with(csrf()).param("decision", decision).param("comment", "Nhận xét")
                        .param("departmentId", "99").param("status", "PUBLISHED").param("departmentReviewedBy", "99"))
                .andExpect(redirectedUrl("/department/topics"))
                .andExpect(flash().attributeExists("success"));
        verify(service).review(eq(10L), argThat(form ->
                form.getDecision().name().equals(decision) && "Nhận xét".equals(form.getComment())));
    }

    @ParameterizedTest @ValueSource(strings = {"", "PUBLISHED", "approved"})
    void invalidDecisionDoesNotCallService(String decision) throws Exception {
        mvc.perform(post("/department/topics/10/review").with(user("head").roles("DEPARTMENT_HEAD"))
                        .with(csrf()).param("decision", decision))
                .andExpect(redirectedUrl("/department/topics/10"))
                .andExpect(flash().attributeExists("error", "reviewForm"));
        verifyNoInteractions(service);
    }

    @Test void tooLongCommentDoesNotCallService() throws Exception {
        mvc.perform(post("/department/topics/10/review").with(user("head").roles("DEPARTMENT_HEAD"))
                        .with(csrf()).param("decision", "APPROVED").param("comment", "x".repeat(4001)))
                .andExpect(redirectedUrl("/department/topics/10"))
                .andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }

    @Test void businessErrorsPreserveFormAndDoNotClaimSuccess() throws Exception {
        doThrow(new BusinessException("Vui lòng nhập lý do từ chối")).when(service).review(eq(10L), any());
        mvc.perform(post("/department/topics/10/review").with(user("head").roles("DEPARTMENT_HEAD"))
                        .with(csrf()).param("decision", "REJECTED"))
                .andExpect(redirectedUrl("/department/topics/10"))
                .andExpect(flash().attributeExists("error", "reviewForm"))
                .andExpect(flash().attributeCount(2));
    }

    @Test void badPageIsHandledWithoutLoopingToSameInvalidUrl() throws Exception {
        mvc.perform(get("/department/topics").with(user("head").roles("DEPARTMENT_HEAD")).param("page", "bad"))
                .andExpect(redirectedUrl("/department/topics"))
                .andExpect(flash().attributeExists("error"));
        verifyNoInteractions(service);
    }
}