package com.example.btms.web.controller.tournament;

import com.example.btms.service.tournamentWebData.TournamentDataService;
import com.example.btms.web.dto.TournamentDTO;
import com.example.btms.web.dto.TournamentDetailDTO;
import com.example.btms.web.dto.TournamentCardDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // Lấy tất cả giải đấu để xử lý (DTO objects)
        List<TournamentDTO> allTournaments = tournamentDataService.getAllTournaments();
        
        // Featured tournaments (giải đấu nổi bật - lấy 4 giải đầu tiên)
        List<TournamentCardDTO> featuredTournaments = tournamentDataService.getFeaturedTournaments(4);
        
        // Live tournaments (đang diễn ra)
        List<TournamentDTO> liveTournaments = tournamentDataService.getOngoingTournaments().stream()
            .limit(3)
            .collect(Collectors.toList());
        
        // Upcoming tournaments (sắp diễn ra)
        List<TournamentDTO> upcomingTournaments = tournamentDataService.getUpcomingTournaments().stream()
            .limit(3)
            .collect(Collectors.toList());
        
        // Registration open (đang mở đăng ký)
        List<TournamentDTO> registrationOpen = tournamentDataService.getOpenForRegistrationTournaments().stream()
            .limit(3)
            .collect(Collectors.toList());
        
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
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer priceMin,
            @RequestParam(required = false) Integer priceMax,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        // Lấy tất cả giải đấu (DTO objects)
        List<TournamentDTO> allTournaments = tournamentDataService.getAllTournaments();
        
        // Filter theo status (multi-select: comma-separated)
        if (status != null && !status.isEmpty()) {
            String[] statuses = status.split(",");
            allTournaments = allTournaments.stream()
                .filter(t -> Arrays.asList(statuses).contains(t.getTrangThai()))
                .collect(Collectors.toList());
        }
        
        // Filter theo province
        if (province != null && !province.isEmpty()) {
            allTournaments = allTournaments.stream()
                .filter(t -> province.equals(t.getTinhThanh()))
                .collect(Collectors.toList());
        }
        
        // Filter theo level (multi-select: comma-separated)
        if (level != null && !level.isEmpty()) {
            String[] levels = level.split(",");
            allTournaments = allTournaments.stream()
                .filter(t -> Arrays.asList(levels).contains(t.getCapDo()))
                .collect(Collectors.toList());
        }
        
        // Filter theo type (multi-select: comma-separated)
        if (type != null && !type.isEmpty()) {
            String[] types = type.split(",");
            allTournaments = allTournaments.stream()
                .filter(t -> Arrays.asList(types).contains(t.getTheLoai()))
                .collect(Collectors.toList());
        }
        
        // Filter theo category (legacy support)
        if (category != null && !category.isEmpty()) {
            allTournaments = allTournaments.stream()
                .filter(t -> category.equals(t.getCapDo()) || category.equals(t.getTheLoai()))
                .collect(Collectors.toList());
        }
        
        // Filter theo date range
        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                LocalDate fromDate = LocalDate.parse(dateFrom);
                allTournaments = allTournaments.stream()
                    .filter(t -> {
                        if (t.getNgayBatDau() == null) return false;
                        return !t.getNgayBatDau().isBefore(fromDate);
                    })
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // Invalid date format, skip filter
            }
        }
        
        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                LocalDate toDate = LocalDate.parse(dateTo);
                allTournaments = allTournaments.stream()
                    .filter(t -> {
                        if (t.getNgayBatDau() == null) return false;
                        return !t.getNgayBatDau().isAfter(toDate);
                    })
                    .collect(Collectors.toList());
            } catch (Exception e) {
                // Invalid date format, skip filter
            }
        }
        
        // Filter theo price range
        if (priceMin != null || priceMax != null) {
            allTournaments = allTournaments.stream()
                .filter(t -> {
                    BigDecimal price = t.getPhiThamGia();
                    if (price == null) return false;
                    if (priceMin != null && price.compareTo(BigDecimal.valueOf(priceMin)) < 0) return false;
                    if (priceMax != null && price.compareTo(BigDecimal.valueOf(priceMax)) > 0) return false;
                    return true;
                })
                .collect(Collectors.toList());
        }
        
        // Sorting
        if (sort != null && !sort.isEmpty()) {
            switch (sort) {
                case "newest":
                    // Sắp xếp theo ngày bắt đầu giảm dần (mới nhất)
                    allTournaments.sort((t1, t2) -> {
                        if (t1.getNgayBatDau() == null) return 1;
                        if (t2.getNgayBatDau() == null) return -1;
                        return t2.getNgayBatDau().compareTo(t1.getNgayBatDau());
                    });
                    break;
                case "most-viewed":
                    // Sắp xếp theo lượt xem
                    allTournaments.sort((t1, t2) -> Integer.compare(
                        t2.getLuotXem() != null ? t2.getLuotXem() : 0,
                        t1.getLuotXem() != null ? t1.getLuotXem() : 0
                    ));
                    break;
                case "highest-rated":
                    // Sắp xếp theo đánh giá cao nhất (BigDecimal)
                    allTournaments.sort((t1, t2) -> {
                        BigDecimal rating1 = t1.getDanhGiaTb() != null ? t1.getDanhGiaTb() : BigDecimal.ZERO;
                        BigDecimal rating2 = t2.getDanhGiaTb() != null ? t2.getDanhGiaTb() : BigDecimal.ZERO;
                        return rating2.compareTo(rating1);
                    });
                    break;
                case "price-low":
                    // Sắp xếp theo giá tăng dần (BigDecimal)
                    allTournaments.sort((t1, t2) -> {
                        BigDecimal price1 = t1.getPhiThamGia() != null ? t1.getPhiThamGia() : BigDecimal.ZERO;
                        BigDecimal price2 = t2.getPhiThamGia() != null ? t2.getPhiThamGia() : BigDecimal.ZERO;
                        return price1.compareTo(price2);
                    });
                    break;
                case "price-high":
                    // Sắp xếp theo giá giảm dần (BigDecimal)
                    allTournaments.sort((t1, t2) -> {
                        BigDecimal price1 = t1.getPhiThamGia() != null ? t1.getPhiThamGia() : BigDecimal.ZERO;
                        BigDecimal price2 = t2.getPhiThamGia() != null ? t2.getPhiThamGia() : BigDecimal.ZERO;
                        return price2.compareTo(price1);
                    });
                    break;
            }
        }
        
        // Pagination (simple)
        int totalItems = allTournaments.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);
        
        List<TournamentDTO> pagedTournaments = start < totalItems 
            ? allTournaments.subList(start, end) 
            : List.of();
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournaments", pagedTournaments);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        
        // Thêm filter parameters để restore state
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedProvince", province);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        model.addAttribute("selectedPriceMin", priceMin);
        model.addAttribute("selectedPriceMax", priceMax);
        model.addAttribute("selectedSort", sort);
        
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
        // Lấy thông tin giải đấu (DetailDTO)
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            // Redirect về trang danh sách nếu không tìm thấy
            return "redirect:/tournaments?error=not-found";
        }
        
        // Lấy các giải đấu liên quan (cùng category hoặc cùng location)
        List<TournamentDTO> relatedTournaments = tournamentDataService.getAllTournaments().stream()
            .filter(t -> !t.getId().equals(id)) // Loại bỏ giải đấu hiện tại
            .filter(t -> (tournament.getCapDo() != null && tournament.getCapDo().equals(t.getCapDo())) 
                      || (tournament.getTinhThanh() != null && tournament.getTinhThanh().equals(t.getTinhThanh())))
            .limit(3)
            .collect(Collectors.toList());
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        model.addAttribute("relatedTournaments", relatedTournaments);
        
        // Kiểm tra xem có đang mở đăng ký không
        boolean isRegistrationOpen = tournamentDataService.isRegistrationOpen(id);
        model.addAttribute("isRegistrationOpen", isRegistrationOpen);
        
        // SEO & metadata
        model.addAttribute("pageTitle", tournament.getTenGiai() + " - BTMS");
        model.addAttribute("pageDescription", tournament.getMoTa());
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
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }
        
        // Kiểm tra xem có đang mở đăng ký không
        boolean isRegistrationOpen = tournamentDataService.isRegistrationOpen(id);
        
        if (!isRegistrationOpen) {
            // Redirect về trang chi tiết nếu không mở đăng ký
            return "redirect:/tournaments/" + id + "?error=registration-closed";
        }
        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Đăng ký - " + tournament.getTenGiai());
        model.addAttribute("pageDescription", "Đăng ký tham gia " + tournament.getTenGiai());
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
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("matches", matches);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Lịch thi đấu - " + tournament.getTenGiai());
        model.addAttribute("pageDescription", "Xem lịch thi đấu của " + tournament.getTenGiai());
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
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }
        
        // Chỉ hiển thị live cho giải đang diễn ra
        if (!"ongoing".equals(tournament.getTrangThai())) {
            return "redirect:/tournaments/" + id + "?error=not-live";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("liveMatches", liveMatches);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "LIVE - " + tournament.getTenGiai());
        model.addAttribute("pageDescription", "Theo dõi trực tiếp " + tournament.getTenGiai());
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
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("standings", standings);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Bảng xếp hạng - " + tournament.getTenGiai());
        model.addAttribute("pageDescription", "Xem bảng xếp hạng của " + tournament.getTenGiai());
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
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }        
        // Thêm dữ liệu vào model
        model.addAttribute("tournament", tournament);
        // model.addAttribute("participants", participants);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Danh sách VĐV - " + tournament.getTenGiai());
        model.addAttribute("pageDescription", "Xem danh sách VĐV tham gia " + tournament.getTenGiai());
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
        TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
        
        if (tournament == null) {
            return "redirect:/tournaments?error=not-found";
        }
        model.addAttribute("tournament", tournament);
        // model.addAttribute("rules", rules);
        
        // SEO & metadata
        model.addAttribute("pageTitle", "Thể lệ - " + tournament.getTenGiai());
        model.addAttribute("pageDescription", "Thể lệ và quy định của " + tournament.getTenGiai());
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
        List<TournamentDTO> completedTournaments = tournamentDataService.getTournamentsByStatus("completed");
        
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
        List<TournamentDTO> upcomingTournaments = tournamentDataService.getUpcomingTournaments();
        List<TournamentDTO> ongoingTournaments = tournamentDataService.getOngoingTournaments();
        
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
