package com.example.student_topic_management.controller;

import com.example.student_topic_management.dto.PublishedTopicFilter;
import com.example.student_topic_management.dto.PublishedTopicView;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.repository.RegistrationRoundRepository;
import com.example.student_topic_management.service.PublishedTopicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@PreAuthorize("isAuthenticated()")
public class PublishedTopicController {

    private final PublishedTopicService publishedTopicService;
    private final RegistrationRoundRepository roundRepository;
    private final DepartmentRepository departmentRepository;

    public PublishedTopicController(PublishedTopicService publishedTopicService,
                                    RegistrationRoundRepository roundRepository,
                                    DepartmentRepository departmentRepository) {
        this.publishedTopicService = publishedTopicService;
        this.roundRepository = roundRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/catalog/topics")
    public String list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "roundId", required = false) Long roundId,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {
        PublishedTopicFilter requested = new PublishedTopicFilter(
                keyword, roundId, departmentId, page, size);
        Page<PublishedTopicView> topics;
        try {
            topics = publishedTopicService.search(requested);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }

        model.addAttribute("topics", topics);
        // Keep the effective size when the service caps the requested page size.
        model.addAttribute("filter", new PublishedTopicFilter(
                keyword, roundId, departmentId, topics.getNumber(), topics.getSize()));
        model.addAttribute("rounds", roundRepository
                .findAll(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
                .stream()
                .map(round -> new FilterOption(round.getId(), round.getName()))
                .toList());
        model.addAttribute("departments", departmentRepository
                .findAll(Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")))
                .stream()
                .map(department -> new FilterOption(department.getId(), department.getName()))
                .toList());
        return "catalog/topics";
    }

    public record FilterOption(Long id, String name) {
    }
}