package com.example.student_topic_management.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnnouncementForm {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    public AnnouncementForm(
            @NotBlank(message = "Tiêu đề không được để trống") @Size(max = 255, message = "Tiêu đề không quá 255 ký tự") String title,
            @NotBlank(message = "Nội dung không được để trống") String content) {
        this.title = title;
        this.content = content;
    }
}
