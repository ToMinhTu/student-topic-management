package com.example.student_topic_management.b05;

import com.example.student_topic_management.dto.*;
import com.example.student_topic_management.repository.TopicRepository;
import com.example.student_topic_management.service.PublishedTopicService;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.domain.*;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PublishedTopicServiceB05Test {
    TopicRepository repository;
    PublishedTopicService service;

    @BeforeEach void setup() {
        repository = mock(TopicRepository.class);
        service = new PublishedTopicService(repository);
    }

    private PublishedTopicFilter filter(String keyword, Long round, Long department, Integer page, Integer size) {
        return new PublishedTopicFilter(keyword, round, department, page, size);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "  CODE-01  |%code-01%",
            "  Quản   Lý  |%quản lý%",
            "JAVA|%java%",
            "  java  |%java%",
            "100%_!|%100!%!_!!%"
    })
    void normalizesCodeTitleAndEscapesLiteralWildcards(String input, String expected) {
        service.search(filter(input, null, null, null, null));
        verify(repository).searchPublishedTopics(expected, null, null, PageRequest.of(0, 10));
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t\n", "\u00a0\u2003"})
    void blankKeywordDoesNotFilter(String keyword) {
        service.search(filter(keyword, null, null, null, null));
        verify(repository).searchPublishedTopics(null, null, null, PageRequest.of(0, 10));
    }

    @Test void nullFilterUsesDefaults() {
        service.search(null);
        verify(repository).searchPublishedTopics(null, null, null, PageRequest.of(0, 10));
    }

    @ParameterizedTest @CsvSource({"7,", ",9", "7,9"})
    void passesOptionalAndCombinedFilters(Long round, Long department) {
        service.search(filter("TOPIC", round, department, 2, 15));
        verify(repository).searchPublishedTopics("%topic%", round, department, PageRequest.of(2, 15));
    }

    @ParameterizedTest @ValueSource(ints = {100, 101, Integer.MAX_VALUE})
    void limitsPageSize(int size) {
        service.search(filter(null, null, null, 0, size));
        verify(repository).searchPublishedTopics(null, null, null, PageRequest.of(0, 100));
    }

    @ParameterizedTest @CsvSource({
            "-1,10,,", "0,0,,", "0,-1,,",
            "0,10,0,", "0,10,-1,", "0,10,,0", "0,10,,-1"
    })
    void rejectsInvalidPagingAndIds(int page, int size, Long round, Long department) {
        assertThrows(IllegalArgumentException.class,
                () -> service.search(filter(null, round, department, page, size)));
        verifyNoInteractions(repository);
    }

    @Test void rejectsOversizedKeyword() {
        assertThrows(IllegalArgumentException.class,
                () -> service.search(filter("x".repeat(256), null, null, null, null)));
        verifyNoInteractions(repository);
    }

    @Test void returnsProjectionPageWithoutExposingEntity() {
        Page<PublishedTopicView> expected = new PageImpl<>(List.of(
                new PublishedTopicView(1L, 7L, 9L, "CODE", "Title", "Description", null)));
        when(repository.searchPublishedTopics(null, null, null, PageRequest.of(0, 10))).thenReturn(expected);
        assertSame(expected, service.search(null));
        assertEquals(Set.of("id", "roundId", "departmentId", "code", "title", "description", "requirements"),
                Arrays.stream(PublishedTopicView.class.getRecordComponents())
                        .map(java.lang.reflect.RecordComponent::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Test void springTransactionInterceptorStartsReadOnlyTransaction() {
        PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(manager.getTransaction(any())).thenReturn(status);
        ProxyFactory factory = new ProxyFactory(service);
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(manager);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        factory.addAdvice(interceptor);
        ((PublishedTopicService) factory.getProxy()).search(null);
        verify(manager).getTransaction(argThat(definition -> definition.isReadOnly()));
        verify(manager).commit(status);
    }
}