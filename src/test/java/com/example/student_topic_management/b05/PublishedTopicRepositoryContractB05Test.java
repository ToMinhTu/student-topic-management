package com.example.student_topic_management.b05;

import com.example.student_topic_management.repository.TopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Declaration checks only: these do NOT parse JPQL or execute SQL.
 * No safe configured test database/H2/Testcontainers is available in this session.
 */
class PublishedTopicRepositoryContractB05Test {
    private Query query() throws Exception {
        return TopicRepository.class.getMethod("searchPublishedTopics",
                String.class, Long.class, Long.class, Pageable.class).getAnnotation(Query.class);
    }

    @Test void dataAndCountAlwaysRequirePublishedAndAllCombinedFilters() throws Exception {
        Query query = query();
        for (String jpql : new String[]{query.value(), query.countQuery()}) {
            assertTrue(jpql.contains("t.status = com.example.student_topic_management.enums.TopicStatus.PUBLISHED"));
            assertTrue(jpql.contains("AND (:roundId IS NULL OR t.roundId = :roundId)"));
            assertTrue(jpql.contains("AND (:departmentId IS NULL OR t.departmentId = :departmentId)"));
            assertTrue(jpql.contains("lower(t.code) LIKE :keywordPattern ESCAPE '!'"));
            assertTrue(jpql.contains("lower(t.title) LIKE :keywordPattern ESCAPE '!'"));
        }
        String dataWhere = query.value().substring(query.value().indexOf("WHERE"),
                query.value().indexOf("ORDER BY")).strip();
        String countWhere = query.countQuery().substring(query.countQuery().indexOf("WHERE")).strip();
        assertEquals(dataWhere, countWhere);
    }

    @Test void declaresStableOrderProjectionAndCount() throws Exception {
        Query query = query();
        assertTrue(query.value().contains("ORDER BY t.code ASC, t.id ASC"));
        assertTrue(query.value().contains("new com.example.student_topic_management.dto.PublishedTopicView("));
        assertTrue(query.countQuery().contains("SELECT count(t)"));
        assertFalse(query.countQuery().contains("ORDER BY"));
        assertFalse(query.nativeQuery());
        assertFalse(query.value().contains("t.proposedBy"));
        assertFalse(query.value().contains("t.departmentComment"));
        assertFalse(query.value().contains("t.rejectionReason"));
    }
}