package com.example.student_topic_management.identity.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.CreateStudentForm;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.identity.service.StudentAccountService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/students")
public class StudentController {
    private final StudentAccountService studentAccountService;
    private final DepartmentRepository departmentRepository;

    public StudentController(StudentAccountService studentAccountService, DepartmentRepository departmentRepository) {
        this.studentAccountService = studentAccountService;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new CreateStudentForm());
        // danh sach department
        model.addAttribute("departments", departmentRepository.findAll());
        return "students/new";
    }

    private String reloadForm(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "students/new";
    }

    // Nhan du lieu tu form
    @PostMapping 
    public String createStudent(@Valid @ModelAttribute("form") CreateStudentForm form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        // Neu nhap sai 
        if (errors.hasErrors()) {
            return reloadForm(model);
        }

        try {
            // Goi service luu vao DB
            studentAccountService.createStudentAccount(form);
        } catch (BusinessException ex) {
            // Loi nghiep vu: Trung username, email
            errors.reject("business", ex.getMessage());
            return reloadForm(model);
        } catch (DataIntegrityViolationException ex) {
            // Loi 2 request cung luc luu trung MSSV (Tu database)
            errors.reject("conflict", "Lỗi dữ liệu: Thông tin định danh đã tồn tại trong hệ thống.");
            return reloadForm(model);
        }

        // Khi thanh cong
        redirect.addFlashAttribute("success", "Đã tạo tài khoản sinh vien thành công!");
        return "redirect:/admin/students/new";
    }
}
