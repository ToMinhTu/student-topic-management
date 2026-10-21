package com.example.student_topic_management.b04;

import com.example.student_topic_management.entity.Topic;
import com.example.student_topic_management.enums.TopicStatus;
import com.example.student_topic_management.identity.entity.AppUser;
import com.example.student_topic_management.identity.entity.LecturerProfile;
import com.example.student_topic_management.identity.entity.Role;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.test.util.ReflectionTestUtils;

final class B04Fixtures {
    private B04Fixtures() {}

    static Topic topic(TopicStatus status) {
        Topic topic = new Topic();
        ReflectionTestUtils.setField(topic, "id", 10L);
        topic.setRoundId(3L);
        topic.setDepartmentId(4L);
        topic.setCode("B04-TOPIC");
        topic.setTitle("Đề tài B04");
        topic.setStatus(status);
        return topic;
    }

    static AppUser user(Long id, String name, boolean enabled, String... roles) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("b04-" + id);
        user.setFullname(name);
        user.setEnabled(enabled);
        user.setRoles(Arrays.stream(roles).map(code -> new Role(1L, code)).collect(Collectors.toSet()));
        return user;
    }

    static LecturerProfile lecturer(Long id, String name, boolean enabled, String... roles) {
        LecturerProfile profile = new LecturerProfile();
        profile.setUserid(id);
        profile.setLecturerCode("GV-" + id);
        profile.setUser(user(id, name, enabled, roles));
        return profile;
    }
}
