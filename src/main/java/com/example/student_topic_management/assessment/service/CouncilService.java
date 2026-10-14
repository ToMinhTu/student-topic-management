package com.example.student_topic_management.assessment.service;

import java.util.List;

import com.example.student_topic_management.assessment.dto.CouncilMemberForm;
import com.example.student_topic_management.assessment.entity.CouncilMember;
import com.example.student_topic_management.assessment.entity.CouncilMemberId;
import com.example.student_topic_management.assessment.repository.CouncilMemberRepository;

import org.springframework.stereotype.Service;

import com.example.student_topic_management.assessment.dto.CouncilForm;
import com.example.student_topic_management.assessment.entity.Council;
import com.example.student_topic_management.assessment.entity.CouncilStatus;
import com.example.student_topic_management.assessment.repository.CouncilRepository;

@Service
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;

    public CouncilService(
            CouncilRepository councilRepository,
            CouncilMemberRepository councilMemberRepository) {

        this.councilRepository = councilRepository;
        this.councilMemberRepository = councilMemberRepository;
    }

    public List<Council> findAll() {
        return councilRepository.findAll();
    }

    public List<Council> findByRoundId(Long roundId) {
        return councilRepository.findByRoundId(roundId);
    }

    public Council findById(Long id) {
        return councilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội đồng"));
    }

    public void saveCouncil(CouncilForm form) {
        Council council;

        if (form.getId() != null) {
            council = findById(form.getId());

            if (council.getStatus() != CouncilStatus.DRAFT) {
                throw new RuntimeException(
                        "Chỉ được sửa hội đồng đang ở trạng thái DRAFT");
            }
        } else {
            council = new Council();

            council.setStatus(CouncilStatus.DRAFT);
            council.setCreatedBy(1L);
        }

        council.setRoundId(form.getRoundId());
        council.setName(form.getName());

        councilRepository.save(council);
    }

    public void addMember(Long councilId, CouncilMemberForm form) {
        Council council = findById(councilId);

        if (council.getStatus() != CouncilStatus.DRAFT) {
            throw new RuntimeException(
                    "Chỉ được thêm thành viên khi hội đồng đang ở trạng thái DRAFT");
        }

        List<CouncilMember> currentMembers = councilMemberRepository.findByIdCouncilId(councilId);

        boolean duplicatedLecturer = currentMembers.stream()
                .anyMatch(member -> member.getId()
                        .getLecturerId()
                        .equals(form.getLecturerId()));

        if (duplicatedLecturer) {
            throw new RuntimeException(
                    "Giảng viên này đã có trong hội đồng");
        }

        boolean duplicatedSlot = currentMembers.stream()
                .anyMatch(member -> member.getSlotNo().intValue() == form.getSlotNo());

        if (duplicatedSlot) {
            throw new RuntimeException(
                    "Vị trí này đã được sử dụng");
        }

        if (currentMembers.size() >= 5) {
            throw new RuntimeException(
                    "Một hội đồng chỉ có tối đa 5 thành viên");
        }

        CouncilMemberId memberId = new CouncilMemberId(
                councilId,
                form.getLecturerId());

        CouncilMember member = new CouncilMember(
                memberId,
                form.getSlotNo().byteValue());

        councilMemberRepository.save(member);
    }

    public List<CouncilMember> findMembers(Long councilId) {
        return councilMemberRepository.findByIdCouncilId(councilId);
    }

}