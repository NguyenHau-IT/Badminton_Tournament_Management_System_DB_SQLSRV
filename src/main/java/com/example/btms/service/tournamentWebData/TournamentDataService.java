package com.example.btms.service.tournamentWebData;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service để load và xử lý dữ liệu giải đấu từ JSON file
 */
@Service
public class TournamentDataService {

    private final ObjectMapper objectMapper;
    private List<Map<String, Object>> tournaments;

    public TournamentDataService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadTournaments();
    }

    /**
     * Load dữ liệu từ tournaments.json
     */
    private void loadTournaments() {
        try {
            ClassPathResource resource = new ClassPathResource("data/tournaments.json");
            tournaments = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (IOException e) {
            System.err.println("Không thể load file tournaments.json: " + e.getMessage());
            tournaments = new ArrayList<>();
        }
    }

    /**
     * Lấy tất cả giải đấu
     */
    public List<Map<String, Object>> getAllTournaments() {
        return new ArrayList<>(tournaments);
    }

    /**
     * Lấy các giải đấu nổi bật (featured = true)
     */
    public List<Map<String, Object>> getFeaturedTournaments() {
        return tournaments.stream()
            .filter(t -> Boolean.TRUE.equals(t.get("featured")))
            .collect(Collectors.toList());
    }

    /**
     * Lấy các giải đấu sắp diễn ra (status = upcoming hoặc registration)
     */
    public List<Map<String, Object>> getUpcomingTournaments() {
        return tournaments.stream()
            .filter(t -> {
                String status = (String) t.get("status");
                return "upcoming".equals(status) || "registration".equals(status);
            })
            .collect(Collectors.toList());
    }

    /**
     * Lấy các giải đấu đang diễn ra (status = ongoing)
     */
    public List<Map<String, Object>> getOngoingTournaments() {
        return tournaments.stream()
            .filter(t -> "ongoing".equals(t.get("status")))
            .collect(Collectors.toList());
    }

    /**
     * Lấy giải đấu theo ID
     */
    public Map<String, Object> getTournamentById(int id) {
        return tournaments.stream()
            .filter(t -> {
                Object idObj = t.get("id");
                return idObj instanceof Integer && (Integer) idObj == id;
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * Lấy N giải đấu gần nhất (cho preview)
     */
    public List<Map<String, Object>> getRecentTournaments(int limit) {
        return tournaments.stream()
            .filter(t -> Boolean.TRUE.equals(t.get("featured")))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Đếm tổng số giải đấu
     */
    public int getTotalTournaments() {
        return tournaments.size();
    }

    /**
     * Lấy thống kê theo trạng thái
     */
    public Map<String, Long> getStatsByStatus() {
        return tournaments.stream()
            .collect(Collectors.groupingBy(
                t -> (String) t.get("status"),
                Collectors.counting()
            ));
    }

    /**
     * Format ngày tháng từ String sang LocalDate
     */
    public LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Kiểm tra xem giải đấu có đang mở đăng ký không
     */
    public boolean isRegistrationOpen(Map<String, Object> tournament) {
        String status = (String) tournament.get("status");
        String deadline = (String) tournament.get("registrationDeadline");
        
        if (!"registration".equals(status)) {
            return false;
        }

        LocalDate deadlineDate = parseDate(deadline);
        return deadlineDate != null && deadlineDate.isAfter(LocalDate.now());
    }
}
