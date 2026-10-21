package com.example.student_topic_management.controller;

import com.example.student_topic_management.dto.RoundForm;
import com.example.student_topic_management.enums.RoundType;
import com.example.student_topic_management.service.RoundService;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dean/rounds")
public class RoundController {

    private static final String LIST_REDIRECT = "redirect:/dean/rounds";
    private static final String DATA_CONFLICT_MESSAGE =
            "Dữ liệu không còn hợp lệ hoặc đang được sử dụng. Vui lòng kiểm tra và thử lại.";
    private static final String CONCURRENT_UPDATE_MESSAGE =
            "Đợt đang được cập nhật bởi thao tác khác. Vui lòng tải lại và thử lại.";

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("rounds", roundService.getAllRounds(page));
        return "rounds/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var round = roundService.getRoundById(id);
        model.addAttribute("round", round);
        model.addAttribute("schedule", roundService.toDisplayForm(round));
        return "rounds/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("roundForm", new RoundForm());
        return prepareForm(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("roundForm") RoundForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return prepareForm(model, null);
        }
        try {
            roundService.createRound(form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            result.reject("round.error", ex.getMessage());
            return prepareForm(model, null);
        } catch (DataIntegrityViolationException ex) {
            result.reject("round.conflict", DATA_CONFLICT_MESSAGE);
            return prepareForm(model, null);
        } catch (ConcurrencyFailureException ex) {
            result.reject("round.concurrent", CONCURRENT_UPDATE_MESSAGE);
            return prepareForm(model, null);
        }
        redirect.addFlashAttribute("message", "Tạo đợt thành công");
        return LIST_REDIRECT;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("roundForm", roundService.getEditForm(id));
        return prepareForm(model, id);
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("roundForm") RoundForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return prepareForm(model, id);
        }
        try {
            roundService.updateRound(id, form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            result.reject("round.error", ex.getMessage());
            return prepareForm(model, id);
        } catch (DataIntegrityViolationException ex) {
            result.reject("round.conflict", DATA_CONFLICT_MESSAGE);
            return prepareForm(model, id);
        } catch (ConcurrencyFailureException ex) {
            result.reject("round.concurrent", CONCURRENT_UPDATE_MESSAGE);
            return prepareForm(model, id);
        }
        redirect.addFlashAttribute("message", "Cập nhật đợt thành công");
        return LIST_REDIRECT + "/" + id;
    }

    @PostMapping("/{id}/open")
    public String open(@PathVariable Long id, RedirectAttributes redirect) {
        roundService.openRound(id);
        redirect.addFlashAttribute("message", "Mở đợt thành công");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, RedirectAttributes redirect) {
        roundService.closeRound(id);
        redirect.addFlashAttribute("message", "Đóng đợt thành công");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        roundService.deleteDraftRound(id);
        redirect.addFlashAttribute("message", "Xóa đợt nháp thành công");
        return LIST_REDIRECT;
    }

    // Các URL thao tác chỉ được thay đổi dữ liệu bằng POST. Nếu người dùng mở
    // trực tiếp URL trên trình duyệt (GET), đưa họ về trang chi tiết thay vì
    // hiển thị Whitelabel 405.
    @GetMapping({"/{id}/open", "/{id}/close", "/{id}/delete"})
    public String actionRequiresPost(@PathVariable Long id, RedirectAttributes redirect) {
        redirect.addFlashAttribute(
                "error",
                "Vui lòng sử dụng nút thao tác trên giao diện để thực hiện chức năng này.");
        return LIST_REDIRECT + "/" + id;
    }

    // Chỉ xử lý lỗi của module đợt; lỗi phân quyền vẫn do Spring Security xử lý.
    @ExceptionHandler({NoSuchElementException.class, IllegalArgumentException.class,
            IllegalStateException.class})
    public String handleRoundError(RuntimeException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return LIST_REDIRECT;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataConflict(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", DATA_CONFLICT_MESSAGE);
        return LIST_REDIRECT;
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public String handleConcurrentUpdate(RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", CONCURRENT_UPDATE_MESSAGE);
        return LIST_REDIRECT;
    }

    private String prepareForm(Model model, Long editId) {
        model.addAttribute("roundTypes", RoundType.values());
        model.addAttribute("editId", editId);
        return "rounds/form";
    }
}
