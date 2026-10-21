package com.example.student_topic_management.service;

import com.example.student_topic_management.dto.PublishedTopicFilter;
import com.example.student_topic_management.dto.PublishedTopicView;
import com.example.student_topic_management.repository.TopicRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class PublishedTopicService {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private final TopicRepository topicRepository;

    public PublishedTopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * Searches all published topics, independently of registration eligibility.
     * Unknown positive filter IDs produce an empty page.
     */
    public Page<PublishedTopicView> search(PublishedTopicFilter filter) {
        PublishedTopicFilter criteria = filter == null
                ? new PublishedTopicFilter(null, null, null, null, null)
                : filter;
        requirePositiveId(criteria.roundId(), "Đợt đăng ký");
        requirePositiveId(criteria.departmentId(), "Bộ môn");

        int page = criteria.page() == null ? 0 : criteria.page();
        int size = criteria.size() == null ? DEFAULT_PAGE_SIZE : criteria.size();
        if (page < 0) {
            throw new IllegalArgumentException("Số trang không được âm");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Kích thước trang phải lớn hơn 0");
        }
        size = Math.min(size, MAX_PAGE_SIZE);

        return topicRepository.searchPublishedTopics(
                toKeywordPattern(criteria.keyword()),
                criteria.roundId(),
                criteria.departmentId(),
                PageRequest.of(page, size));
    }

    private void requirePositiveId(Long id, String field) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException(field + " phải có ID lớn hơn 0");
        }
    }

    private String toKeywordPattern(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.strip()
                .replaceAll("[\\p{javaWhitespace}\\p{Zs}]+", " ")
                .strip()
                .toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 255) {
            throw new IllegalArgumentException("Từ khóa không được vượt quá 255 ký tự");
        }
        // Treat user-supplied LIKE wildcards as literal characters.
        return "%" + normalized.replace("!", "!!")
                .replace("%", "!%").replace("_", "!_") + "%";
    }
}