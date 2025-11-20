package com.example.btms.web.controller.tournament;

import com.example.btms.service.tournamentWebData.TournamentDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý các trang liên quan đến giải đấu
 * Bao gồm: danh sách, chi tiết, đăng ký, lịch thi đấu, live scoring
 */
@Controller
@RequestMapping({"/tournament", "/tournaments", "/tour"})
public class TournamentController {

    private final TournamentDataService tournamentDataService;

    public TournamentController(TournamentDataService tournamentDataService) {
        this.tournamentDataService = tournamentDataService;
    }

    /**
     * Trang chủ Tournament Hub - Dashboard tổng quan về các giải đấu
     * Route: /tournaments hoặc /tournaments/home
     * 
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-home
     */
    @GetMapping({"", "/", "/home"})
    public String tournamentHome(Model model) {
        // Lấy tất cả giải đấu để xử lý
        List<Map<String, Object>> allTournaments = tournamentDataService.getAllTournaments();
        
        // Featured tournaments (giải đấu nổi bật - lấy 4 giải đầu tiên)
        List<Map<String, Object>> featuredTournaments = allTournaments.stream()
            .limit(4)
            .toList();
        
        // Live tournaments (đang diễn ra)
        List<Map<String, Object>> liveTournaments = allTournaments.stream()
            .filter(t -> "ongoing".equals(t.get("status")))
            .limit(3)
            .toList();
        
        // Upcoming tournaments (sắp diễn ra)
        List<Map<String, Object>> upcomingTournaments = allTournaments.stream()
            .filter(t -> "upcoming".equals(t.get("status")))
            .limit(3)
            .toList();
        
        // Registration open (đang mở đăng ký)
        List<Map<String, Object>> registrationOpen = allTournaments.stream()
            .filter(t -> "registration".equals(t.get("status")))
            .limit(3)
            .toList();
        
        // Thống kê tổng quan
        Map<String, Long> statsByStatus = tournamentDataService.getStatsByStatus();
        long totalTournaments = allTournaments.size();
        long activeTournaments = statsByStatus.getOrDefault("ongoing", 0L);
        long upcomingCount = statsByStatus.getOrDefault("upcoming", 0L);
        long registrationCount = statsByStatus.getOrDefault("registration", 0L);
        
        // Quick stats for dashboard
        Map<String, Object> quickStats = Map.of(
            "total", totalTournaments,
            "active", activeTournaments,
            "upcoming", upcomingCount,
            "registration", registrationCount,
            "participants", 1234,  // Mock data - sẽ thay bằng real data sau
            "liveMatches", liveTournaments.size()
        );
        
        // Add to model
        model.addAttribute("featuredTournaments", featuredTournaments);
        model.addAttribute("liveTournaments", liveTournaments);
        model.addAttribute("upcomingTournaments", upcomingTournaments);
        model.addAttribute("registrationOpen", registrationOpen);
        model.addAttribute("quickStats", quickStats);
        model.addAttribute("statsByStatus", statsByStatus);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Tournament Hub - Trung tâm Giải đấu Cầu lông - BTMS");
        model.addAttribute("pageDescription", "Khám phá và tham gia các giải đấu cầu lông chuyên nghiệp. Xem lịch thi đấu, đăng ký tham gia và theo dõi trực tiếp.");
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-home";
    }

    /**
     * Trang danh sách tất cả giải đấu (full list với pagination)
     * Route: /tournaments/list
     * 
     * @param status Filter theo trạng thái (optional): upcoming, ongoing, registration, completed
     * @param category Filter theo loại (optional): professional, amateur, club, youth, etc.
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-list
     */
    @GetMapping("/list")
    public String listTournaments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        // Lấy tất cả giải đấu
        List<Map<String, Object>> allTournaments = tournamentDataService.getAllTournaments();
        
        // Filter theo status nếu có
        if (status != null && !status.isEmpty()) {
            allTournaments = allTournaments.stream()
                .filter(t -> status.equals(t.get("status")))
                .toList();
        }
        
        // Filter theo category nếu có
        if (category != null && !category.isEmpty()) {
            allTournaments = allTournaments.stream()
                .filter(t -> category.equals(t.get("category")))
                .toList();
        }
        
