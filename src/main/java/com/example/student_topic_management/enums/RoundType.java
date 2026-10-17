package com.example.student_topic_management.enums;

public enum RoundType {
    COURSE("Đồ án môn học"),
    RESEARCH("Nghiên cứu khoa học"),
    SPECIALIZATION("Tiểu luận chuyên ngành"),
    THESIS("Khóa luận tốt nghiệp");

    private final String label;

    RoundType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
