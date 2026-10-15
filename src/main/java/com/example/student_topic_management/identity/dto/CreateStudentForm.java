package com.example.student_topic_management.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateStudentForm {
    @NotBlank (message = "Tên đăng nhập không được để trống")
    @Size (max = 100, message = "Tối đa 100 ký tự")
    private String username;

    @NotBlank (message = "Mật khẩu không được để trống")
    @Size (min = 12, max = 72, message = "Mật khẩu tối thiểu 12 ký tự")
    private String password;

    @NotBlank (message = "Họ tên không được để trống")
    @Size (max = 150)
    private String fullname;

    @NotBlank (message = "Email không được để trống")
    @Email (message = "Email không hợp lệ")
    @Size (max = 254)
    private String email;

    @NotBlank (message = "Mã số sinh viên không được để trống")
    @Size (max = 30)
    private String studentCode;

    @Size (max = 100)
    private String className;

    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long departmentId;

    public CreateStudentForm() {
    }

    public CreateStudentForm(
            @NotBlank(message = "Tên đăng nhập không được để trống") @Size(max = 100, message = "Tối đa 100 ký tự") String username,
            @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 12, max = 72, message = "Mật khẩu tối thiểu 12 ký tự") String password,
            @NotBlank(message = "Họ tên không được để trống") @Size(max = 150) String fullname,
            @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") @Size(max = 254) String email,
            @NotBlank(message = "Mã số sinh viên không được để trống") @Size(max = 30) String studentCode,
            @Size(max = 100) String className, Long departmentId) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.studentCode = studentCode;
        this.className = className;
        this.departmentId = departmentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    
}
