package com.example.student_topic_management.identity.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.student_topic_management.identity.dto.DepartmentForm;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.repository.DepartmentRepository;

@Service 
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public void saveDepartment(DepartmentForm dto) {
        Department dept = new Department();

        // check neu co id thi chuyen qua sua
        if(dto.getId() != null) {
            dept = departmentRepository.findById(dto.getId()).orElse(new Department());
        }

        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());

        departmentRepository.save(dept);
    }
}
