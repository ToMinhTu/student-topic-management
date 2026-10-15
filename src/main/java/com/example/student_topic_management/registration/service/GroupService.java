package com.example.student_topic_management.registration.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.student_topic_management.registration.dto.GroupCreateForm;
import com.example.student_topic_management.registration.entity.GroupMember;
import com.example.student_topic_management.registration.entity.StudentGroup;
import com.example.student_topic_management.registration.repository.GroupMemberRepository;
import com.example.student_topic_management.registration.repository.StudentGroupRepository;

@Service
public class GroupService {

    private final StudentGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final StudentAccess studentAccess;
    private final RoundAccess roundAccess;
    private final Clock clock;

    public GroupService(
            StudentGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            StudentAccess studentAccess,
            RoundAccess roundAccess,
            Clock clock) {

        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.studentAccess = studentAccess;
        this.roundAccess = roundAccess;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Long createGroup(GroupCreateForm form) {

        if (form == null || form.getRoundId() == null) {
            throw new IllegalArgumentException("Đợt đăng ký không hợp lệ");
        }

        if (form.getName() == null ||
                form.getName().trim().isEmpty() ||
                form.getName().trim().length() > 150) {
            throw new IllegalArgumentException("Tên nhóm không hợp lệ");
        }

        // Lấy sinh viên thật từ phiên đăng nhập
        Long studentId = studentAccess.requireCurrentStudentId();

        // Khóa đúng thứ tự: Round -> Student
        roundAccess.lockRound(form.getRoundId());
        studentAccess.lockEnabledStudent(studentId);

        // Kiểm tra thời gian đăng ký
        roundAccess.requireStudentWindow(
                form.getRoundId(),
                clock.instant()
        );

        // Một sinh viên không được có hai nhóm active
        if (memberRepository.existsByStudentIdAndLeftAtIsNull(studentId)) {
            throw new IllegalStateException(
                    "Bạn đã tham gia một nhóm khác"
            );
        }

        // Tạo nhóm
        StudentGroup group = new StudentGroup(
                form.getRoundId(),
                form.getName().trim()
        );

        group = groupRepository.save(group);

        // Thêm trưởng nhóm vào slot 1
        GroupMember leader = new GroupMember(
                group,
                studentId,
                (byte) 1
        );

        memberRepository.save(leader);

        return group.getId();
    }
}