        // Pagination (simple)
        int totalItems = allTournaments.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);
        
        List<Map<String, Object>> pagedTournaments = start < totalItems 
            ? allTournaments.subList(start, end) 
            : List.of();
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournaments", pagedTournaments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        
        // Lấy thống kê theo status
        Map<String, Long> statsByStatus = tournamentDataService.getStatsByStatus();
        model.addAttribute("statsByStatus", statsByStatus);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Danh sách Giải đấu - BTMS");
        model.addAttribute("pageDescription", "Khám phá các giải đấu cầu lông đang diễn ra và sắp tới");
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-list";
    }

    /**
     * Trang chi tiết giải đấu
     * Route: /tournaments/{id} hoặc /tournaments/detail/{id}
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-detail
     */
    @GetMapping({"/{id}", "/detail/{id}"})
    public String tournamentDetail(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            // Redirect về trang danh sách nếu không tìm thấy
            return "redirect:/tournaments?error=not-found";
        }
        
        // Lấy các giải đấu liên quan (cùng category hoặc cùng location)
        List<Map<String, Object>> relatedTournaments = tournamentDataService.getAllTournaments().stream()
            .filter(t -> !t.get("id").equals(id)) // Loại bỏ giải đấu hiện tại
            .filter(t -> t.get("category").equals(tournament.get("category")) 
                      || t.get("location").equals(tournament.get("location")))
            .limit(3)
            .toList();
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        model.addAttribute("relatedTournaments", relatedTournaments);
        
        // Kiểm tra xem có đang mở đăng ký không
        boolean isRegistrationOpen = tournamentDataService.isRegistrationOpen(tournament);
        model.addAttribute("isRegistrationOpen", isRegistrationOpen);
        
        // SEO & metadata
        model.addAttribute("pageTitle", tournament.get("name") + " - BTMS");
        model.addAttribute("pageDescription", tournament.get("description"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-detail";
    }

    /**
     * Trang đăng ký tham gia giải đấu
     * Route: /tournaments/{id}/register
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-register
     */
    @GetMapping("/{id}/register")
    public String tournamentRegister(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }
        
        // Kiểm tra xem có đang mở đăng ký không
        boolean isRegistrationOpen = tournamentDataService.isRegistrationOpen(tournament);
        
        if (!isRegistrationOpen) {
            // Redirect về trang chi tiết nếu không mở đăng ký
            return "redirect:/tournaments/" + id + "?error=registration-closed";
        }
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Đăng ký - " + tournament.get("name"));
        model.addAttribute("pageDescription", "Đăng ký tham gia " + tournament.get("name"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-register";
    }

    /**
     * Trang lịch thi đấu của giải
     * Route: /tournaments/{id}/schedule
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-schedule
     */
    @GetMapping("/{id}/schedule")
    public String tournamentSchedule(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("matches", matches);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Lịch thi đấu - " + tournament.get("name"));
        model.addAttribute("pageDescription", "Xem lịch thi đấu của " + tournament.get("name"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-schedule";
    }

    /**
     * Trang live scoring
     * Route: /tournaments/{id}/live
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-live
     */
    @GetMapping("/{id}/live")
    public String tournamentLive(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }
        
        // Chỉ hiển thị live cho giải đang diễn ra
        if (!"ongoing".equals(tournament.get("status"))) {
            return "redirect:/tournaments/" + id + "?error=not-live";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("liveMatches", liveMatches);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "LIVE - " + tournament.get("name"));
        model.addAttribute("pageDescription", "Theo dõi trực tiếp " + tournament.get("name"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-live";
    }

    /**
     * Trang bảng xếp hạng của giải
     * Route: /tournaments/{id}/standings
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-standings
     */
    @GetMapping("/{id}/standings")
    public String tournamentStandings(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("standings", standings);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Bảng xếp hạng - " + tournament.get("name"));
        model.addAttribute("pageDescription", "Xem bảng xếp hạng của " + tournament.get("name"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-standings";
    }

    /**
     * Trang danh sách VĐV tham gia
     * Route: /tournaments/{id}/participants
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-participants
     */
    @GetMapping("/{id}/participants")
    public String tournamentParticipants(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("participants", participants);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Danh sách VĐV - " + tournament.get("name"));
        model.addAttribute("pageDescription", "Xem danh sách VĐV tham gia " + tournament.get("name"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-participants";
    }

    /**
     * Trang thể lệ & quy định
     * Route: /tournaments/{id}/rules
     * 
     * @param id ID của giải đấu
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-rules
     */
    @GetMapping("/{id}/rules")
    public String tournamentRules(@PathVariable int id, Model model) {
        // Lấy thông tin giải đấu
        Map<String, Object> tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }
        model.addAttribute("tournament", tournament);
        // model.addAttribute("rules", rules);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Thể lệ - " + tournament.get("name"));
        model.addAttribute("pageDescription", "Thể lệ và quy định của " + tournament.get("name"));
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-rules";
    }

    /**
     * Trang lịch sử các giải đã qua
     * Route: /tournaments/history
     * 
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-history
     */
    @GetMapping("/history")
    public String tournamentHistory(Model model) {
        // Lấy các giải đã kết thúc
        List<Map<String, Object>> completedTournaments = tournamentDataService.getAllTournaments().stream()
            .filter(t -> "completed".equals(t.get("status")))
            .toList();
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournaments", completedTournaments);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Lịch sử Giải đấu - BTMS");
        model.addAttribute("pageDescription", "Xem lại các giải đấu đã diễn ra");
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-history";
    }

    /**
     * Trang calendar view của tất cả giải đấu
     * Route: /tournaments/calendar
     * 
     * @param model Model để truyền dữ liệu
     * @return Template: tournament/tournament-calendar
     */
    @GetMapping("/calendar")
    public String tournamentCalendar(Model model) {
        // Lấy tất cả giải đấu (upcoming + ongoing)
        List<Map<String, Object>> upcomingTournaments = tournamentDataService.getUpcomingTournaments();
        List<Map<String, Object>> ongoingTournaments = tournamentDataService.getOngoingTournaments();
        
        // Thêm dữ liệu vào model
        model.addAttribute("upcomingTournaments", upcomingTournaments);
        model.addAttribute("ongoingTournaments", ongoingTournaments);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Lịch Giải đấu - BTMS");
        model.addAttribute("pageDescription", "Xem lịch thi đấu của tất cả các giải");
        model.addAttribute("activePage", "tournaments");
        
        return "tournament/tournament-calendar";
    }
}
