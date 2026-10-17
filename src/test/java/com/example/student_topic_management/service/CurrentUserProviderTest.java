package com.example.student_topic_management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.student_topic_management.identity.service.CurrentUserService;

class CurrentUserProviderTest {

    private final CurrentUserService currentUserService =
            mock(CurrentUserService.class);
    private final CurrentUserProvider currentUserProvider =
            new CurrentUserProvider(currentUserService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsCurrentUserIdForDean() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "dean", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_DEAN"))));
        when(currentUserService.getCurrentUserId()).thenReturn(42L);

        assertEquals(42L, currentUserProvider.requireDeanUserId());
    }

    @Test
    void rejectsNonDeanWithoutLookingUpCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "lecturer", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_LECTURER"))));

        assertThrows(
                AccessDeniedException.class,
                currentUserProvider::requireDeanUserId);
        verifyNoInteractions(currentUserService);
    }
}
