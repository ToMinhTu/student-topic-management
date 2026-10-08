package com.example.student_topic_management.identity.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.repository.DepartmentRepository;

@Controller 
public class DepartmentController {
    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping ("/departments")
    public String listDepartments(Model model) {
        List<Department> list = departmentRepository.findAll();
        model.addAttribute("departments", list);
        return "departments/list";
    }
}
