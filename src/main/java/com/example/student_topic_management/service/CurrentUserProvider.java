
package com.example.student_topic_management.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.student_topic_management.identity.service.CurrentUserService;

@Component
public class CurrentUserProvider {

    private final CurrentUserService currentUserService;

    public CurrentUserProvider(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public Long requireDeanUserId() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null ||
            !auth.isAuthenticated() ||
            auth.getAuthorities().stream()
                .noneMatch(a ->
                    a.getAuthority().equals("DEAN") ||
                    a.getAuthority().equals("ROLE_DEAN"))) {

            throw new AccessDeniedException(
                    "Bạn không có quyền trưởng khoa");
        }

        return currentUserService.getCurrentUserId();
    }
}
