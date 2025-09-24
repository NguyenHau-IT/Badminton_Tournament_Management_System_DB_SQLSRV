package com.example.btms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.btms.service.match.CourtManagerService;
import com.example.btms.service.scoreboard.ScoreboardRemote;

/**
 * Controller xử lý web interface cho scoreboard
 * Route: /scoreboard/{pin} - hiển thị giao diện nhập điểm
 */
@Controller
public class ScoreboardViewController {

    /**
     * Hiển thị giao diện scoreboard với mã PIN
     */
    @GetMapping("/scoreboard/{pin}")
    public String showScoreboard(@PathVariable String pin, Model model) {
        // Kiểm tra PIN có hợp lệ không
        if (pin == null || pin.trim().isEmpty()) {
            model.addAttribute("error", "Mã PIN không hợp lệ");
            return "error";
        }

        // Lấy thông tin sân từ CourtManagerService
        CourtManagerService courtManager = CourtManagerService.getInstance();
        java.util.Map<String, CourtManagerService.CourtStatus> allCourts = courtManager.getAllCourtStatus();

        // Tìm sân có mã PIN tương ứng
        String courtId = null;
        String header = null;
        for (CourtManagerService.CourtStatus court : allCourts.values()) {
            if (pin.equals(court.pinCode)) {
                courtId = court.courtId;
                header = court.header;
                break;
            }
        }

        // Lấy thông tin match hiện tại
        var match = ScoreboardRemote.get().match();
        var snapshot = match.snapshot();

        // Truyền dữ liệu vào model
        model.addAttribute("pinCode", pin); // Đổi tên để khớp với template
        model.addAttribute("courtInfo", courtId != null ? courtId : "Sân"); // Thông tin sân
        model.addAttribute("header", header); // Nội dung trận đấu
        model.addAttribute("pin", pin);
        model.addAttribute("match", snapshot);
        model.addAttribute("names", snapshot.names);
        model.addAttribute("score", snapshot.score);
        model.addAttribute("games", snapshot.games);
        model.addAttribute("gameNumber", snapshot.gameNumber);
        model.addAttribute("bestOf", snapshot.bestOf);
        model.addAttribute("doubles", snapshot.doubles);

        return "scoreboard";
    }

    /**
     * Hiển thị giao diện nhập PIN
     */
    @GetMapping("/pin")
    public String showPinEntry() {
        return "pin-entry";
    }
}
