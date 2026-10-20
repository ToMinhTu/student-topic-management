package com.example.student_topic_management.registration.service;

public interface GroupRegistrationAccess {

    void requireNoActiveRegistration(Long groupId);
}