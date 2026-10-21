package com.example.student_topic_management.assessment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.student_topic_management.assessment.dto.DefenseForm;
import com.example.student_topic_management.assessment.repository.CouncilMemberRepository;
import com.example.student_topic_management.assessment.repository.CouncilRepository;
import com.example.student_topic_management.assessment.repository.DefenseRepository;
import com.example.student_topic_management.assessment.repository.ReviewerAssignmentRepository;
import com.example.student_topic_management.identity.service.CurrentUserService;

class DefenseServiceTest {

    private JdbcTemplate jdbc;
    private DefenseService service;

    @BeforeEach
    void setup() {
        jdbc = mock(JdbcTemplate.class);
        service = new DefenseService(
                mock(DefenseRepository.class),
                mock(CouncilRepository.class),
                mock(CouncilMemberRepository.class),
                mock(ReviewerAssignmentRepository.class),
                mock(CurrentUserService.class),
                jdbc);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void missingRegistrationReturnsReadableMessage() {
        when(jdbc.query(anyString(), any(RowMapper.class), eq(99L)))
                .thenReturn(List.of());

        DefenseForm form = new DefenseForm();
        form.setRegistrationId(99L);
        form.setRoundId(36L);
        form.setCouncilId(2L);
        form.setScheduledAt(LocalDateTime.now().plusDays(1));
        form.setLocation("A101");

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> service.saveDefense(form));

        assertEquals("Không tìm thấy hồ sơ đăng ký #99", error.getMessage());
    }
}
