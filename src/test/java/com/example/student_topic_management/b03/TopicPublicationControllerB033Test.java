package com.example.student_topic_management.b03;

import com.example.student_topic_management.common.config.SecurityConfig;
import com.example.student_topic_management.controller.*;
import com.example.student_topic_management.dto.PublishedTopicView;
import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.Role;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;
import com.example.student_topic_management.repository.*;
import com.example.student_topic_management.service.*;
import java.time.*;
import java.util.*;
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

@SpringJUnitWebConfig(TopicPublicationControllerB033Test.Config.class)
class TopicPublicationControllerB033Test {
    static final String BASE = "/dean/topics/publish";
    @Configuration @EnableWebMvc @EnableWebSecurity
    @Import({SecurityConfig.class, TopicPublicationController.class, TopicPublicationService.class,
            PublishedTopicController.class, PublishedTopicService.class})
    static class Config {
        @Bean TopicRepository topics() { return mock(TopicRepository.class); }
        @Bean CurrentUserService users() { return mock(CurrentUserService.class); }
        @Bean TopicAuditService audit() { return mock(TopicAuditService.class); }
        @Bean RegistrationRoundRepository rounds() { return mock(RegistrationRoundRepository.class); }
        @Bean DepartmentRepository departments() { return mock(DepartmentRepository.class); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean Clock clock() { return Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC); }
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
    @Autowired TopicRepository repository;
    @Autowired CurrentUserService users;
    @Autowired TopicAuditService audit;
    @Autowired RegistrationRoundRepository rounds;
    @Autowired DepartmentRepository departments;
    MockMvc mvc;
    Topic topic;

    @BeforeEach void setup() {
        reset(repository, users, audit, rounds, departments);
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        AppUser dean = mock(AppUser.class);
        when(dean.getId()).thenReturn(20L);
        when(dean.getEnabled()).thenReturn(true);
        Role role = mock(Role.class);
        when(role.getCode()).thenReturn("DEAN");
        when(dean.getRoles()).thenReturn(Set.of(role));
        when(users.getCurrentUser()).thenReturn(dean);
        topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setRoundId(7L);
        topic.setDepartmentId(9L);
        topic.setCode("B033-CODE");
        topic.setTitle("<script>Publication</script>");
        topic.setDescription("Mô tả công bố");
        topic.setStatus(TopicStatus.APPROVED);
        when(repository.findByIdForUpdate(10L)).thenReturn(Optional.of(topic));
        when(repository.findByStatusAndPublishedAtIsNull(eq(TopicStatus.APPROVED), any()))
                .thenAnswer(invocation -> new PageImpl<>(
                        topic.getStatus() == TopicStatus.APPROVED && topic.getPublishedAt() == null ? List.of(topic) : List.of(),
                        invocation.getArgument(1, Pageable.class),
                        topic.getStatus() == TopicStatus.APPROVED && topic.getPublishedAt() == null ? 1 : 0));
        when(rounds.findAll(any(Sort.class))).thenReturn(List.of());
        when(departments.findAll(any(Sort.class))).thenReturn(List.of());
        // Simulated repository boundary only; this does not execute the B05 JPQL against a database.
        when(repository.searchPublishedTopics(any(), any(), any(), any())).thenAnswer(invocation -> {
            List<PublishedTopicView> data = topic.getStatus() == TopicStatus.PUBLISHED ? List.of(
                    new PublishedTopicView(topic.getId(), topic.getRoundId(), topic.getDepartmentId(),
                            topic.getCode(), topic.getTitle(), topic.getDescription(), topic.getRequirements())) : List.of();
            return new PageImpl<>(data, invocation.getArgument(3, Pageable.class), data.size());
        });
    }

    @Test void deanSeesApprovedListWithRealTemplateAndCsrfButton() throws Exception {
        mvc.perform(get(BASE).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TopicPublicationController.class))
                .andExpect(view().name("dean/topics/publish-list"))
                .andExpect(content().string(containsString("B033-CODE")))
                .andExpect(content().string(containsString("action=\"" + BASE + "/10\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("Công bố")))
                .andExpect(content().string(containsString("&lt;script&gt;Publication&lt;/script&gt;")));
    }

    @Test void publishThenCatalogShowsDtoAndApprovedQueueBecomesEmpty() throws Exception {
        mvc.perform(get("/catalog/topics").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("B033-CODE"))));
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("success"));
        assertEquals(TopicStatus.PUBLISHED, topic.getStatus());
        assertEquals(LocalDateTime.of(2026, 10, 20, 10, 0), topic.getPublishedAt());
        mvc.perform(get("/catalog/topics").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(PublishedTopicController.class))
                .andExpect(content().string(containsString("B033-CODE")));
        mvc.perform(get(BASE).with(user("dean").roles("DEAN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Không có đề tài chờ công bố")));
    }

    @ParameterizedTest @ValueSource(strings = {"ADMIN", "DEPARTMENT_HEAD", "LECTURER", "STUDENT"})
    void onlyDeanCanListOrPublish(String role) throws Exception {
        mvc.perform(get(BASE).with(user("other").roles(role))).andExpect(status().isForbidden());
        mvc.perform(post(BASE + "/10").with(user("other").roles(role)).with(csrf()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(repository, audit);
    }

    @Test void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get(BASE)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mvc.perform(post(BASE + "/10").with(csrf())).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(repository, audit);
    }

    @Test void missingCsrfCannotPublish() throws Exception {
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN"))).andExpect(status().isForbidden());
        assertEquals(TopicStatus.APPROVED, topic.getStatus());
        verifyNoInteractions(repository, audit);
    }

    @Test void wrongStateProducesErrorWithoutPublishing() throws Exception {
        topic.setStatus(TopicStatus.SUBMITTED);
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("error"));
        assertEquals(TopicStatus.SUBMITTED, topic.getStatus());
        verifyNoInteractions(audit);
    }

    @Test void duplicatePublicationProducesErrorWithoutChangingTime() throws Exception {
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(flash().attributeExists("success"));
        LocalDateTime publishedAt = topic.getPublishedAt();
        clearInvocations(repository, audit);
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(redirectedUrl(BASE)).andExpect(flash().attributeExists("error"));
        assertEquals(publishedAt, topic.getPublishedAt());
        verify(repository, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test void publicationCannotBeTriggeredWithGet() throws Exception {
        mvc.perform(get(BASE + "/10").with(user("dean").roles("DEAN")))
                .andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(repository, audit);
    }

    @Test void dataFailureDoesNotLeakSqlOrClaimSuccess() throws Exception {
        when(repository.saveAndFlush(topic)).thenThrow(new DataIntegrityViolationException("PRIVATE SQL"));
        mvc.perform(post(BASE + "/10").with(user("dean").roles("DEAN")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(not(containsString("PRIVATE SQL"))));
        verifyNoInteractions(audit);
    }
}