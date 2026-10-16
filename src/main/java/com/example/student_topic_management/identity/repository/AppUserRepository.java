package com.example.student_topic_management.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.student_topic_management.identity.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // nap san roles
    @EntityGraph(attributePaths = "roles")
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Dem so luong Admin dang enabled = true
    @Query("SELECT COUNT(u) FROM AppUser u JOIN u.roles r WHERE r.code = 'ADMIN' AND u.enabled = true")
    long countActiveAdmins();
}
