package com.example.student_topic_management.registration.service;

import java.time.Instant;

public interface RoundAccess {

    void lockRound(Long roundId);

    void requireStudentWindow(Long roundId, Instant now);
}