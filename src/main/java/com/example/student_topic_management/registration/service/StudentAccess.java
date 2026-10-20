package com.example.student_topic_management.registration.service;

public interface StudentAccess {

    Long requireCurrentStudentId();

    void lockEnabledStudent(Long studentId);
}