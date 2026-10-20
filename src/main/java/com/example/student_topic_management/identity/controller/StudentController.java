package com.example.student_topic_management.identity.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasRole('ADMIN')")
public class StudentController {
    private final StudentAccountService studentAccountService;
    private final DepartmentRepository departmentRepository;

    public StudentController(StudentAccountService studentAccountService, DepartmentRepository departmentRepository) {
        this.studentAccountService = studentAccountService;
        this.departmentRepository = departmentRepository;
    }

    private String reloadForm(Model model) {
        // danh sach department
        model.addAttribute("departments", departmentRepository.findAll());
        return "users/student_form";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new CreateStudentForm());
        return reloadForm(model);
    }

    // Nhan du lieu tu form
    @PostMapping("/save")
    public String saveStudent(@Valid @ModelAttribute("form") CreateStudentForm form, BindingResult errors,
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
            // Loi nay hien thi ngay duoi input
            errors.reject("business", ex.getMessage());
            return reloadForm(model);
        } catch (DataIntegrityViolationException ex) {
            // Loi 2 request cung luc luu trung MSSV (Tu database)
            // errors.reject("conflict", "Lỗi dữ liệu: Thông tin định danh đã tồn tại trong
            // hệ thống.");
            // Loi nay se chuyen qua popup messages
            model.addAttribute("error", "Lỗi dữ liệu: Thông tin định danh đã tồn tại trong hệ thống.");
            return reloadForm(model);
        }

        // Khi thanh cong
        redirect.addFlashAttribute("success", "Đã tạo tài khoản sinh vien thành công!");
        return "redirect:/admin/users";
    }
}
