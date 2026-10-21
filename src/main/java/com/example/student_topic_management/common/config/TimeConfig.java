package com.example.student_topic_management.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class TimeConfig {
    // Test thoi gian
    @Bean 
    public Clock applicationClock() {return Clock.systemUTC();}
}
