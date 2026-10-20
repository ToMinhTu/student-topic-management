package com.example.student_topic_management.service;

import com.example.student_topic_management.dto.RoundForm;
import com.example.student_topic_management.entity.RegistrationRound;
import com.example.student_topic_management.enums.RoundStatus;
import com.example.student_topic_management.enums.RoundType;
import com.example.student_topic_management.repository.RegistrationRoundRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoundService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int PAGE_SIZE = 10;

    private final RegistrationRoundRepository roundRepository;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    @Autowired
    public RoundService(RegistrationRoundRepository roundRepository,
                        CurrentUserProvider currentUserProvider) {
        this(roundRepository, currentUserProvider, Clock.systemUTC());
    }

    // Đồng hồ cố định giúp kiểm thử chính xác tại thời điểm hết hạn.
    RoundService(RegistrationRoundRepository roundRepository,
                 CurrentUserProvider currentUserProvider, Clock clock) {
        this.roundRepository = roundRepository;
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<RegistrationRound> getAllRounds(int page) {
        currentUserProvider.requireDeanUserId();
        if (page < 0) {
            throw new IllegalArgumentException("Số trang không hợp lệ");
        }
        return roundRepository.findAllByOrderByCreatedAtDescIdDesc(PageRequest.of(page, PAGE_SIZE));
    }

    @Transactional(readOnly = true)
    public RegistrationRound getRoundById(Long id) {
        currentUserProvider.requireDeanUserId();
        validateId(id);
        return roundRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đợt: " + id));
    }

    @Transactional(readOnly = true)
    public RoundForm getEditForm(Long id) {
        RegistrationRound round = getRoundById(id);
        requireStatus(round, RoundStatus.DRAFT, "Chỉ được sửa đợt nháp");
        return toDisplayForm(round);
    }

    @Transactional
    public RegistrationRound createRound(RoundForm form) {
        Long userId = currentUserProvider.requireDeanUserId();
        validateRound(form);
        RegistrationRound round = new RegistrationRound();
        copyFormToEntity(form, round);
        round.setStatus(RoundStatus.DRAFT);
        round.setCreatedBy(userId);
        // MySQL tạo created_at; flush để phát hiện lỗi ràng buộc trong thao tác này.
        return roundRepository.saveAndFlush(round);
    }

    @Transactional
    public RegistrationRound updateRound(Long id, RoundForm form) {
        currentUserProvider.requireDeanUserId();
        validateId(id);
        validateRound(form);
        RegistrationRound round = lockedRound(id);
        requireStatus(round, RoundStatus.DRAFT, "Chỉ được sửa đợt nháp");
        copyFormToEntity(form, round);
        return roundRepository.saveAndFlush(round);
    }

    @Transactional
    public RegistrationRound openRound(Long id) {
        currentUserProvider.requireDeanUserId();
        RegistrationRound round = lockedRound(id);
        requireStatus(round, RoundStatus.DRAFT, "Chỉ được mở đợt nháp");
        Instant studentEnd = round.getStudentEndsAt().toInstant(ZoneOffset.UTC);
        if (!clock.instant().isBefore(studentEnd)) {
            throw new IllegalStateException("Đợt đã quá hạn đăng ký");
        }
        round.setStatus(RoundStatus.OPEN);
        return roundRepository.saveAndFlush(round);
    }

    @Transactional
    public RegistrationRound closeRound(Long id) {
        currentUserProvider.requireDeanUserId();
        RegistrationRound round = lockedRound(id);
        requireStatus(round, RoundStatus.OPEN, "Chỉ được đóng đợt đang mở");
        Instant studentEnd = round.getStudentEndsAt().toInstant(ZoneOffset.UTC);
        if (clock.instant().isBefore(studentEnd)) {
            throw new IllegalStateException("Chưa đến thời gian đóng đợt");
        }
        round.setStatus(RoundStatus.CLOSED);
        return roundRepository.saveAndFlush(round);
    }

    @Transactional
    public void deleteDraftRound(Long id) {
        currentUserProvider.requireDeanUserId();
        RegistrationRound round = lockedRound(id);
        requireStatus(round, RoundStatus.DRAFT, "Chỉ được xóa đợt nháp");
        try {
            // Để FK RESTRICT ngăn xóa dữ liệu liên quan, tránh truy vấn kiểm tra dễ bị tranh chấp.
            roundRepository.delete(round);
            roundRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Không thể xóa đợt vì còn dữ liệu liên quan", ex);
        }
    }

    // Kiểm tra ở service để cả lời gọi ngoài controller cũng tuân thủ nghiệp vụ.
    public void validateRound(RoundForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu đợt không được rỗng");
        }
        if (form.getName() == null || form.getName().isBlank() || form.getName().length() > 200) {
            throw new IllegalArgumentException("Vui lòng nhập tên đợt từ 1 đến 200 ký tự");
        }
        if (form.getRoundType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại đợt");
        }

        LocalDateTime proposalStart = form.getProposalStartsAt();
        LocalDateTime proposalEnd = form.getProposalEndsAt();
        LocalDateTime studentStart = form.getStudentStartsAt();
        LocalDateTime studentEnd = form.getStudentEndsAt();
        if (proposalStart == null || proposalEnd == null || studentStart == null || studentEnd == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thời gian hai giai đoạn");
        }
        if (!proposalStart.isBefore(proposalEnd)) {
            throw new IllegalArgumentException("Bắt đầu đề xuất phải trước kết thúc đề xuất");
        }
        if (proposalEnd.isAfter(studentStart)) {
            throw new IllegalArgumentException("Giai đoạn đăng ký phải bắt đầu sau hoặc đúng lúc kết thúc đề xuất");
        }
        if (!studentStart.isBefore(studentEnd)) {
            throw new IllegalArgumentException("Bắt đầu đăng ký phải trước kết thúc đăng ký");
        }

        Integer min = form.getGroupMinSize();
        Integer max = form.getGroupMaxSize();
        if (min == null || max == null || min < 1 || max > 5 || min > max) {
            throw new IllegalArgumentException("Số thành viên phải từ 1 đến 5; tối thiểu không vượt quá tối đa");
        }

        RoundType type = form.getRoundType();
        LocalDateTime reviewerDue = form.getReviewerDueAt();
        if (type == RoundType.SPECIALIZATION || type == RoundType.THESIS) {
            if (reviewerDue == null || !reviewerDue.isAfter(studentEnd)) {
                throw new IllegalArgumentException("Hạn phản biện phải sau khi kết thúc đăng ký");
            }
        } else if (reviewerDue != null) {
            throw new IllegalArgumentException("Loại đợt này không có hạn phản biện");
        }

        if (type == RoundType.THESIS) {
            if (form.getCouncilDate() == null || form.getCouncilDate().isBefore(studentEnd.toLocalDate())) {
                throw new IllegalArgumentException("Ngày hội đồng phải từ ngày kết thúc đăng ký trở đi");
            }
        } else if (form.getCouncilDate() != null) {
            throw new IllegalArgumentException("Chỉ đợt khóa luận tốt nghiệp có ngày hội đồng cấp đợt");
        }
        if (form.getReportDueAt() != null && !form.getReportDueAt().isAfter(studentEnd)) {
            throw new IllegalArgumentException("Hạn nộp báo cáo phải sau khi kết thúc đăng ký");
        }
    }

    // Tạo bản sao giờ Việt Nam; không thay đổi entity đang được JPA quản lý.
    public RoundForm toDisplayForm(RegistrationRound round) {
        RoundForm form = new RoundForm();
        form.setName(round.getName());
        form.setRoundType(round.getRoundType());
        form.setProposalStartsAt(toVietnamTime(round.getProposalStartsAt()));
        form.setProposalEndsAt(toVietnamTime(round.getProposalEndsAt()));
        form.setStudentStartsAt(toVietnamTime(round.getStudentStartsAt()));
        form.setStudentEndsAt(toVietnamTime(round.getStudentEndsAt()));
        form.setReviewerDueAt(toVietnamTime(round.getReviewerDueAt()));
        form.setCouncilDate(round.getCouncilDate());
        form.setReportDueAt(toVietnamTime(round.getReportDueAt()));
        form.setGroupMinSize(round.getGroupMinSize().intValue());
        form.setGroupMaxSize(round.getGroupMaxSize().intValue());
        return form;
    }

    private void copyFormToEntity(RoundForm form, RegistrationRound round) {
        round.setName(form.getName().strip());
        round.setRoundType(form.getRoundType());
        round.setProposalStartsAt(toUtc(form.getProposalStartsAt()));
        round.setProposalEndsAt(toUtc(form.getProposalEndsAt()));
        round.setStudentStartsAt(toUtc(form.getStudentStartsAt()));
        round.setStudentEndsAt(toUtc(form.getStudentEndsAt()));
        round.setReviewerDueAt(toUtc(form.getReviewerDueAt()));
        round.setCouncilDate(form.getCouncilDate());
        round.setReportDueAt(toUtc(form.getReportDueAt()));
        round.setGroupMinSize(form.getGroupMinSize().byteValue());
        round.setGroupMaxSize(form.getGroupMaxSize().byteValue());
    }

    private LocalDateTime toUtc(LocalDateTime vietnamTime) {
        return vietnamTime == null ? null
                : LocalDateTime.ofInstant(vietnamTime.atZone(VN_ZONE).toInstant(), ZoneOffset.UTC);
    }

    private LocalDateTime toVietnamTime(LocalDateTime utcTime) {
        return utcTime == null ? null
                : LocalDateTime.ofInstant(utcTime.toInstant(ZoneOffset.UTC), VN_ZONE);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID đợt không hợp lệ");
        }
    }

    private void requireStatus(RegistrationRound round, RoundStatus expected, String message) {
        if (round.getStatus() != expected) {
            throw new IllegalStateException(message);
        }
    }

    // Các thao tác ghi cùng khóa một bản ghi trong transaction trước khi kiểm tra trạng thái.
    private RegistrationRound lockedRound(Long id) {
        validateId(id);
        return roundRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đợt: " + id));
    }
}
