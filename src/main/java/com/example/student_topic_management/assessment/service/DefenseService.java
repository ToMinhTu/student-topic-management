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
import com.example.student_topic_management.repository.RegistrationRoundRepository;

@Service
public class DefenseService {

    private final DefenseRepository defenseRepository;
    private final CouncilRepository councilRepository;
    private final RegistrationRoundRepository roundRepository;

    public DefenseService(
            DefenseRepository defenseRepository,
            CouncilRepository councilRepository,
            RegistrationRoundRepository roundRepository) {

        this.defenseRepository = defenseRepository;
        this.councilRepository = councilRepository;
        this.roundRepository = roundRepository;
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

        validateCouncilDate(form);

        Defense defense = new Defense();

        defense.setRegistrationId(form.getRegistrationId());
        defense.setRoundId(form.getRoundId());
        defense.setCouncilId(form.getCouncilId());
        defense.setScheduledAt(form.getScheduledAt());
        defense.setLocation(form.getLocation());
        defense.setStatus("SCHEDULED");

        defenseRepository.save(defense);
    }

    public DefenseForm getForm(Long id) {
        Defense defense = findById(id);
        DefenseForm form = new DefenseForm();
        form.setId(defense.getId());
        form.setRegistrationId(defense.getRegistrationId());
        form.setRoundId(defense.getRoundId());
        form.setCouncilId(defense.getCouncilId());
        form.setScheduledAt(defense.getScheduledAt());
        form.setLocation(defense.getLocation());
        return form;
    }

    public void updateDefense(DefenseForm form) {
        Defense defense = findById(form.getId());
        Council council = councilRepository.findById(form.getCouncilId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội đồng"));
        if (council.getStatus() != CouncilStatus.ACTIVE) {
            throw new RuntimeException("Hội đồng phải ở trạng thái ACTIVE");
        }
        if (form.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian bảo vệ phải ở tương lai");
        }
        if (!council.getRoundId().equals(form.getRoundId())) {
            throw new RuntimeException("Hội đồng không thuộc đợt đăng ký này");
        }
        validateCouncilDate(form);
        defense.setRegistrationId(form.getRegistrationId());
        defense.setRoundId(form.getRoundId());
        defense.setCouncilId(form.getCouncilId());
        defense.setScheduledAt(form.getScheduledAt());
        defense.setLocation(form.getLocation());
        defenseRepository.save(defense);
    }

    public void deleteDefense(Long id) {
        defenseRepository.deleteById(id);
    }

    private void validateCouncilDate(DefenseForm form) {
        var round = roundRepository.findById(form.getRoundId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt đăng ký"));
        if (round.getCouncilDate() != null) {
            if (!round.getCouncilDate().equals(form.getScheduledAt().toLocalDate())) {
                throw new RuntimeException("Ngày bảo vệ phải trùng ngày hội đồng của đợt đăng ký");
            }
            return;
        }

        if (round.getReportDueAt() == null
                || form.getScheduledAt().isAfter(round.getReportDueAt())) {
            throw new RuntimeException("Ngày bảo vệ phải trước hoặc bằng hạn báo cáo đề tài");
        }
    }
}
