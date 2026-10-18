package com.example.student_topic_management.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.student_topic_management.identity.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long>{
    // kiem tra trung ten khi tao moi
    boolean existsByNameIgnoreCase(String name);
    // Khi sua: kiem tra ten moi co trung vs bo mon khac hay khong
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
