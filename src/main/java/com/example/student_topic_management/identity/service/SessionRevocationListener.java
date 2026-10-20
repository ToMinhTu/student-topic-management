package com.example.student_topic_management.identity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SessionRevocationListener {
    private final SessionRegistry registry;

    public SessionRevocationListener(SessionRegistry registry) {
        this.registry = registry;
    }

    // Lang nghe event sau khi thay doi (lock user/change password) duoc commit
    // xuong DB
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChange(AccountAccessChanged event) {
        // Lay ID session cua trinh duyet dang thuc hien
        String currentSessionId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getDetails() instanceof WebAuthenticationDetails details) {
            currentSessionId = details.getSessionId();
        }

        // Tim tat ca user dang online trong SessionRegistry
        for (Object principal : registry.getAllPrincipals()) {
            if (principal instanceof UserDetails details && details.getUsername().equals(event.username())) {
                // Thu hoi (expire) cac phien login cua user
                for (SessionInformation session : registry.getAllSessions(principal, false)) {
                    // Chi xoa nhung session tu trinh duyet khac
                    // Tranh viec tu logout chinh minh
                    // Admin lock user khac thi khong bi sao
                    if (currentSessionId == null || !session.getSessionId().equals(currentSessionId)) {
                        session.expireNow();
                    }
                }
            }
        }
    }
}
