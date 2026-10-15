package com.example.student_topic_management.registration.policy;

import com.example.student_topic_management.registration.entity.GroupStatus;
import com.example.student_topic_management.registration.entity.StudentGroup;

import org.springframework.stereotype.Component;

@Component
public class GroupPolicy {

    public void requireForming(StudentGroup group) {

        if (group == null) {
            throw new IllegalArgumentException(
                "Nhóm không tồn tại"
            );
        }

        if (group.getStatus() != GroupStatus.FORMING) {
            throw new IllegalStateException(
                "Nhóm đã bị khóa hoặc hoàn thành"
            );
        }
    }
}
