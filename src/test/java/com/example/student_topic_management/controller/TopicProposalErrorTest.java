package com.example.student_topic_management.controller;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.service.TopicProposalService;
import org.junit.jupiter.api.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TopicProposalErrorTest {
    private TopicProposalService service;
    private MockMvc mvc;

    @BeforeEach void setup() {
        service = mock(TopicProposalService.class);
        mvc = MockMvcBuilders.standaloneSetup(new TopicProposalController(service)).build();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder form(String path) {
        return post(path).param("roundId", "1").param("departmentId", "2")
                .param("code", "CODE").param("title", "Title").param("description", "Description");
    }

    @Test void createConstraintFailureKeepsFormWithoutLeakingSql() throws Exception {
        when(service.createDraft(any())).thenThrow(new DataIntegrityViolationException("SECRET SQL"));
        mvc.perform(form("/topics")).andExpect(status().isOk()).andExpect(view().name("topics/form"))
                .andExpect(model().attributeHasErrors("topicForm"))
                .andExpect(model().attribute("editMode", false));
    }

    @Test void updateConstraintFailureKeepsEditContext() throws Exception {
        when(service.updateDraft(eq(9L), any())).thenThrow(new DataIntegrityViolationException("SECRET SQL"));
        mvc.perform(form("/topics/9")).andExpect(status().isOk()).andExpect(view().name("topics/form"))
                .andExpect(model().attributeHasErrors("topicForm"))
                .andExpect(model().attribute("topicId", 9L)).andExpect(model().attribute("editMode", true));
    }

    @Test void submitFailureRedirectsWithSafeMessage() throws Exception {
        when(service.submitProposal(9L)).thenThrow(new BusinessException("Đợt đã đóng"));
        mvc.perform(post("/topics/9/submit")).andExpect(redirectedUrl("/topics/9"))
                .andExpect(flash().attribute("error", "Đợt đã đóng"))
                .andExpect(flash().attributeCount(1));
    }

    @Test void missingDetailRedirectsToList() throws Exception {
        when(service.getMyTopicDetail(9L)).thenThrow(new BusinessException("Không có quyền xem"));
        mvc.perform(get("/topics/9")).andExpect(redirectedUrl("/topics/my"))
                .andExpect(flash().attribute("error", "Không có quyền xem"));
    }
}
