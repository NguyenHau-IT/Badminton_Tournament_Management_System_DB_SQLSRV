package com.example.btms.web.controller.scoreBoard;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.service.match.CourtManagerService;
import com.example.btms.service.scoreboard.ScoreboardRemote;
import com.example.btms.service.threading.BackgroundTaskManager;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/court")
@CrossOrigin(origins = "*")
public class ScoreboardPinController {

    private static final Logger log = LoggerFactory.getLogger(ScoreboardPinController.class);

    private final Object LOCK = ScoreboardRemote.get().lock();
    private final com.example.btms.util.log.Log appLog = ScoreboardRemote.get().log();
    private final CourtManagerService courtManager = CourtManagerService.getInstance();

    // SSE clients cho từng mã PIN - sử dụng ConcurrentHashMap để thread-safe
    private final Map<String, List<SseEmitter>> pinClients = new ConcurrentHashMap<>();
    // BadmintonMatch riêng biệt cho từng mã PIN - sử dụng ConcurrentHashMap để
    // thread-safe
    private final Map<String, BadmintonMatch> pinMatches = new ConcurrentHashMap<>();
    // Cache ObjectMapper để tái sử dụng, tránh tạo mới liên tục
    private final ObjectMapper om = new ObjectMapper();

    // 🚀 Performance optimization: JSON payload cache và throttling
    private final Map<String, String> jsonPayloadCache = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> lastBroadcastTime = new ConcurrentHashMap<>();
    private static final long MIN_BROADCAST_INTERVAL_MS = 50; // Minimum 50ms between broadcasts

    // 🚀 Enhanced Background Task Manager (Java 21 optimized)
    @Autowired
    private BackgroundTaskManager taskManager;

    public ScoreboardPinController() {
        controllerInstance = this;
        // Không cần add listener cho match chung nữa

        // 🧹 Cleanup task để dọn dẹp dead SSE clients và cache cũ
        startCleanupTask();
    }

