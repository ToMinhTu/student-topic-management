package com.example.student_topic_management.service;

import com.example.student_topic_management.dto.RoundForm;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.RoundStatus;
import com.example.student_topic_management.enums.RoundType;
import com.example.student_topic_management.repository.RegistrationRoundRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// Mỗi test dùng transaction tự rollback, không giữ lại bản ghi thử trong MySQL.
@SpringBootTest
@Transactional
class RoundPersistenceTest {
    @Autowired private RoundService service;
    @Autowired private RegistrationRoundRepository repository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @MockitoBean private CurrentUserProvider user;

    @BeforeEach
    void currentUser() {
        Long userId = jdbc.queryForObject("SELECT MIN(id) FROM users", Long.class);
        org.junit.jupiter.api.Assumptions.assumeTrue(userId != null, "Cần ít nhất một tài khoản để kiểm thử FK");
        when(user.requireDeanUserId()).thenReturn(userId);
    }

    private RoundForm form(RoundType type) {
        LocalDateTime start = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(10)
                .withSecond(0).withNano(0);
        RoundForm form = new RoundForm();
        form.setName("Kiểm thử tự rollback");
        form.setRoundType(type);
        form.setProposalStartsAt(start);
        form.setProposalEndsAt(start.plusDays(1));
        form.setStudentStartsAt(start.plusDays(1));
        form.setStudentEndsAt(start.plusDays(2));
        if (type == RoundType.SPECIALIZATION || type == RoundType.THESIS) {
            form.setReviewerDueAt(start.plusDays(3));
        }
        if (type == RoundType.THESIS) {
            form.setCouncilDate(start.plusDays(4).toLocalDate());
        }
        return form;
    }

    @ParameterizedTest
    @EnumSource(RoundType.class)
    void persistsAllTypesAgainstMysqlConstraintsAndReloadsVietnamTime(RoundType type) {
        RoundForm input = form(type);
        RegistrationRound saved = service.createRound(input);
        Long id = saved.getId();
        entityManager.clear();
        RegistrationRound loaded = service.getRoundById(id);
        assertNotNull(loaded.getCreatedAt());
        assertEquals(type, loaded.getRoundType());
        assertEquals(RoundStatus.DRAFT, loaded.getStatus());
        assertEquals(input.getProposalStartsAt().minusHours(7), loaded.getProposalStartsAt());
        assertEquals(input.getStudentEndsAt(), service.getEditForm(id).getStudentEndsAt());
        assertFalse(service.getAllRounds(0).isEmpty());
    }

    @Test
    void updatesThenOpensDraftInDatabase() {
        RegistrationRound round = service.createRound(form(RoundType.COURSE));
        RoundForm edit = form(RoundType.THESIS);
        edit.setName("Đã chỉnh sửa");
        service.updateRound(round.getId(), edit);
        entityManager.clear();
        assertEquals("Đã chỉnh sửa", service.getRoundById(round.getId()).getName());
        service.openRound(round.getId());
        entityManager.clear();
        assertEquals(RoundStatus.OPEN, service.getRoundById(round.getId()).getStatus());
    }

    @Test
    void deletesDraftInDatabase() {
        Long id = service.createRound(form(RoundType.COURSE)).getId();
        service.deleteDraftRound(id);
        entityManager.clear();
        assertFalse(repository.existsById(id));
    }

    @Test
    void closesExpiredOpenRoundInDatabase() {
        RegistrationRound round = service.createRound(form(RoundType.COURSE));
        round.setStatus(RoundStatus.OPEN);
        round.setProposalStartsAt(round.getProposalStartsAt().minusDays(20));
        round.setProposalEndsAt(round.getProposalEndsAt().minusDays(20));
        round.setStudentStartsAt(round.getStudentStartsAt().minusDays(20));
        round.setStudentEndsAt(round.getStudentEndsAt().minusDays(20));
        repository.flush();
        service.closeRound(round.getId());
        entityManager.clear();
        assertEquals(RoundStatus.CLOSED, service.getRoundById(round.getId()).getStatus());
    }
}
