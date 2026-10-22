package com.example.student_topic_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudentTopicManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentTopicManagementApplication.class, args);
    }
}
