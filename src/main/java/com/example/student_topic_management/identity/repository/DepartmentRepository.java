package com.example.student_topic_management.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long>{
    
}
