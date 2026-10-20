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
import com.example.student_topic_management.identity.dto.CreateLecturerForm;
import com.example.student_topic_management.identity.repository.DepartmentRepository;
import com.example.student_topic_management.identity.service.LecturerAccountService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/lecturers")
@PreAuthorize("hasRole('ADMIN')")
public class LecturerController {
    private final LecturerAccountService lecturerAccountService;
    private final DepartmentRepository departmentRepository;

    public LecturerController(LecturerAccountService lecturerAccountService,
            DepartmentRepository departmentRepository) {
        this.lecturerAccountService = lecturerAccountService;
        this.departmentRepository = departmentRepository;
    }

    private String reloadForm(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "users/lecturer_form";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new CreateLecturerForm());
        return reloadForm(model);
    }

    @PostMapping("/save")
    public String saveLecturer(@Valid @ModelAttribute("form") CreateLecturerForm form, BindingResult errors,
            Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return reloadForm(model);
        }

        try {
            lecturerAccountService.createLecturerAccount(form);
        } catch (BusinessException ex) {
            // Loi nghiep vu hien thi ngay duoi input
            errors.reject("business", ex.getMessage());
            return reloadForm(model);
        } catch (DataIntegrityViolationException ex) {
            // errors.reject("conflict", "Thông tin định danh đã tồn tại.");
            // Loi trung request se gui qua popup messages.
            model.addAttribute("error", "Lỗi dữ liệu: Thông tin định danh đã tồn tại trong hệ thống.");
            return reloadForm(model);
        }
        redirect.addFlashAttribute("success", "Đã tạo tài khoản Giảng viên thành công!");
        return "redirect:/admin/users";
    }
}
