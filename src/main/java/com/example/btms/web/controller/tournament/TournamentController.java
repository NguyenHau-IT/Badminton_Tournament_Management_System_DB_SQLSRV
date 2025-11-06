package com.example.btms.web.controller.tournament;

import java.sql.SQLException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.btms.model.tournament.GiaiDau;
import com.example.btms.service.tournament.GiaiDauService;

/**
 * Controller xử lý các request liên quan đến Tournament Platform
 * - Danh sách giải đấu
 * - Chi tiết giải đấu
 * - Ranking
 * - Schedule
 * - Statistics
 */
@Controller
@RequestMapping("/tournament")
public class TournamentController {

    private final GiaiDauService giaiDauService;

    public TournamentController(GiaiDauService giaiDauService) {
        this.giaiDauService = giaiDauService;
    }

    /**
     * Hiển thị danh sách tất cả giải đấu
     * GET /tournament/list
     */
    @GetMapping("/list")
    public String listTournaments(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<GiaiDau> tournaments = giaiDauService.getAllGiaiDau();
            model.addAttribute("tournaments", tournaments);
            model.addAttribute("totalTournaments", tournaments.size());
            
            // Thống kê nhanh
            long activeTournaments = tournaments.stream()
                    .filter(GiaiDau::isActive)
                    .count();
            long upcomingTournaments = tournaments.stream()
                    .filter(GiaiDau::isUpcoming)
                    .count();
            long finishedTournaments = tournaments.stream()
                    .filter(GiaiDau::isFinished)
                    .count();
            
            model.addAttribute("activeTournaments", activeTournaments);
            model.addAttribute("upcomingTournaments", upcomingTournaments);
            model.addAttribute("finishedTournaments", finishedTournaments);
            
            return "tournament/list";
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không thể tải danh sách giải đấu: " + e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Hiển thị chi tiết giải đấu
     * GET /tournament/{id}
     */
    @GetMapping("/{id}")
    public String tournamentDetail(@PathVariable("id") Integer id, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        try {
            var tournamentOpt = giaiDauService.getGiaiDauById(id);
            
            if (tournamentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Không tìm thấy giải đấu với ID: " + id);
                return "redirect:/tournament/list";
            }
            
            GiaiDau tournament = tournamentOpt.get();
            model.addAttribute("tournament", tournament);
            model.addAttribute("isActive", tournament.isActive());
            model.addAttribute("isUpcoming", tournament.isUpcoming());
            model.addAttribute("isFinished", tournament.isFinished());
            
            return "badmintonTournament/badmintonTournament";
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Lỗi khi tải thông tin giải đấu: " + e.getMessage());
            return "redirect:/tournament/list";
        }
    }

    /**
     * Tìm kiếm giải đấu
     * GET /tournament/search?q=...
     */
    @GetMapping("/search")
    public String searchTournaments(@RequestParam("q") String query, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        try {
            List<GiaiDau> tournaments = giaiDauService.searchGiaiDauByName(query);
            model.addAttribute("tournaments", tournaments);
            model.addAttribute("searchQuery", query);
            model.addAttribute("totalTournaments", tournaments.size());
            
            return "tournament/list";
        } catch (SQLException e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Lỗi khi tìm kiếm giải đấu: " + e.getMessage());
            return "redirect:/tournament/list";
        }
    }

    /**
     * API endpoint để lấy ranking của giải đấu (JSON)
     * GET /tournament/{id}/ranking
     * TODO: Implement sau khi có RankingService
     */
    @GetMapping("/{id}/ranking")
    public String getTournamentRanking(@PathVariable("id") Integer id, Model model) {
        // Placeholder - sẽ implement ở task tiếp theo
        model.addAttribute("tournamentId", id);
        return "tournament/ranking";
    }

    /**
     * API endpoint để lấy schedule của giải đấu (JSON)
     * GET /tournament/{id}/schedule
     * TODO: Implement sau khi có ScheduleService
     */
    @GetMapping("/{id}/schedule")
    public String getTournamentSchedule(@PathVariable("id") Integer id, Model model) {
        // Placeholder - sẽ implement ở task tiếp theo
        model.addAttribute("tournamentId", id);
        return "tournament/schedule";
    }

    /**
     * API endpoint để lấy statistics của giải đấu (JSON)
     * GET /tournament/{id}/stats
     * TODO: Implement sau khi có StatisticsService
     */
    @GetMapping("/{id}/stats")
    public String getTournamentStats(@PathVariable("id") Integer id, Model model) {
        // Placeholder - sẽ implement ở task tiếp theo
        model.addAttribute("tournamentId", id);
        return "tournament/stats";
    }
}
