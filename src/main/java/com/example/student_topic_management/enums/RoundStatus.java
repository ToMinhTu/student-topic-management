package com.example.student_topic_management.enums;

public enum RoundStatus {
    DRAFT("Nháp"),
    OPEN("Đang mở"),
    CLOSED("Đã đóng");

    private final String label;

    RoundStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
