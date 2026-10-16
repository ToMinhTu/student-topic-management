package com.example.student_topic_management.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentForm {
    private Long id;

    @NotBlank(message = "Tên Bộ môn không được để trống")
    @Size(max = 150, message = "Tên Bộ môn tối đa 150 ký tự")
    private String name;

    private String description;

    public DepartmentForm(Long id,
            @NotBlank(message = "Tên Bộ môn không được để trống") @Size(max = 150, message = "Tên Bộ môn tối đa 150 ký tự") String name,
            String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