    /**
     * 🧹 Background cleanup task để tối ưu performance
     */
    private void startCleanupTask() {
        // Schedule cleanup every 30 seconds
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 seconds

                    // Cleanup dead SSE clients
                    pinClients.forEach((pin, clients) -> {
                        if (clients != null) {
                            clients.removeIf(client -> {
                                try {
                                    // Test if client is still alive by sending ping
                                    client.send(SseEmitter.event().name("ping").data(""));
                                    return false;
                                } catch (Exception e) {
                                    return true; // Remove dead client
                                }
                            });
                        }
                    });

                    // Cleanup old JSON cache entries
                    if (jsonPayloadCache.size() > 50) {
                        jsonPayloadCache.clear();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.warn("Cleanup task error: {}", e.getMessage());
                }
            }
        }, "SSE-Cleanup").start();
    }

    /* ---------- helpers ---------- */
    private Map<String, Integer> view(String pinCode) {
        BadmintonMatch match = getOrCreateMatch(pinCode);
        var s = match.snapshot();
        return Map.of("teamAScore", s.score[0], "teamBScore", s.score[1]);
    }

    private void broadcastSnapshotToPin(String pinCode) {
        // 🚀 Throttling: Tránh broadcast quá nhiều trong thời gian ngắn
        long currentTime = System.currentTimeMillis();
        AtomicLong lastTime = lastBroadcastTime.computeIfAbsent(pinCode, k -> new AtomicLong(0));

        if (currentTime - lastTime.get() < MIN_BROADCAST_INTERVAL_MS) {
            return; // Skip broadcast nếu quá gần lần trước
        }
        lastTime.set(currentTime);

        // 🚀 Sử dụng enhanced task manager cho SSE broadcast (Java 21 optimized)
        taskManager.executeSseBroadcast(() -> {
            try {
                BadmintonMatch match = getOrCreateMatch(pinCode);

                // 🚀 Cache JSON payload để tránh serialize lại liên tục
                String payload = jsonPayloadCache.computeIfAbsent(pinCode + "_" + currentTime, k -> {
                    try {
                        return om.writeValueAsString(match.snapshot());
                    } catch (Exception e) {
                        log.warn("JSON serialization error for PIN {}: {}", pinCode, e.getMessage());
                        return "{}";
                    }
                });

                List<SseEmitter> clients = pinClients.get(pinCode);
                if (clients != null && !clients.isEmpty()) {
                    // Sử dụng CopyOnWriteArrayList để tránh ConcurrentModificationException
                    clients.removeIf(client -> {
                        try {
                            client.send(SseEmitter.event().name("update").data(payload));
                            return false;
                        } catch (IOException ex) {
                            try {
                                client.complete();
                            } catch (Exception ignore) {
                            }
                            return true;
                        }
                    });

                    // 🧹 Cleanup old cache entries periodically
                    if (jsonPayloadCache.size() > 100) {
                        jsonPayloadCache.clear();
                    }
                }
            } catch (RuntimeException e) {
                // Other runtime issues - log but don't crash the task
                log.warn("Error broadcasting to PIN {}: {}", pinCode, e.getMessage());
            }
        });
    }

    /**
     * Tìm control panel theo PIN và gọi helper trong BadmintonControlPanel để
     * ghi CHI_TIET_VAN khi +1 điểm (append "P1/P2@<millis>" và update tổng điểm).
     * side = 0 (A), 1 (B)
     */
    private void tryUpdateChiTietVanOnPointForPin(String pinCode, int side) {
        try {
            Map<String, CourtManagerService.CourtStatus> all = courtManager.getAllCourtStatus();
            String courtId = null;
            for (var cs : all.values()) {
                if (pinCode != null && pinCode.equals(cs.pinCode)) {
                    courtId = cs.courtId;
                    break;
                }
            }
            if (courtId == null)
                return;
            var session = courtManager.getCourt(courtId);
            if (session == null || session.controlPanel == null)
                return;
            Object panel = session.controlPanel;
            try {
                var m = panel.getClass().getDeclaredMethod("updateChiTietVanOnPoint", int.class);
                m.setAccessible(true);
                m.invoke(panel, side);
            } catch (NoSuchMethodException ignore) {
                // Control panel chưa có helper (phiên bản cũ) → bỏ qua nhẹ nhàng
            }
        } catch (ReflectiveOperationException ex) {
            log.warn("CHI_TIET_VAN onPoint (web) reflection failed for PIN {}: {}", pinCode, ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("CHI_TIET_VAN onPoint (web) runtime failed for PIN {}: {}", pinCode, ex.getMessage());
        }
    }

    /**
     * Tìm control panel theo PIN và đồng bộ lại tổng điểm của set hiện tại (không
     * thêm token). Dùng cho -1/undo từ web.
     */
    private void tryUpdateChiTietVanTotalsOnlyForPin(String pinCode) {
        try {
            Map<String, CourtManagerService.CourtStatus> all = courtManager.getAllCourtStatus();
            String courtId = null;
            for (var cs : all.values()) {
                if (pinCode != null && pinCode.equals(cs.pinCode)) {
                    courtId = cs.courtId;
                    break;
                }
            }
            if (courtId == null)
                return;
            var session = courtManager.getCourt(courtId);
            if (session == null || session.controlPanel == null)
                return;
            Object panel = session.controlPanel;
            try {
                var m = panel.getClass().getDeclaredMethod("updateChiTietVanTotalsOnly");
                m.setAccessible(true);
                m.invoke(panel);
            } catch (NoSuchMethodException ignore) {
                // Control panel chưa có helper (phiên bản cũ) → bỏ qua nhẹ nhàng
            }
        } catch (ReflectiveOperationException ex) {
            log.warn("CHI_TIET_VAN totalsOnly (web) reflection failed for PIN {}: {}", pinCode, ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("CHI_TIET_VAN totalsOnly (web) runtime failed for PIN {}: {}", pinCode, ex.getMessage());
        }
    }

    /**
     * Lấy hoặc tạo BadmintonMatch cho mã PIN cụ thể
     * Sử dụng cache để tránh tạo mới liên tục
     */
    private BadmintonMatch getOrCreateMatch(String pinCode) {
        return pinMatches.computeIfAbsent(pinCode, k -> {
            BadmintonMatch newMatch = new BadmintonMatch();
            // Add listener để broadcast khi match thay đổi
            // Sử dụng weak reference để tránh memory leak
            newMatch.addPropertyChangeListener(evt -> {
                // Chỉ broadcast những thay đổi quan trọng
                String propertyName = evt.getPropertyName();
                if ("score".equals(propertyName) || "games".equals(propertyName) ||
                        "gameNumber".equals(propertyName) || "server".equals(propertyName)) {
                    broadcastSnapshotToPin(pinCode);
                }
            });
            return newMatch;
        });
    }

    /* ---------- GET với mã PIN ---------- */
    @GetMapping("/{pin}")
    public ResponseEntity<Map<String, Integer>> getScoreboardWithPin(@PathVariable String pin) {
        log.info("Received GET request for PIN: {}", pin);
        synchronized (LOCK) {
            try {

                Map<String, Integer> result = view(pin);
                log.info("Returning GET result for PIN {}: {}", pin, result);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("Error in GET for PIN {}: {}", pin, e.getMessage(), e);
                return ResponseEntity.status(500).body(Map.of("teamAScore", 0, "teamBScore", 0));
            }
        }
    }

    /* ---------- TEST endpoint ---------- */
    @GetMapping("/{pin}/test")
    public ResponseEntity<String> testPin(@PathVariable String pin) {
        log.info("Received test request for PIN: {}", pin);
        return ResponseEntity.ok("PIN " + pin + " is working! Controller is active.");
    }

    /* ---------- PIN Validation endpoint ---------- */
    @GetMapping("/{pin}/status")
    public ResponseEntity<Map<String, Object>> validatePin(@PathVariable String pin) {
        try {
            log.info("Validating PIN: {}", pin);

            // Kiểm tra PIN có tồn tại trong CourtManagerService không
            Map<String, CourtManagerService.CourtStatus> allCourts = courtManager.getAllCourtStatus();
            boolean pinExists = allCourts.values().stream()
                    .anyMatch(court -> pin.equals(court.pinCode));

            if (pinExists) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("valid", true);
                response.put("pin", pin);
                response.put("timestamp", System.currentTimeMillis());

                // Lấy thông tin sân nếu có
                for (CourtManagerService.CourtStatus court : allCourts.values()) {
                    if (pin.equals(court.pinCode)) {
                        response.put("courtId", court.courtId);
                        response.put("header", court.header);
                        break;
                    }
                }

                log.info("PIN {} is valid", pin);
                return ResponseEntity.ok(response);
            } else {
                log.warn("PIN {} not found", pin);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error validating PIN {}: {}", pin, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /* ---------- Simple test endpoint ---------- */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check request received");
        return ResponseEntity.ok("ScoreboardPinController is running!");
    }

    @GetMapping("/{pin}/sync")
    public ResponseEntity<BadmintonMatch.Snapshot> getSnapshotWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            return ResponseEntity.ok(match.snapshot());
        }
    }

    @GetMapping(value = "/{pin}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamWithPin(@PathVariable String pin) {

        // 🚀 Shorter timeout để tránh zombie connections
        SseEmitter em = new SseEmitter(300000L); // 5 minutes timeout thay vì vô hạn

        // Thêm vào danh sách clients của mã PIN này
        pinClients.computeIfAbsent(pin, k -> new CopyOnWriteArrayList<>()).add(em);

        try {
            BadmintonMatch match = getOrCreateMatch(pin);
            em.send(SseEmitter.event().name("init")
                    .data(om.writeValueAsString(match.snapshot())));
        } catch (IOException ignore) {
            // Remove failed client immediately
            List<SseEmitter> clients = pinClients.get(pin);
            if (clients != null) {
                clients.remove(em);
            }
        }

        em.onCompletion(() -> {
            List<SseEmitter> clients = pinClients.get(pin);
            if (clients != null) {
                clients.remove(em);
            }
        });
        em.onTimeout(() -> {
            List<SseEmitter> clients = pinClients.get(pin);
            if (clients != null) {
                clients.remove(em);
            }
        });
        em.onError(e -> {
            List<SseEmitter> clients = pinClients.get(pin);
            if (clients != null) {
                clients.remove(em);
            }
        });

        return em;
    }

    /* ---------- ACTIONS với mã PIN ---------- */
    @PostMapping("/{pin}/increaseA")
    public ResponseEntity<Map<String, Integer>> increaseAWithPin(@PathVariable String pin) {
        log.info("Received increaseA request for PIN: {}", pin);
        synchronized (LOCK) {
            try {

                BadmintonMatch match = getOrCreateMatch(pin);
                log.info("Got match for PIN {}: {}", pin, match);

                match.pointTo(0);
                log.info("Increased score for Team A (PIN: {}), new score: {}", pin, match.getScore()[0]);
                // Ghi CHI_TIET_VAN cho +1 A
                tryUpdateChiTietVanOnPointForPin(pin, 0);

                try {
                    appLog.plusA(match);
                } catch (Exception ignore) {
                    log.warn("Error logging plusA for PIN {}: {}", pin, ignore.getMessage());
                }

                broadcastSnapshotToPin(pin);
                Map<String, Integer> result = view(pin);
                log.info("Returning result for PIN {}: {}", pin, result);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("Error in increaseA for PIN {}: {}", pin, e.getMessage(), e);
                return ResponseEntity.status(500).body(Map.of("teamAScore", 0, "teamBScore", 0));
            }
        }
    }

    @PostMapping("/{pin}/decreaseA")
    public ResponseEntity<Map<String, Integer>> decreaseAWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.pointDown(0, -1);
            log.info("Decreased score for Team A (PIN: {})", pin);
            // Đồng bộ tổng điểm set hiện tại (không thêm token)
            tryUpdateChiTietVanTotalsOnlyForPin(pin);
            try {
                appLog.minusA(match);
            } catch (Exception ignore) {
            }
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(view(pin));
        }
    }

    @PostMapping("/{pin}/increaseB")
    public ResponseEntity<Map<String, Integer>> increaseBWithPin(@PathVariable String pin) {
        log.info("Received increaseB request for PIN: {}", pin);
        synchronized (LOCK) {
            try {

                BadmintonMatch match = getOrCreateMatch(pin);
                log.info("Got match for PIN {}: {}", pin, match);

                match.pointTo(1);
                log.info("Increased score for Team B (PIN: {}), new score: {}", pin, match.getScore()[1]);
                // Ghi CHI_TIET_VAN cho +1 B
                tryUpdateChiTietVanOnPointForPin(pin, 1);

                try {
                    appLog.plusB(match);
                } catch (Exception ignore) {
                    log.warn("Error logging plusB for PIN {}: {}", pin, ignore.getMessage());
                }

                broadcastSnapshotToPin(pin);
                Map<String, Integer> result = view(pin);
                log.info("Returning result for PIN {}: {}", pin, result);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                log.error("Error in increaseB for PIN {}: {}", pin, e.getMessage(), e);
                return ResponseEntity.status(500).body(Map.of("teamAScore", 0, "teamBScore", 0));
            }
        }
    }

    @PostMapping("/{pin}/decreaseB")
    public ResponseEntity<Map<String, Integer>> decreaseBWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.pointDown(1, -1);
            log.info("Decreased score for Team B (PIN: {})", pin);
            // Đồng bộ tổng điểm set hiện tại (không thêm token)
            tryUpdateChiTietVanTotalsOnlyForPin(pin);
            try {
                appLog.minusB(match);
            } catch (Exception ignore) {
            }
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(view(pin));
        }
    }

    @PostMapping("/{pin}/reset")
    public ResponseEntity<Map<String, Integer>> resetWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.resetAll();
            log.info("Reset scores to 0 - 0 (PIN: {})", pin);
            try {
                appLog.logTs("Đặt lại điểm");
                appLog.logScore(match.snapshot());
            } catch (Exception ignore) {
            }
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(view(pin));
        }
    }

    @PostMapping("/{pin}/next")
    public ResponseEntity<BadmintonMatch.Snapshot> nextGameWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.nextGame();
            try {
                appLog.nextGame(match);
            } catch (Exception ignore) {
            }
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(match.snapshot());
        }
    }

    @PostMapping("/{pin}/swap")
    public ResponseEntity<BadmintonMatch.Snapshot> swapEndsWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.swapEnds();
            try {
                appLog.swapEnds(match);
            } catch (Exception ignore) {
            }
            // Ghi dấu SWAP vào CHI_TIET_VAN qua control panel (nếu có)
            tryUpdateChiTietVanSwapMarkerForPin(pin);
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(match.snapshot());
        }
    }

    @PostMapping("/{pin}/change-server")
    public ResponseEntity<BadmintonMatch.Snapshot> changeServerWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.changeServer();
            try {
                appLog.logTs("Đổi giao cầu");
                appLog.logScore(match.snapshot());
            } catch (Exception ignore) {
            }
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(match.snapshot());
        }
    }

    @PostMapping("/{pin}/undo")
    public ResponseEntity<BadmintonMatch.Snapshot> undoWithPin(@PathVariable String pin) {
        synchronized (LOCK) {

            BadmintonMatch match = getOrCreateMatch(pin);
            match.undo();
            try {
                appLog.undo(match);
            } catch (Exception ignore) {
            }
            // Hoàn tác: đồng bộ tổng điểm set hiện tại (không thêm token)
            tryUpdateChiTietVanTotalsOnlyForPin(pin);
            broadcastSnapshotToPin(pin);
            return ResponseEntity.ok(match.snapshot());
        }
    }

    /*
     * ---------- Public method to get match by PIN for desktop app sync ----------
     */
    public static BadmintonMatch getMatchByPin(String pinCode) {
        ScoreboardPinController instance = getControllerInstance();
        if (instance != null) {
            synchronized (instance.LOCK) {
                return instance.getOrCreateMatch(pinCode);
            }
        }
        return null;
    }

    private static ScoreboardPinController controllerInstance;

    private static ScoreboardPinController getControllerInstance() {
        return controllerInstance;
    }

    /* ---------- Generic action endpoint for JavaScript compatibility ---------- */
    @PostMapping("/{pin}/{action}")
    public ResponseEntity<Map<String, Integer>> handleActionWithPin(@PathVariable String pin,
            @PathVariable String action) {
        log.info("Received {} action for PIN: {}", action, pin);

        synchronized (LOCK) {
            try {
                BadmintonMatch match = getOrCreateMatch(pin);

                switch (action) {
                    case "increaseA" -> {
                        match.pointTo(0);
                        log.info("Increased score for Team A (PIN: {}), new score: {}", pin, match.getScore()[0]);
                        try {
                            appLog.plusA(match);
                        } catch (Exception ignore) {
                        }
                        tryUpdateChiTietVanOnPointForPin(pin, 0);
                    }
                    case "decreaseA" -> {
                        match.pointDown(0, -1);
                        log.info("Decreased score for Team A (PIN: {})", pin);
                        try {
                            appLog.minusA(match);
                        } catch (Exception ignore) {
                        }
                        tryUpdateChiTietVanTotalsOnlyForPin(pin);
                    }
                    case "increaseB" -> {
                        match.pointTo(1);
                        log.info("Increased score for Team B (PIN: {}), new score: {}", pin, match.getScore()[1]);
                        try {
                            appLog.plusB(match);
                        } catch (Exception ignore) {
                        }
                        tryUpdateChiTietVanOnPointForPin(pin, 1);
                    }
                    case "decreaseB" -> {
                        match.pointDown(1, -1);
                        log.info("Decreased score for Team B (PIN: {})", pin);
                        try {
                            appLog.minusB(match);
                        } catch (Exception ignore) {
                        }
                        tryUpdateChiTietVanTotalsOnlyForPin(pin);
                    }
                    case "reset" -> {
                        match.resetAll();
                        log.info("Reset match for PIN: {}", pin);
                        // Log reset action (no specific method available)
                        try {
                            appLog.logTs("Reset match for PIN: %s", pin);
                        } catch (Exception ignore) {
                        }
                    }
                    case "next" -> {
                        match.nextGame();
                        log.info("Next game for PIN: {}", pin);
                        try {
                            appLog.nextGame(match);
                        } catch (Exception ignore) {
                        }
                    }
                    case "swap" -> {
                        log.info("=== SWAP ENDS REQUEST for PIN: {} ===", pin);

                        // Log trạng thái trước khi swap
                        BadmintonMatch.Snapshot beforeSwap = match.snapshot();
                        log.info("Trước khi đổi sân - VĐV A: '%s' (Điểm: %d, Ván: %d), VĐV B: '%s' (Điểm: %d, Ván: %d)",
                                beforeSwap.names[0], beforeSwap.score[0], beforeSwap.games[0],
                                beforeSwap.names[1], beforeSwap.score[1], beforeSwap.games[1]);

                        match.swapEnds();
                        log.info("Đã thực hiện swap ends cho PIN: {}", pin);

                        // Log trạng thái sau khi swap
                        BadmintonMatch.Snapshot afterSwap = match.snapshot();
                        log.info("Sau khi đổi sân - VĐV A: '%s' (Điểm: %d, Ván: %d), VĐV B: '%s' (Điểm: %d, Ván: %d)",
                                afterSwap.names[0], afterSwap.score[0], afterSwap.games[0],
                                afterSwap.names[1], afterSwap.score[1], afterSwap.games[1]);

                        try {
                            appLog.swapEnds(match);
                        } catch (Exception ignore) {
                            log.warn("Error logging swapEnds for PIN {}: {}", pin, ignore.getMessage());
                        }
                        // Ghi dấu SWAP vào CHI_TIET_VAN qua control panel (nếu có)
                        tryUpdateChiTietVanSwapMarkerForPin(pin);
                    }
                    case "change-server" -> {
                        log.info("=== CHANGE SERVER REQUEST for PIN: {} ===", pin);
                        match.changeServer();
                        log.info("Đã thực hiện change server cho PIN: {}", pin);
                        try {
                            appLog.logTs("Đổi giao cầu");
                            appLog.logScore(match.snapshot());
                        } catch (Exception ignore) {
                            log.warn("Error logging changeServer for PIN {}: {}", pin, ignore.getMessage());
                        }
                    }
                    case "undo" -> {
                        match.undo();
                        log.info("Undo action for PIN: {}", pin);
                        try {
                            appLog.undo(match);
                        } catch (Exception ignore) {
                        }
                        tryUpdateChiTietVanTotalsOnlyForPin(pin);
                    }
                    default -> {
                        log.warn("Unknown action '{}' for PIN: {}", action, pin);
                        return ResponseEntity.badRequest().body(Map.of("teamAScore", 0, "teamBScore", 0));
                    }
                }

                broadcastSnapshotToPin(pin);
                Map<String, Integer> result = view(pin);
                log.info("Action {} completed for PIN {}, result: {}", action, pin, result);
                return ResponseEntity.ok(result);

            } catch (Exception e) {
                log.error("Error in action {} for PIN {}: {}", action, pin, e.getMessage(), e);
                return ResponseEntity.status(500).body(Map.of("teamAScore", 0, "teamBScore", 0));
            }
        }
    }

    /** Gọi helper trên control panel để append SWAP@ và đồng bộ tổng điểm. */
    private void tryUpdateChiTietVanSwapMarkerForPin(String pinCode) {
        try {
            Map<String, CourtManagerService.CourtStatus> all = courtManager.getAllCourtStatus();
            String courtId = null;
            for (var cs : all.values()) {
                if (pinCode != null && pinCode.equals(cs.pinCode)) {
                    courtId = cs.courtId;
                    break;
                }
            }
            if (courtId == null)
                return;
            var session = courtManager.getCourt(courtId);
            if (session == null || session.controlPanel == null)
                return;
            Object panel = session.controlPanel;
            try {
                var m = panel.getClass().getDeclaredMethod("appendSwapMarkerAndResyncChiTietVan");
                m.setAccessible(true);
                m.invoke(panel);
            } catch (NoSuchMethodException ignore) {
                // Control panel chưa có helper (phiên bản cũ)
            }
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException ex) {
            log.warn("CHI_TIET_VAN swap marker (web) failed for PIN {}: {}", pinCode, ex.getMessage());
        }
    }
}
