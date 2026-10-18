// toan-D2
// toan-D2 - thêm chức năng quản lý lịch bảo vệ
package com.example.student_topic_management.assessment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.student_topic_management.assessment.dto.DefenseForm;
import com.example.student_topic_management.assessment.entity.Council;
import com.example.student_topic_management.assessment.entity.CouncilStatus;
import com.example.student_topic_management.assessment.entity.Defense;
import com.example.student_topic_management.assessment.repository.CouncilRepository;
import com.example.student_topic_management.assessment.repository.DefenseRepository;

@Service
public class DefenseService {

    private final DefenseRepository defenseRepository;
    private final CouncilRepository councilRepository;

    public DefenseService(
            DefenseRepository defenseRepository,
            CouncilRepository councilRepository) {

        this.defenseRepository = defenseRepository;
        this.councilRepository = councilRepository;
    }

    public List<Defense> findAll() {
        return defenseRepository.findAll();
    }

    public List<Defense> findByRoundId(Long roundId) {
        return defenseRepository.findByRoundId(roundId);
    }

    public Defense findById(Long id) {
        return defenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy lịch bảo vệ"));
    }

    public void saveDefense(DefenseForm form) {

        Council council = councilRepository.findById(form.getCouncilId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy hội đồng"));

        if (council.getStatus() != CouncilStatus.ACTIVE) {
            throw new RuntimeException(
                    "Hội đồng phải ở trạng thái ACTIVE");
        }

        if (defenseRepository.existsByRegistrationId(
                form.getRegistrationId())) {

            throw new RuntimeException(
                    "Hồ sơ này đã có lịch bảo vệ");
        }

        if (form.getScheduledAt()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Thời gian bảo vệ phải ở tương lai");
        }

        if (!council.getRoundId()
                .equals(form.getRoundId())) {

            throw new RuntimeException(
                    "Hội đồng không thuộc đợt đăng ký này");
        }

        Defense defense = new Defense();

        defense.setRegistrationId(form.getRegistrationId());
        defense.setRoundId(form.getRoundId());
        defense.setCouncilId(form.getCouncilId());
        defense.setScheduledAt(form.getScheduledAt());
        defense.setLocation(form.getLocation());
        defense.setStatus("SCHEDULED");

        defenseRepository.save(defense);
    }
}