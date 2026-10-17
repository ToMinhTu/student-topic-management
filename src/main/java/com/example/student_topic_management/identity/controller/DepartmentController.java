package com.example.student_topic_management.identity.controller;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.student_topic_management.common.exception.BusinessException;
import com.example.student_topic_management.identity.dto.DepartmentForm;
import com.example.student_topic_management.identity.entity.Department;
import com.example.student_topic_management.identity.service.DepartmentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/departments")
public class DepartmentController {
    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    // 1. Hien thi danh sach
    @GetMapping
    public String list(Model model) {
        model.addAttribute("departments", service.findAll());
        return "departments/list";
    }

    // 2. Hien thi form them moi
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new DepartmentForm());
        return "departments/form";
    }

    // 3. Hien form chinh sua
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Department dept = service.findById(id);
        DepartmentForm form = new DepartmentForm();
        form.setId(dept.getId());
        form.setName(dept.getName());
        form.setDescription(dept.getDescription());

        model.addAttribute("form", form);
        return "departments/form";
    }

    // 4. Nhan du lieu luu (Cho ca them va sua)
    @PostMapping("/save")
    public String saveDepartment(@Valid @ModelAttribute("form") DepartmentForm form,
            BindingResult errors, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return "departments/form";
        }
        try {
            service.save(form);
            redirect.addFlashAttribute("success", "Lưu bộ môn thành công!");
        } catch (BusinessException ex) {
            errors.rejectValue("name", "error.form", ex.getMessage());
            return "departments/form";
        }
        return "redirect:/admin/departments";
    }

    // 5. Xoa bo mon
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xoá bộ môn thành công!");
        } catch (DataIntegrityViolationException ex) {
            // Loi khoa ngoai: Do bo mon da co giang vien/sinh vien tham chieu
            redirect.addFlashAttribute("error", "Không thể xóa bộ môn đã có sinh viên hoặc giảng viên trực thuộc!");
        }
        return "redirect:/admin/departments";
    }
}
