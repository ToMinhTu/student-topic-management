package com.example.student_topic_management.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnnouncementForm {
    private Long id;

    @NotBlank(message = "Vui lòng nhập tiêu đề thông báo")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Vui lòng nhập nội dung thông báo")
    private String content;

    public AnnouncementForm() {
    }

    public AnnouncementForm(Long id,
            @NotBlank(message = "Vui lòng nhập tiêu đề thông báo") @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự") String title,
            @NotBlank(message = "Vui lòng nhập nội dung thông báo") String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
