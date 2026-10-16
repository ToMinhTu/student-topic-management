package com.example.student_topic_management.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
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

}
