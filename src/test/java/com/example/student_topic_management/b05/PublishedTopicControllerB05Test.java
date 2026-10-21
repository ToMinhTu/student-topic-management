package com.example.student_topic_management.b05;

import com.example.student_topic_management.common.config.SecurityConfig;
import com.example.student_topic_management.controller.PublishedTopicController;
import com.example.student_topic_management.dto.*;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.registration.controller.TopicRegistrationController;
import com.example.student_topic_management.registration.service.*;
import com.example.student_topic_management.repository.*;
import com.example.student_topic_management.service.PublishedTopicService;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
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
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringJUnitWebConfig(PublishedTopicControllerB05Test.Config.class)
class PublishedTopicControllerB05Test {
    @Configuration @EnableWebMvc @EnableWebSecurity
    @Import({SecurityConfig.class, PublishedTopicController.class,
            PublishedTopicService.class, TopicRegistrationController.class})
    static class Config {
        @Bean TopicRepository topicRepository() { return mock(TopicRepository.class); }
        @Bean RegistrationRoundRepository roundRepository() { return mock(RegistrationRoundRepository.class); }
        @Bean DepartmentRepository departmentRepository() { return mock(DepartmentRepository.class); }
        @Bean UserDetailsService userDetailsService() { return mock(UserDetailsService.class); }
        @Bean(name = "registrationTopicRepository")
        com.example.student_topic_management.registration.repository.TopicRepository registrationTopics() {
            return mock(com.example.student_topic_management.registration.repository.TopicRepository.class);
        }
        @Bean TopicRegistrationService registrationService() { return mock(TopicRegistrationService.class); }
        @Bean StudentAccess studentAccess() { return mock(StudentAccess.class); }
        @Bean JdbcTemplate jdbcTemplate() { return mock(JdbcTemplate.class); }
        @Bean Clock clock() { return Clock.systemUTC(); }
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
    @Autowired RegistrationRoundRepository rounds;
    @Autowired DepartmentRepository departments;
    @Autowired PublishedTopicService service;
    MockMvc mvc;

    @BeforeEach void setup() {
        reset(repository, rounds, departments);
        mvc = webAppContextSetup(context).apply(springSecurity()).build();
        RegistrationRound round = mock(RegistrationRound.class);
        when(round.getId()).thenReturn(7L);
        when(round.getName()).thenReturn("Đợt B05");
        Department department = mock(Department.class);
        when(department.getId()).thenReturn(9L);
        when(department.getName()).thenReturn("Bộ môn B05");
        when(rounds.findAll(any(Sort.class))).thenReturn(List.of(round));
        when(departments.findAll(any(Sort.class))).thenReturn(List.of(department));
        when(repository.searchPublishedTopics(any(), any(), any(), any())).thenAnswer(invocation ->
                Page.empty(invocation.getArgument(3, Pageable.class)));
    }

    @ParameterizedTest @ValueSource(strings = {"STUDENT", "LECTURER", "DEAN", "ADMIN", "DEPARTMENT_HEAD"})
    void authenticatedUsersRenderRealLayoutAndEmptyState(String role) throws Exception {
        mvc.perform(get("/catalog/topics").with(user("b05").roles(role)))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(PublishedTopicController.class))
                .andExpect(view().name("catalog/topics"))
                .andExpect(model().attributeExists("topics", "filter", "rounds", "departments"))
                .andExpect(model().attribute("filter", new PublishedTopicFilter(null, null, null, 0, 10)))
                .andExpect(content().string(containsString("Không có đề tài đã công bố")))
                .andExpect(content().string(containsString("id=\"app-sidebar\"")))
                .andExpect(content().string(containsString("Đợt B05")))
                .andExpect(content().string(containsString("Bộ môn B05")))
                .andExpect(content().string(not(containsString("rel=\"next\""))));
        verify(repository).searchPublishedTopics(null, null, null, PageRequest.of(0, 10));
        verify(rounds).findAll(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        verify(departments).findAll(Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
    }

    @Test void anonymousIsRedirectedBeforeReadingData() throws Exception {
        mvc.perform(get("/catalog/topics"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(repository, rounds, departments);
    }

    @Test void serviceMethodSecurityAlsoRejectsAnonymousInvocation() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> service.search(null));
        verifyNoInteractions(repository);
    }

    @ParameterizedTest @CsvSource({
            "page,-1", "size,0", "size,-1", "roundId,0", "roundId,-1",
            "departmentId,0", "departmentId,-1", "page,no", "size,no",
            "roundId,no", "departmentId,no", "page,2147483648"
    })
    void invalidParametersReturn400WithoutQueries(String name, String value) throws Exception {
        mvc.perform(get("/catalog/topics").with(user("b05")).param(name, value))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository, rounds, departments);
    }

