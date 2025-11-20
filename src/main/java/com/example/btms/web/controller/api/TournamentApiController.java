package com.example.btms.web.controller.api;

import com.example.btms.service.tournamentWebData.TournamentDataService;
import com.example.btms.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API Controller cho Tournament
 * 
 * Base URL: /api/tournaments
 * 
 * Provides JSON endpoints for:
 * - Tournament list (with pagination, filtering, sorting)
 * - Tournament details
 * - Featured tournaments
 * - Search functionality
 * - Calendar events
 * - Statistics & analytics
 * - Top tournaments (by views, ratings)
 * 
 * All endpoints return JSON responses
 * 
 * @author BTMS Team
 * @version 2.0 - Web Platform API
 */
@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*", maxAge = 3600) // Allow CORS for frontend apps
public class TournamentApiController {

    private static final Logger logger = LoggerFactory.getLogger(TournamentApiController.class);
    
    private final TournamentDataService tournamentDataService;

    public TournamentApiController(TournamentDataService tournamentDataService) {
        this.tournamentDataService = tournamentDataService;
    }

    // ========== BASIC CRUD ENDPOINTS ==========

    /**
     * GET /api/tournaments
     * Lấy danh sách tất cả giải đấu với pagination
     * 
     * Query params:
     * - page: Page number (default 0)
     * - size: Page size (default 20)
     * - sort: Sort field (default: ngayBd)
     * - direction: Sort direction (asc/desc, default: desc)
     * 
     * Response: Page<TournamentDTO>
     */
    @GetMapping
    public ResponseEntity<Page<TournamentDTO>> getAllTournaments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ngayBd") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.debug("GET /api/tournaments - page: {}, size: {}, sort: {}, direction: {}", 
                     page, size, sort, direction);
        
        try {
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            Page<TournamentDTO> tournaments = tournamentDataService.getAllTournaments(pageable);
            
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/{id}
     * Lấy chi tiết một giải đấu
     * 
     * Path variable:
     * - id: Tournament ID
     * 
     * Response: TournamentDetailDTO
     * Status: 200 OK | 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentDetailDTO> getTournamentById(@PathVariable Integer id) {
        logger.debug("GET /api/tournaments/{}", id);
        
        try {
            TournamentDetailDTO tournament = tournamentDataService.getTournamentById(id);
            
            if (tournament == null) {
                logger.warn("Tournament not found: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            logger.error("Error getting tournament {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/count
     * Đếm tổng số giải đấu
     * 
     * Response: { "count": 123 }
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTotalCount() {
        logger.debug("GET /api/tournaments/count");
        
        try {
            long count = tournamentDataService.getTotalTournaments();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error counting tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/exists/{id}
     * Kiểm tra giải đấu có tồn tại không
     * 
     * Response: { "exists": true/false }
     */
    @GetMapping("/exists/{id}")
    public ResponseEntity<Map<String, Boolean>> checkExists(@PathVariable Integer id) {
        logger.debug("GET /api/tournaments/exists/{}", id);
        
        try {
            boolean exists = tournamentDataService.existsById(id);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking tournament existence: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== FEATURED TOURNAMENTS ==========

    /**
     * GET /api/tournaments/featured
     * Lấy danh sách giải đấu nổi bật
     * 
     * Query params:
     * - limit: Max results (default 10)
     * 
     * Response: List<TournamentCardDTO>
     */
    @GetMapping("/featured")
    public ResponseEntity<List<TournamentCardDTO>> getFeaturedTournaments(
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("GET /api/tournaments/featured - limit: {}", limit);
        
        try {
            List<TournamentCardDTO> tournaments = tournamentDataService.getFeaturedTournaments(limit);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting featured tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/featured/active
     * Lấy giải nổi bật đang active (registration/ongoing)
     * 
     * Response: List<TournamentCardDTO>
     */
    @GetMapping("/featured/active")
    public ResponseEntity<List<TournamentCardDTO>> getActiveFeaturedTournaments() {
        logger.debug("GET /api/tournaments/featured/active");
        
        try {
            List<TournamentCardDTO> tournaments = tournamentDataService.getActiveFeaturedTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting active featured tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== FILTER BY STATUS ==========

    /**
     * GET /api/tournaments/status/{status}
     * Lấy giải đấu theo trạng thái
     * 
     * Path variable:
     * - status: draft, upcoming, registration, ongoing, completed, cancelled
     * 
     * Query params:
     * - page, size, sort, direction (optional)
     * 
     * Response: Page<TournamentDTO> or List<TournamentDTO>
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTournamentsByStatus(
            @PathVariable String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("GET /api/tournaments/status/{} - page: {}", status, page);
        
        try {
            if (page != null) {
                // With pagination
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayBd"));
                Page<TournamentDTO> tournaments = tournamentDataService.getTournamentsByStatus(status, pageable);
                return ResponseEntity.ok(tournaments);
            } else {
                // Without pagination
                List<TournamentDTO> tournaments = tournamentDataService.getTournamentsByStatus(status);
                return ResponseEntity.ok(tournaments);
            }
        } catch (Exception e) {
            logger.error("Error getting tournaments by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/upcoming
     * Lấy các giải sắp diễn ra
     * 
     * Response: List<TournamentDTO>
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<TournamentDTO>> getUpcomingTournaments() {
        logger.debug("GET /api/tournaments/upcoming");
        
        try {
            List<TournamentDTO> tournaments = tournamentDataService.getUpcomingTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting upcoming tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/ongoing
     * Lấy các giải đang diễn ra
     * 
     * Response: List<TournamentDTO>
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<TournamentDTO>> getOngoingTournaments() {
        logger.debug("GET /api/tournaments/ongoing");
        
        try {
            List<TournamentDTO> tournaments = tournamentDataService.getOngoingTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting ongoing tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/registration-open
     * Lấy các giải đang mở đăng ký
     * 
     * Response: List<TournamentDTO>
     */
    @GetMapping("/registration-open")
    public ResponseEntity<List<TournamentDTO>> getRegistrationOpenTournaments() {
        logger.debug("GET /api/tournaments/registration-open");
        
        try {
            List<TournamentDTO> tournaments = tournamentDataService.getOpenForRegistrationTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting registration-open tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== FILTER BY LOCATION ==========

    /**
     * GET /api/tournaments/province/{province}
     * Lấy giải đấu theo tỉnh/thành
     * 
     * Path variable:
     * - province: Tên tỉnh/thành phố
     * 
     * Response: List<TournamentDTO>
     */
    @GetMapping("/province/{province}")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByProvince(@PathVariable String province) {
        logger.debug("GET /api/tournaments/province/{}", province);
        
        try {
            List<TournamentDTO> tournaments = tournamentDataService.getTournamentsByProvince(province);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting tournaments by province {}: {}", province, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== INFINITE SCROLL API ==========

    /**
     * GET /api/tournaments/list
     * API endpoint for infinite scroll with advanced filters
     * Compatible with tournament list page filters
     * 
     * Query params:
     * - status: comma-separated statuses (ongoing,registration,upcoming,completed)
     * - category: Category filter (legacy support)
     * - province: Province/city filter
     * - level: comma-separated levels
     * - type: comma-separated types
     * - dateFrom: Start date filter (yyyy-MM-dd)
     * - dateTo: End date filter (yyyy-MM-dd)
     * - priceMin: Minimum price
     * - priceMax: Maximum price
     * - sort: newest|most-viewed|highest-rated|price-low|price-high
     * - page: Page number (default 0)
     * - size: Page size (default 12)
     * 
     * Response: 
     * {
     *   "tournaments": [...],
     *   "currentPage": 0,
     *   "totalPages": 5,
     *   "totalItems": 50,
     *   "hasMore": true
     * }
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getTournamentsList(
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        logger.debug("GET /api/tournaments/list - page: {}, size: {}, filters: status={}, province={}, sort={}", 
                     page, size, status, province, sort);
        
        try {
            // Get all tournaments first
            List<TournamentDTO> allTournaments = tournamentDataService.getAllTournaments();
            
            // Apply filters (same logic as HTML controller)
            
            // Status filter (multi-select)
            if (status != null && !status.isEmpty() && !status.equals("all")) {
                List<String> statusList = Arrays.asList(status.split(","));
                allTournaments = allTournaments.stream()
                    .filter(t -> statusList.contains(t.getTrangThai()))
                    .collect(Collectors.toList());
            }
            
            // Province filter (exact match)
            if (province != null && !province.isEmpty()) {
                allTournaments = allTournaments.stream()
                    .filter(t -> province.equals(t.getTinhThanh()))
                    .collect(Collectors.toList());
            }
            
            // Level filter (multi-select)
            if (level != null && !level.isEmpty()) {
                List<String> levelList = Arrays.asList(level.split(","));
                allTournaments = allTournaments.stream()
                    .filter(t -> levelList.contains(t.getCapDo()))
                    .collect(Collectors.toList());
            }
            
            // Type filter (multi-select)
            if (type != null && !type.isEmpty()) {
                List<String> typeList = Arrays.asList(type.split(","));
                allTournaments = allTournaments.stream()
                    .filter(t -> typeList.contains(t.getTheLoai()))
                    .collect(Collectors.toList());
            }
            
            // Category filter (legacy support)
            if (category != null && !category.isEmpty() && !category.equals("all")) {
                allTournaments = allTournaments.stream()
                    .filter(t -> category.equals(t.getCapDo()))
                    .collect(Collectors.toList());
            }
            
            // Date range filter
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
                    logger.warn("Invalid dateFrom format: {}", dateFrom);
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
                    logger.warn("Invalid dateTo format: {}", dateTo);
                }
            }
            
            // Price range filter
            if (priceMin != null || priceMax != null) {
                allTournaments = allTournaments.stream()
                    .filter(t -> {
                        java.math.BigDecimal price = t.getPhiThamGia();
                        if (price == null) return false;
                        if (priceMin != null && price.compareTo(java.math.BigDecimal.valueOf(priceMin)) < 0) return false;
                        if (priceMax != null && price.compareTo(java.math.BigDecimal.valueOf(priceMax)) > 0) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
            }
            
            // Sorting
            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "newest":
                        allTournaments.sort((t1, t2) -> {
                            if (t1.getNgayBatDau() == null) return 1;
                            if (t2.getNgayBatDau() == null) return -1;
                            return t2.getNgayBatDau().compareTo(t1.getNgayBatDau());
                        });
                        break;
                    case "most-viewed":
                        allTournaments.sort((t1, t2) -> Integer.compare(
                            t2.getLuotXem() != null ? t2.getLuotXem() : 0,
                            t1.getLuotXem() != null ? t1.getLuotXem() : 0
                        ));
                        break;
                    case "highest-rated":
                        allTournaments.sort((t1, t2) -> {
                            java.math.BigDecimal rating1 = t1.getDanhGiaTb() != null ? t1.getDanhGiaTb() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal rating2 = t2.getDanhGiaTb() != null ? t2.getDanhGiaTb() : java.math.BigDecimal.ZERO;
                            return rating2.compareTo(rating1);
                        });
                        break;
                    case "price-low":
                        allTournaments.sort((t1, t2) -> {
                            java.math.BigDecimal price1 = t1.getPhiThamGia() != null ? t1.getPhiThamGia() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal price2 = t2.getPhiThamGia() != null ? t2.getPhiThamGia() : java.math.BigDecimal.ZERO;
                            return price1.compareTo(price2);
                        });
                        break;
                    case "price-high":
                        allTournaments.sort((t1, t2) -> {
                            java.math.BigDecimal price1 = t1.getPhiThamGia() != null ? t1.getPhiThamGia() : java.math.BigDecimal.ZERO;
                            java.math.BigDecimal price2 = t2.getPhiThamGia() != null ? t2.getPhiThamGia() : java.math.BigDecimal.ZERO;
                            return price2.compareTo(price1);
                        });
                        break;
                }
            }
            
            // Calculate pagination
            int totalItems = allTournaments.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int start = page * size;
            int end = Math.min(start + size, totalItems);
            
            // Get page slice
            List<TournamentDTO> pagedTournaments = start < totalItems 
                ? allTournaments.subList(start, end)
                : java.util.Collections.emptyList();
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("tournaments", pagedTournaments);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalItems", totalItems);
            response.put("hasMore", page < totalPages - 1);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting tournament list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== SEARCH ==========

    /**
     * GET /api/tournaments/search
     * Search giải đấu theo keyword
     * 
     * Query params:
     * - q: Keyword (tìm trong tên, địa điểm, tỉnh/thành)
     * - page, size (optional)
     * 
     * Response: Page<TournamentDTO> or List<TournamentDTO>
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTournaments(
            @RequestParam(value = "q") String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("GET /api/tournaments/search?q={} - page: {}", keyword, page);
        
        try {
            if (page != null) {
                // With pagination
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "luotXem"));
                Page<TournamentDTO> tournaments = tournamentDataService.searchTournaments(keyword, pageable);
                return ResponseEntity.ok(tournaments);
            } else {
                // Without pagination
                List<TournamentDTO> tournaments = tournamentDataService.searchTournaments(keyword);
                return ResponseEntity.ok(tournaments);
            }
        } catch (Exception e) {
            logger.error("Error searching tournaments with keyword '{}': {}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/advanced-search
     * Advanced search với nhiều filters
     * 
     * Query params:
     * - keyword: Search keyword (optional)
     * - status: Tournament status (optional)
     * - province: Province/city (optional)
     * - level: Tournament level (optional)
     * - type: Tournament type (optional)
     * - featured: true/false (optional)
     * - fromDate: Start date filter (optional, format: yyyy-MM-dd)
     * - toDate: End date filter (optional, format: yyyy-MM-dd)
     * - page, size, sort, direction
     * 
     * Response: Page<TournamentDTO>
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<Page<TournamentDTO>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ngayBd") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        logger.debug("GET /api/tournaments/advanced-search - keyword: {}, status: {}, province: {}, level: {}, type: {}, featured: {}, dates: {} to {}", 
                     keyword, status, province, level, type, featured, fromDate, toDate);
        
        try {
            Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<TournamentDTO> tournaments = tournamentDataService.advancedSearch(
                    keyword, status, province, level, type, featured, fromDate, toDate, pageable);
            
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error in advanced search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== CALENDAR ==========

    /**
     * GET /api/tournaments/calendar/{year}/{month}
     * Lấy giải đấu trong một tháng (for FullCalendar.js)
     * 
     * Path variables:
     * - year: 2025
     * - month: 1-12
     * 
     * Response: List<TournamentCalendarEventDTO>
     */
    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<List<TournamentCalendarEventDTO>> getCalendarEvents(
            @PathVariable int year,
            @PathVariable int month) {
        
        logger.debug("GET /api/tournaments/calendar/{}/{}", year, month);
        
        try {
            List<TournamentCalendarEventDTO> events = tournamentDataService.getTournamentsInMonth(year, month);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            logger.error("Error getting calendar events for {}/{}: {}", year, month, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/date-range
     * Lấy giải đấu trong khoảng thời gian
     * 
     * Query params:
     * - startDate: Start date (format: yyyy-MM-dd)
     * - endDate: End date (format: yyyy-MM-dd)
     * 
     * Response: List<TournamentDTO>
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("GET /api/tournaments/date-range?startDate={}&endDate={}", startDate, endDate);
        
        try {
            List<TournamentDTO> tournaments = tournamentDataService.getTournamentsBetweenDates(startDate, endDate);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting tournaments between {} and {}: {}", startDate, endDate, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== TOP TOURNAMENTS ==========

    /**
     * GET /api/tournaments/top/viewed
     * Lấy top giải đấu có lượt xem cao nhất
     * 
     * Query params:
     * - limit: Max results (default 10)
     * 
     * Response: List<TournamentCardDTO>
     */
    @GetMapping("/top/viewed")
    public ResponseEntity<List<TournamentCardDTO>> getTopViewedTournaments(
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("GET /api/tournaments/top/viewed - limit: {}", limit);
        
        try {
            List<TournamentCardDTO> tournaments = tournamentDataService.getTopViewedTournaments(limit);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting top viewed tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/top/rated
     * Lấy top giải đấu có đánh giá cao nhất
     * 
     * Query params:
     * - limit: Max results (default 10)
     * 
     * Response: List<TournamentCardDTO>
     */
    @GetMapping("/top/rated")
    public ResponseEntity<List<TournamentCardDTO>> getTopRatedTournaments(
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("GET /api/tournaments/top/rated - limit: {}", limit);
        
        try {
            List<TournamentCardDTO> tournaments = tournamentDataService.getTopRatedTournaments(limit);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error getting top rated tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== STATISTICS ==========

    /**
     * GET /api/tournaments/{id}/stats
     * Lấy thống kê chi tiết của một giải đấu
     * 
     * Response: TournamentStatsDTO
     * Status: 200 OK | 404 Not Found
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<TournamentStatsDTO> getTournamentStats(@PathVariable Integer id) {
        logger.debug("GET /api/tournaments/{}/stats", id);
        
        try {
            TournamentStatsDTO stats = tournamentDataService.getTournamentStats(id);
            
            if (stats == null) {
                logger.warn("Tournament stats not found: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting tournament stats {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/stats/by-status
     * Lấy thống kê số lượng giải đấu theo từng trạng thái
     * 
     * Response: Map<String, Long>
     * Example: { "ongoing": 5, "upcoming": 10, "completed": 100, ... }
     */
    @GetMapping("/stats/by-status")
    public ResponseEntity<Map<String, Long>> getStatsByStatus() {
        logger.debug("GET /api/tournaments/stats/by-status");
        
        try {
            Map<String, Long> stats = tournamentDataService.getStatsByStatus();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting stats by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/stats/featured-count
     * Đếm số giải nổi bật
     * 
     * Response: { "count": 15 }
     */
    @GetMapping("/stats/featured-count")
    public ResponseEntity<Map<String, Long>> getFeaturedCount() {
        logger.debug("GET /api/tournaments/stats/featured-count");
        
        try {
            long count = tournamentDataService.countFeaturedTournaments();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error counting featured tournaments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== BUSINESS LOGIC HELPERS ==========

    /**
     * GET /api/tournaments/{id}/registration-status
     * Kiểm tra giải đấu có đang mở đăng ký không
     * 
     * Response: { "isOpen": true/false, "canRegister": true/false, "hasSlots": true/false }
     */
    @GetMapping("/{id}/registration-status")
    public ResponseEntity<Map<String, Boolean>> getRegistrationStatus(@PathVariable Integer id) {
        logger.debug("GET /api/tournaments/{}/registration-status", id);
        
        try {
            boolean isOpen = tournamentDataService.isRegistrationOpen(id);
            boolean hasSlots = tournamentDataService.hasAvailableSlots(id);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("isOpen", isOpen);
            response.put("canRegister", isOpen && hasSlots);
            response.put("hasSlots", hasSlots);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting registration status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/tournaments/{id}/is-ongoing
     * Kiểm tra giải đấu có đang diễn ra không
     * 
     * Response: { "isOngoing": true/false }
     */
    @GetMapping("/{id}/is-ongoing")
    public ResponseEntity<Map<String, Boolean>> isOngoing(@PathVariable Integer id) {
        logger.debug("GET /api/tournaments/{}/is-ongoing", id);
        
        try {
            boolean ongoing = tournamentDataService.isOngoing(id);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isOngoing", ongoing);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking ongoing status {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== SEARCH AUTOCOMPLETE ==========

    /**
     * GET /api/tournaments/autocomplete
     * Search tournaments for autocomplete dropdown
     * 
     * Query Parameters:
     * - q: search keyword (required, min 2 characters)
     * - limit: max results (optional, default 5, max 10)
     * 
     * Example: GET /api/tournaments/autocomplete?q=cau%20long&limit=5
     * 
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "tenGiai": "Giải Cầu lông TP.HCM",
     *     "tinhThanh": "TP.HCM",
     *     "ngayBatDau": "15/03/2025",
     *     "trangThai": "ongoing",
     *     "hinhAnh": "/images/tournament-1.jpg"
     *   }
     * ]
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocompleteSearch(
            @RequestParam(value = "q", required = true) String query,
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        
        logger.debug("GET /api/tournaments/autocomplete - query: {}, limit: {}", query, limit);
        
        try {
            // Validate query length
            if (query == null || query.trim().length() < 2) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Query must be at least 2 characters");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Limit max results to 10
            if (limit > 10) limit = 10;
            if (limit < 1) limit = 5;
            
            // Search tournaments
            List<TournamentSearchDTO> results = tournamentDataService.searchTournaments(query.trim(), limit);
            
            logger.info("Search '{}' returned {} results", query, results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Search failed for query '{}': {}", query, e.getMessage(), e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ========== HEALTH CHECK ==========

    /**
     * GET /api/tournaments/health
     * Health check endpoint
     * 
     * Response: { "status": "UP", "timestamp": 1234567890, "totalTournaments": 123 }
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.debug("GET /api/tournaments/health");
        
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("totalTournaments", tournamentDataService.getTotalTournaments());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("timestamp", System.currentTimeMillis());
            health.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}
