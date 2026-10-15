package com.example.student_topic_management.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
    Optional<Role> findByCode(String code);
}