    @Test void oversizedKeywordReturns400() throws Exception {
        mvc.perform(get("/catalog/topics").with(user("b05")).param("keyword", "x".repeat(256)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test void combinedFiltersRenderEscapedDtoAndKeepBothPaginationLinks() throws Exception {
        List<PublishedTopicView> data = List.of(new PublishedTopicView(
                1L, 7L, 9L, "CODE-01", "<script>alert(1)</script>", "Mô tả B05", null));
        Page<PublishedTopicView> result = new PageImpl<>(data, PageRequest.of(1, 10), 30);
        when(repository.searchPublishedTopics("%code%", 7L, 9L, PageRequest.of(1, 10))).thenReturn(result);
        MvcResult response = mvc.perform(get("/catalog/topics").with(user("b05"))
                        .param("keyword", "CODE").param("roundId", "7").param("departmentId", "9")
                        .param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("topics", sameInstance(result)))
                .andExpect(model().attribute("filter", new PublishedTopicFilter("CODE", 7L, 9L, 1, 10)))
                .andExpect(content().string(containsString("CODE-01")))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
                .andExpect(content().string(containsString("Không có")))
                .andExpect(content().string(containsString(
                        "/catalog/topics?keyword=CODE&amp;roundId=7&amp;departmentId=9&amp;page=0&amp;size=10")))
                .andExpect(content().string(containsString(
                        "/catalog/topics?keyword=CODE&amp;roundId=7&amp;departmentId=9&amp;page=2&amp;size=10")))
                .andReturn();
        String html = response.getResponse().getContentAsString();
        assertFalse(html.contains("/topics/1/register"));
        assertFalse(html.contains("proposedBy"));
        assertFalse(html.contains("rejectionReason"));
        verify(repository).searchPublishedTopics("%code%", 7L, 9L, PageRequest.of(1, 10));
        assertEquals(List.of(new PublishedTopicController.FilterOption(7L, "Đợt B05")),
                response.getModelAndView().getModel().get("rounds"));
        assertEquals(List.of(new PublishedTopicController.FilterOption(9L, "Bộ môn B05")),
                response.getModelAndView().getModel().get("departments"));
    }

    @Test void cappedSizeIsReflectedInModelAndForm() throws Exception {
        mvc.perform(get("/catalog/topics").with(user("b05")).param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filter", new PublishedTopicFilter(null, null, null, 0, 100)))
                .andExpect(content().string(containsString("name=\"size\" value=\"100\"")));
        verify(repository).searchPublishedTopics(null, null, null, PageRequest.of(0, 100));
    }

    @Test void emptyDropdownValuesAreOptional() throws Exception {
        mvc.perform(get("/catalog/topics").with(user("b05")).param("roundId", "").param("departmentId", ""))
                .andExpect(status().isOk());
        verify(repository).searchPublishedTopics(null, null, null, PageRequest.of(0, 10));
    }

    @Test void khaiTopicsEndpointStillUsesItsOriginalControllerAndTemplate() throws Exception {
        when(rounds.findAllByOrderByCreatedAtDescIdDesc(Pageable.unpaged()))
                .thenReturn(Page.empty());
        mvc.perform(get("/topics").with(user("student").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(TopicRegistrationController.class))
                .andExpect(view().name("topics/list"));
        verifyNoInteractions(repository, departments);
    }
}