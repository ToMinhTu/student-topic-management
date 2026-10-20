package com.example.student_topic_management.identity.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.DepartmentForm;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.repository.DepartmentRepository;

@Service
public class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return repository.findAll();
    }

    public Department findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy bộ môn"));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void save(DepartmentForm form) {
        String name = form.getName().trim();

        // 1. Them moi
        if (form.getId() == null) {
            if (repository.existsByNameIgnoreCase(name)) {
                throw new BusinessException("Tên bộ môn đã tồn tại");
            }
            Department newDept = new Department(name, form.getDescription());
            repository.save(newDept);
        }
        // 2. Chinh sua
        else {
            Department existingDept = findById(form.getId());
            if (repository.existsByNameIgnoreCaseAndIdNot(name, form.getId())) {
                throw new BusinessException("Tên bộ môn đã bị trùng với bộ môn khác");
            }
            existingDept.setName(name);
            existingDept.setDescription(form.getDescription());
            repository.save(existingDept);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Department dept = findById(id);
        repository.delete(dept);
    }
}
