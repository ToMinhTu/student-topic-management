package com.example.student_topic_management.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateLecturerForm {
    @NotBlank(message = "Tên đăng nhập không để trống")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "Mật khẩu không để trống")
    @Size(min = 12, max = 72, message = "Mật khẩu tối thiểu 12 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không để trống")
    @Size(max = 150)
    private String fullname;

    @NotBlank(message = "Email không để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mã giảng viên không để trống")
    @Size(max = 30)
    private String lecturerCode;

    @NotNull(message = "Vui lòng chọn bộ môn")
    private Long departmentId;

    private boolean assignDean;
    private boolean assignDepartmentHead;

    public CreateLecturerForm() {
    }

    public CreateLecturerForm(@NotBlank(message = "Tên đăng nhập không để trống") @Size(max = 100) String username,
            @NotBlank(message = "Mật khẩu không để trống") @Size(min = 12, max = 72, message = "Mật khẩu tối thiểu 12 ký tự") String password,
            @NotBlank(message = "Họ tên không để trống") @Size(max = 150) String fullname,
            @NotBlank(message = "Email không để trống") @Email(message = "Email không hợp lệ") String email,
            @NotBlank(message = "Mã giảng viên không để trống") @Size(max = 30) String lecturerCode,
            @NotNull(message = "Vui lòng chọn bộ môn") Long departmentId, boolean assignDean,
            boolean assignDepartmentHead) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.lecturerCode = lecturerCode;
        this.departmentId = departmentId;
        this.assignDean = assignDean;
        this.assignDepartmentHead = assignDepartmentHead;
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

    public String getLecturerCode() {
        return lecturerCode;
    }

    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isAssignDean() {
        return assignDean;
    }

    public void setAssignDean(boolean assignDean) {
        this.assignDean = assignDean;
    }

    public boolean isAssignDepartmentHead() {
        return assignDepartmentHead;
    }

    public void setAssignDepartmentHead(boolean assignDepartmentHead) {
        this.assignDepartmentHead = assignDepartmentHead;
    }

}
