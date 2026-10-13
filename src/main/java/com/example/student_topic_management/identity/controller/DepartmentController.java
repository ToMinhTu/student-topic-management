package com.example.student_topic_management.identity.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.student_topic_management.identity.dto.DepartmentForm;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.service.DepartmentService;

import jakarta.validation.Valid;

@Controller
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // Hien thi danh sach
    @GetMapping("departments")
    public String listDepartments(Model model) {
        List<Department> list = departmentService.findAll();
        model.addAttribute("departments", list);
        return "departments/list";
    }

    // Hien thi form them moi
    @GetMapping("/departments/new")
    public String showCreateForm(Model model) {
        model.addAttribute("departmentDTO", new DepartmentForm());
        return "departments/form";
    }

    // Nhan du lieu tu form gui len va luu
    @PostMapping("/departments/save")
    public String saveDepartment(@Valid @ModelAttribute("departmentDTO") DepartmentForm departmentDTO,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "departments/form";
        }

        departmentService.saveDepartment(departmentDTO);
        return "redirect:/departments";
    }
}
