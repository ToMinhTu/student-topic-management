package com.example.student_topic_management.registration.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;
import com.example.student_topic_management.registration.service.GroupService;
import com.example.student_topic_management.registration.service.StudentAccess;

class GroupControllerTest {

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        GroupController controller = new GroupController(
                mock(StudentGroupRepository.class),
                mock(GroupMemberRepository.class),
                mock(JdbcTemplate.class),
                mock(GroupService.class),
                mock(Clock.class),
                mock(StudentAccess.class));

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void directGetToGroupActionRedirectsInsteadOfReturning405() throws Exception {
        mvc.perform(get("/groups/1/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/groups/1"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        containsString("sử dụng nút thao tác")));
    }
}
