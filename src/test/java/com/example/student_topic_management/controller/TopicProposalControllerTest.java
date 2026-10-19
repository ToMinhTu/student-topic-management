package com.example.student_topic_management.controller;

import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.repository.TopicRepository;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TopicProposalControllerTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager em;
    @Autowired TopicRepository topics;
    private String username;
    private String otherUsername;
    private Long ownerId;
    private Long otherId;
    private Long departmentId;
    private Long roundId;
    private final String password = "B02-test-password";

    @BeforeEach void fixtures() {
        String key = UUID.randomUUID().toString();
        username = "b02-" + key;
        otherUsername = "other-" + key;
        jdbc.update("INSERT INTO departments(name) VALUES (?)", "B02-" + key);
        departmentId = jdbc.queryForObject("SELECT id FROM departments WHERE name=?", Long.class, "B02-" + key);
        ownerId = addUser(username, "LECTURER");
        otherId = addUser(otherUsername, "STUDENT");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC).withNano(0);
        jdbc.update("""
                INSERT INTO registration_rounds
                (name,round_type,proposal_starts_at,proposal_ends_at,student_starts_at,student_ends_at,status,created_by)
                VALUES (?,'COURSE',?,?,?,?,'OPEN',?)
                """, "B02-" + key, Timestamp.valueOf(now.minusDays(1)), Timestamp.valueOf(now.plusDays(1)),
                Timestamp.valueOf(now.plusDays(2)), Timestamp.valueOf(now.plusDays(3)), ownerId);
        roundId = jdbc.queryForObject("SELECT id FROM registration_rounds WHERE name=?", Long.class, "B02-" + key);
    }

    private Long addUser(String name, String role) {
        jdbc.update("INSERT INTO users(username,password_hash,full_name,email) VALUES (?,?,?,?)",
                name, encoder.encode(password), "B02 Test", name + "@example.test");
        Long id = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, name);
        jdbc.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE code=?", id, role);
        return id;
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mvc.perform(formLogin().user(username).password(password))
                .andExpect(authenticated().withUsername(username))
                .andReturn().getRequest().getSession(false);
    }

    private MockHttpServletRequestBuilder validPost(String path) {
        return validPost(path, "Đề xuất kiểm thử");
    }

    private MockHttpServletRequestBuilder validPost(String path, String title) {
        return post(path).with(csrf())
                .param("roundId", roundId.toString()).param("departmentId", departmentId.toString())
                .param("code", "B02-CODE").param("title", title)
                .param("description", "Mô tả kiểm thử").param("requirements", "Yêu cầu");
    }

    private Long create(MockHttpSession session) throws Exception {
        String url = mvc.perform(validPost("/topics").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("success"))
                .andReturn().getResponse().getRedirectedUrl();
        mvc.perform(get(url).session(session)).andExpect(status().isOk());
        return Long.valueOf(url.substring(url.lastIndexOf('/') + 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LECTURER", "STUDENT"})
    void loginRenderCreateEditSubmitAndVerifyMysql(String role) throws Exception {
        jdbc.update("DELETE FROM user_roles WHERE user_id=?", ownerId);
        jdbc.update("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE code=?", ownerId, role);
        MockHttpSession session = login();
        mvc.perform(get("/topics/new").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        String url = mvc.perform(validPost("/topics").session(session)
                        .param("proposedBy", otherId.toString()).param("status", "PUBLISHED"))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        Long id = Long.valueOf(url.substring(url.lastIndexOf('/') + 1));
        assertEquals(ownerId, jdbc.queryForObject("SELECT proposed_by FROM topics WHERE id=?", Long.class, id));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM topics WHERE id=?", String.class, id));
        mvc.perform(get(url).session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Đề xuất kiểm thử")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get(url + "/edit").session(session)).andExpect(status().isOk());
        mvc.perform(validPost(url, "Tên đã sửa").session(session))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("success"));
        assertEquals("Tên đã sửa", jdbc.queryForObject("SELECT title FROM topics WHERE id=?", String.class, id));
        mvc.perform(post(url + "/submit").session(session).with(csrf()))
                .andExpect(redirectedUrl(url)).andExpect(flash().attributeExists("success"));
        assertEquals("SUBMITTED", jdbc.queryForObject("SELECT status FROM topics WHERE id=?", String.class, id));
        mvc.perform(get("/topics/my").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Tên đã sửa")));
        mvc.perform(get(url).session(session)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Gửi đề xuất"))));
        mvc.perform(validPost(url).session(session)).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("topicForm"));
        assertEquals("Tên đã sửa", jdbc.queryForObject("SELECT title FROM topics WHERE id=?", String.class, id));
        mvc.perform(get(url + "/edit").session(session)).andExpect(redirectedUrl("/topics/my"))
                .andExpect(flash().attributeExists("error"));
        mvc.perform(post(url + "/submit").session(session).with(csrf()))
                .andExpect(redirectedUrl(url)).andExpect(flash().attributeExists("error"));
        em.clear();
        Topic reloaded = topics.findByIdAndProposedBy(id, ownerId).orElseThrow();
        assertEquals("Tên đã sửa", reloaded.getTitle());
        assertTrue(topics.findByIdAndProposedBy(id, otherId).isEmpty());
        assertEquals(1, topics.findByProposedBy(ownerId,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    @Test void requiresLogin() throws Exception {
        mvc.perform(get("/topics/my")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test void rejectsWrongRoleForEveryEndpoint() throws Exception {
        for (String path : new String[]{"/topics/my", "/topics/new", "/topics/1", "/topics/1/edit"}) {
            mvc.perform(get(path).with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        }
        for (String path : new String[]{"/topics", "/topics/1", "/topics/1/submit"}) {
            mvc.perform(post(path).with(user("admin").roles("ADMIN")).with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test void csrfIsRequiredForEveryMutation() throws Exception {
        MockHttpSession session = login();
        for (String path : new String[]{"/topics", "/topics/1", "/topics/1/submit"}) {
            mvc.perform(post(path).session(session)).andExpect(status().isForbidden());
            mvc.perform(post(path).session(session).with(csrf().useInvalidToken()))
                    .andExpect(status().isForbidden());
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM topics WHERE round_id=?", Integer.class, roundId));
    }

    @Test void cannotReadEditOrSubmitAnotherUsersTopic() throws Exception {
        Long id = create(login());
        mvc.perform(get("/topics/" + id).with(user(otherUsername).roles("STUDENT")))
                .andExpect(redirectedUrl("/topics/my")).andExpect(flash().attributeExists("error"));
        mvc.perform(get("/topics/" + id + "/edit").with(user(otherUsername).roles("STUDENT")))
                .andExpect(redirectedUrl("/topics/my"));
        mvc.perform(validPost("/topics/" + id).with(user(otherUsername).roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(model().attributeHasErrors("topicForm"));
        mvc.perform(post("/topics/" + id + "/submit").with(user(otherUsername).roles("STUDENT")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM topics WHERE id=?", String.class, id));
        mvc.perform(get("/topics/my").with(user(otherUsername).roles("STUDENT")))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("Đề xuất kiểm thử"))));
    }

    @Test void validatesFieldsAndPreservesInputOnError() throws Exception {
        mvc.perform(validPost("/topics", "").session(login()))
                .andExpect(status().isOk()).andExpect(view().name("topics/form"))
                .andExpect(model().attributeHasFieldErrors("topicForm", "title"))
                .andExpect(content().string(containsString("B02-CODE")));
    }

    @Test void duplicateCodeReturnsFormInsteadOf500() throws Exception {
        MockHttpSession session = login();
        create(session);
        mvc.perform(validPost("/topics").session(session)).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("topicForm"))
                .andExpect(content().string(containsString("Mã đề tài đã tồn tại")));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM topics WHERE round_id=?", Integer.class, roundId));
    }

    @Test void closedRoundSubmitReturnsVisibleFriendlyError() throws Exception {
        MockHttpSession session = login();
        Long id = create(session);
        jdbc.update("UPDATE registration_rounds SET status='CLOSED' WHERE id=?", roundId);
        em.clear();
        MvcResult result = mvc.perform(post("/topics/" + id + "/submit").session(session).with(csrf()))
                .andExpect(redirectedUrl("/topics/" + id)).andExpect(flash().attributeExists("error")).andReturn();
        mvc.perform(get("/topics/" + id).session(session).flashAttrs(result.getFlashMap()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("không trong thời gian đề xuất")))
                .andExpect(content().string(containsString("role=\"alert\"")));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM topics WHERE id=?", String.class, id));
    }

    @Test void expiredRoundRejectsSubmitWithoutChangingMysql() throws Exception {
        MockHttpSession session = login();
        Long id = create(session);
        jdbc.update("UPDATE registration_rounds SET proposal_ends_at=? WHERE id=?",
                Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1)), roundId);
        em.clear();
        mvc.perform(post("/topics/" + id + "/submit").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("error"));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM topics WHERE id=?", String.class, id));
    }

    @Test void databaseRejectsDuplicateEvenWithoutServicePrecheck() throws Exception {
        Long id = create(login());
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                INSERT INTO topics(round_id,department_id,code,title,description,proposed_by)
                SELECT round_id,department_id,code,title,description,proposed_by FROM topics WHERE id=?
                """, id));
    }

    @Test void databaseRejectsInvalidForeignKey() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                INSERT INTO topics(round_id,department_id,code,title,description,proposed_by)
                VALUES (?,?,?,'FK','FK',?)
                """, roundId, -1L, "INVALID-FK", ownerId));
    }

    @org.springframework.test.context.transaction.AfterTransaction
    void fixturesWereRolledBack() {
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username IN (?,?)",
                Integer.class, username, otherUsername));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM topics WHERE round_id=?", Integer.class, roundId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM registration_rounds WHERE id=?", Integer.class, roundId));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM departments WHERE id=?", Integer.class, departmentId));
    }

    @Test void invalidNumericIdRendersValidationError() throws Exception {
        mvc.perform(post("/topics").session(login()).with(csrf())
                        .param("roundId", "-1").param("departmentId", "not-a-number")
                        .param("code", "B02").param("title", "Valid").param("description", "Valid"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("topicForm", "roundId", "departmentId"));
    }

    @Test void missingTopicRedirectsWithoutServerError() throws Exception {
        mvc.perform(get("/topics/9223372036854775807").session(login()))
                .andExpect(redirectedUrl("/topics/my")).andExpect(flash().attributeExists("error"));
    }

    @Test void userContentIsEscapedWhenRendering() throws Exception {
        MockHttpSession session = login();
        String url = mvc.perform(validPost("/topics", "<script>alert(1)</script>").session(session))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        mvc.perform(get(url).session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

}
