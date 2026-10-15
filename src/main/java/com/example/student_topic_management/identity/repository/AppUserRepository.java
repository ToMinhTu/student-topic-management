package com.example.student_topic_management.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    // nap san roles
    @EntityGraph(attributePaths = "roles")
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
