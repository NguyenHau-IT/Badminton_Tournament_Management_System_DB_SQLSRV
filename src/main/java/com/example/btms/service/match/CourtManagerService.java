package com.example.btms.service.match;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;

import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.model.match.CourtSession;
import com.example.btms.util.threading.SerialExecutor;

/**
 * Service quản lý nhiều sân cầu lông trên một máy
 */
public class CourtManagerService {
    private static final CourtManagerService INSTANCE = new CourtManagerService();

    private final Map<String, CourtSession> courtSessions = new ConcurrentHashMap<>();
    private final Map<String, BadmintonMatch> courtMatches = new ConcurrentHashMap<>();
    // Mỗi sân có một SerialExecutor để đảm bảo các thao tác trên sân đó chạy tuần
    // tự
    private final Map<String, SerialExecutor> courtExecutors = new ConcurrentHashMap<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    // Backing executor dùng chung cho tất cả SerialExecutor của sân: Virtual
    // Threads (Java 21)
    private static final ExecutorService COURT_BACKING_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private CourtManagerService() {
    }

    public static CourtManagerService getInstance() {
        return INSTANCE;
    }

    /**
     * Tạo sân mới
     */
    public CourtSession createCourt(String courtId, String header) {
        CourtSession session = new CourtSession(courtId, header, new BadmintonMatch());
        courtSessions.put(courtId, session);
        courtMatches.put(courtId, session.match);
        // Tạo SerialExecutor cho sân này
        courtExecutors.put(courtId, new SerialExecutor(COURT_BACKING_EXECUTOR, "Court-" + courtId));

        pcs.firePropertyChange("courtAdded", null, courtId);
        return session;
    }

    /**
     * Lấy sân theo ID
     */
    public CourtSession getCourt(String courtId) {
        return courtSessions.get(courtId);
    }

    /**
     * Lấy trận đấu của sân
     */
    public BadmintonMatch getCourtMatch(String courtId) {
        return courtMatches.get(courtId);
    }

    /**
     * Lấy danh sách tất cả các sân
     */
    public List<String> getAllCourtIds() {
        return new ArrayList<>(courtSessions.keySet());
    }

    /**
     * Lấy danh sách các sân đang hoạt động
     */
    public List<String> getActiveCourtIds() {
        List<String> active = new ArrayList<>();
        for (Map.Entry<String, CourtSession> entry : courtSessions.entrySet()) {
            if (entry.getValue().display != null) {
                active.add(entry.getKey());
            }
        }
        return active;
    }

    /**
     * Có bất kỳ sân nào đang mở khung hiển thị hoặc có bảng điều khiển hay không.
     * "Mở" nghĩa là người dùng đã mở cửa sổ hiển thị (display != null) hoặc đang có
     * panel điều khiển gắn với sân (controlPanel != null).
     */
    public boolean hasAnyOpenCourt() {
        for (CourtSession session : courtSessions.values()) {
            if (session != null && (session.display != null || session.controlPanel != null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Xóa sân
     */
    public void removeCourt(String courtId) {
        CourtSession session = courtSessions.remove(courtId);
        courtMatches.remove(courtId);
        // Dọn executor hàng đợi cho sân này
        SerialExecutor ex = courtExecutors.remove(courtId);
        if (ex != null) {
            try {
                ex.clearQueue();
            } catch (Exception ignore) {
            }
        }

        if (session != null) {
            // Đóng display frame nếu có
            if (session.display != null) {
                try {
                    session.display.dispose();
                } catch (Exception e) {
                    // Ignore dispose errors
                }
            }
            // Xóa controlPanel reference
            session.controlPanel = null;
        }

        pcs.firePropertyChange("courtRemoved", null, courtId);
    }

    /**
     * Đóng tất cả các sân
     */
    public void closeAllCourts() {
        for (CourtSession session : courtSessions.values()) {
            if (session.display != null) {
                try {
                    session.display.dispose();
                } catch (Exception e) {
                    // Ignore dispose errors
                }
            }
            // Xóa controlPanel reference
            session.controlPanel = null;
        }
        courtSessions.clear();
        courtMatches.clear();
        // Dọn tất cả executor hàng đợi (backing executor shared vẫn giữ để tái sử dụng)
        for (SerialExecutor ex : courtExecutors.values()) {
            try {
                ex.clearQueue();
            } catch (Exception ignore) {
            }
        }
        courtExecutors.clear();
        pcs.firePropertyChange("allCourtsClosed", null, null);
    }

    /**
     * Gửi một tác vụ cần thực thi tuần tự theo sân (đảm bảo không race trên state
     * của sân đó).
     * Trả về CompletableFuture để caller theo dõi hoàn tất.
     */
    public CompletableFuture<Void> submitToCourt(String courtId, Runnable task) {
        SerialExecutor ex = courtExecutors.get(courtId);
        if (ex == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Court not found: " + courtId));
        }
        return ex.submit(task);
    }

    /**
     * Đăng ký listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Hủy đăng ký listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Lấy tổng quan trạng thái tất cả các sân
     */
    public Map<String, CourtStatus> getAllCourtStatus() {
        Map<String, CourtStatus> status = new HashMap<>();
        for (Map.Entry<String, CourtSession> entry : courtSessions.entrySet()) {
            String courtId = entry.getKey();
            CourtSession session = entry.getValue();
            BadmintonMatch match = session.match;

            // Lấy thông tin từ BadmintonControlPanel nếu có, nếu không thì từ
            // BadmintonMatch
            String[] names = match.getNames();
            int[] score = match.getScore();
            int[] games = match.getGames();
            int gameNumber = match.getGameNumber();
            int bestOf = match.getBestOf();
            int server = match.getServer();
            boolean doubles = match.isDoubles();
            boolean isPlaying = false; // Đang thi đấu
            boolean isPaused = false; // Tạm dừng (nghỉ giữa ván hoặc tạm dừng thủ công)
            boolean isFinished = false; // Kết thúc trận
            boolean hasNames = false; // Đã chọn tên hai bên (không phải placeholder)
            boolean hasStarted = false; // Đã bấm bắt đầu trận

            // Nếu có controlPanel, cập nhật thông tin từ đó
            if (session.controlPanel != null) {
                try {
                    // Sử dụng reflection để lấy thông tin từ BadmintonControlPanel
                    // Lấy tên đội từ các combo box
                    java.lang.reflect.Field cboNameAField = session.controlPanel.getClass()
                            .getDeclaredField("cboNameA");
                    java.lang.reflect.Field cboNameBField = session.controlPanel.getClass()
                            .getDeclaredField("cboNameB");
                    java.lang.reflect.Field cboTeamAField = session.controlPanel.getClass()
                            .getDeclaredField("cboTeamA");
                    java.lang.reflect.Field cboTeamBField = session.controlPanel.getClass()
                            .getDeclaredField("cboTeamB");
                    java.lang.reflect.Field bestOfField = session.controlPanel.getClass().getDeclaredField("bestOf");
                    java.lang.reflect.Field doublesField = session.controlPanel.getClass().getDeclaredField("doubles");

                    cboNameAField.setAccessible(true);
                    cboNameBField.setAccessible(true);
                    cboTeamAField.setAccessible(true);
                    cboTeamBField.setAccessible(true);
                    bestOfField.setAccessible(true);
                    doublesField.setAccessible(true);

                    javax.swing.JComboBox<?> cboNameA = (javax.swing.JComboBox<?>) cboNameAField
                            .get(session.controlPanel);
                    javax.swing.JComboBox<?> cboNameB = (javax.swing.JComboBox<?>) cboNameBField
                            .get(session.controlPanel);
                    javax.swing.JComboBox<?> cboTeamA = (javax.swing.JComboBox<?>) cboTeamAField
                            .get(session.controlPanel);
                    javax.swing.JComboBox<?> cboTeamB = (javax.swing.JComboBox<?>) cboTeamBField
                            .get(session.controlPanel);
                    javax.swing.JComboBox<?> bestOfCombo = (javax.swing.JComboBox<?>) bestOfField
                            .get(session.controlPanel);
                    javax.swing.JCheckBox doublesCheck = (javax.swing.JCheckBox) doublesField.get(session.controlPanel);

                    // Lấy trạng thái đã bắt đầu từ control panel nếu có
                    try {
                        java.lang.reflect.Field hasStartedField = session.controlPanel.getClass()
                                .getDeclaredField("hasStarted");
                        hasStartedField.setAccessible(true);
                        Object hs = hasStartedField.get(session.controlPanel);
                        if (hs instanceof Boolean b) {
                            hasStarted = b.booleanValue();
                        }
                    } catch (Exception ignore) {
                        // fallback sẽ tính sau
                    }

                    // Cập nhật tên đội từ UI
                    if (doublesCheck != null && doublesCheck.isSelected()) {
                        // Đánh đôi - lấy từ cboTeamA và cboTeamB
                        if (cboTeamA.getSelectedItem() != null
                                && !cboTeamA.getSelectedItem().toString().contains("— Chọn đội —")) {
                            names[0] = cboTeamA.getSelectedItem().toString();
                        }
                        if (cboTeamB.getSelectedItem() != null
                                && !cboTeamB.getSelectedItem().toString().contains("— Chọn đội —")) {
                            names[1] = cboTeamB.getSelectedItem().toString();
                        }
                    } else {
                        // Đánh đơn - lấy từ cboNameA và cboNameB
                        if (cboNameA.getSelectedItem() != null
                                && !cboNameA.getSelectedItem().toString().contains("— Chọn VĐV —")) {
                            names[0] = cboNameA.getSelectedItem().toString();
                        }
                        if (cboNameB.getSelectedItem() != null
                                && !cboNameB.getSelectedItem().toString().contains("— Chọn VĐV —")) {
                            names[1] = cboNameB.getSelectedItem().toString();
                        }
                    }

                    // Cập nhật thông tin khác
                    if (bestOfCombo.getSelectedItem() != null) {
                        String bestOfText = bestOfCombo.getSelectedItem().toString();
                        if (bestOfText.contains("1"))
                            bestOf = 1;
                        else if (bestOfText.contains("3"))
                            bestOf = 3;
                        else if (bestOfText.contains("5"))
                            bestOf = 5;
                    }

                    if (doublesCheck != null) {
                        doubles = doublesCheck.isSelected();
                    }

                    // Cập nhật điểm số và ván từ BadmintonMatch (đã được cập nhật bởi
                    // BadmintonControlPanel)
                    try {
                        // Lấy BadmintonMatch từ controlPanel để có thông tin mới nhất
                        java.lang.reflect.Method getMatchMethod = session.controlPanel.getClass()
                                .getDeclaredMethod("getMatch");
                        getMatchMethod.setAccessible(true);
                        Object currentMatch = getMatchMethod.invoke(session.controlPanel);

                        if (currentMatch != null) {
                            // Lấy điểm số mới nhất
                            java.lang.reflect.Method getScoreMethod = currentMatch.getClass()
                                    .getDeclaredMethod("getScore");
                            getScoreMethod.setAccessible(true);
                            score = (int[]) getScoreMethod.invoke(currentMatch);

                            // Lấy ván mới nhất
                            java.lang.reflect.Method getGamesMethod = currentMatch.getClass()
                                    .getDeclaredMethod("getGames");
                            getGamesMethod.setAccessible(true);
                            games = (int[]) getGamesMethod.invoke(currentMatch);

                            // Lấy ván hiện tại
                            java.lang.reflect.Method getGameNumberMethod = currentMatch.getClass()
                                    .getDeclaredMethod("getGameNumber");
                            getGameNumberMethod.setAccessible(true);
                            gameNumber = (Integer) getGameNumberMethod.invoke(currentMatch);

                            // Lấy server hiện tại
                            java.lang.reflect.Method getServerMethod = currentMatch.getClass()
                                    .getDeclaredMethod("getServer");
                            getServerMethod.setAccessible(true);
                            server = (Integer) getServerMethod.invoke(currentMatch);

                            // Trạng thái trận đấu: dùng các getter công khai nếu có
                            try {
                                java.lang.reflect.Method isBetween = currentMatch.getClass()
                                        .getDeclaredMethod("isBetweenGamesInterval");
                                java.lang.reflect.Method isManualPausedM = null;
                                try {
                                    isManualPausedM = currentMatch.getClass().getDeclaredMethod("isManualPaused");
                                } catch (NoSuchMethodException ignore) {
                                }
                                java.lang.reflect.Method isFinishedM = currentMatch.getClass()
                                        .getDeclaredMethod("isMatchFinished");
                                isBetween.setAccessible(true);
                                isFinishedM.setAccessible(true);
                                boolean pausedBetween = (Boolean) isBetween.invoke(currentMatch);
                                boolean pausedManual = false;
                                if (isManualPausedM != null) {
                                    isManualPausedM.setAccessible(true);
                                    pausedManual = (Boolean) isManualPausedM.invoke(currentMatch);
                                }
                                isPaused = pausedBetween || pausedManual;
                                isFinished = (Boolean) isFinishedM.invoke(currentMatch);
                            } catch (Exception ignore) {
                                // ignore
                            }
                            // Đã bắt đầu khi có điểm/ván hoặc gameNumber > 1
                            if (!hasStarted) {
                                hasStarted = (score[0] > 0 || score[1] > 0 || games[0] > 0 || games[1] > 0
                                        || gameNumber > 1);
                            }
                            isPlaying = hasStarted && !isFinished && !isPaused;
                        }
                    } catch (Exception e) {
                        // Nếu không thể lấy từ controlPanel, giữ nguyên thông tin từ match
                        System.err.println("Không thể lấy điểm số từ BadmintonControlPanel: " + e.getMessage());
                        // Fallback: kiểm tra điểm số để xác định trạng thái
                        isFinished = match.isMatchFinished();
                        boolean pausedManual = false;
                        try {
                            java.lang.reflect.Method isManualPausedM = match.getClass()
                                    .getDeclaredMethod("isManualPaused");
                            isManualPausedM.setAccessible(true);
                            pausedManual = (Boolean) isManualPausedM.invoke(match);
                        } catch (Exception ignore2) {
                        }
                        isPaused = match.isBetweenGamesInterval() || pausedManual;
                        if (!hasStarted) {
                            hasStarted = (score[0] > 0 || score[1] > 0 || games[0] > 0 || games[1] > 0
                                    || gameNumber > 1);
                        }
                        isPlaying = hasStarted
                                && !isFinished && !isPaused;
                    }

                    // Cập nhật header (nội dung trận đấu) từ UI
                    try {
                        java.lang.reflect.Field cboHeaderSinglesField = session.controlPanel.getClass()
                                .getDeclaredField("cboHeaderSingles");
                        java.lang.reflect.Field cboHeaderDoublesField = session.controlPanel.getClass()
                                .getDeclaredField("cboHeaderDoubles");

                        cboHeaderSinglesField.setAccessible(true);
                        cboHeaderDoublesField.setAccessible(true);

                        javax.swing.JComboBox<?> cboHeaderSingles = (javax.swing.JComboBox<?>) cboHeaderSinglesField
                                .get(session.controlPanel);
                        javax.swing.JComboBox<?> cboHeaderDoubles = (javax.swing.JComboBox<?>) cboHeaderDoublesField
                                .get(session.controlPanel);

                        if (doubles) {
                            // Đánh đôi
                            if (cboHeaderDoubles.getSelectedItem() != null && !cboHeaderDoubles.getSelectedItem()
                                    .toString().contains("— Chọn nội dung đôi —")) {
                                session.header = cboHeaderDoubles.getSelectedItem().toString();
                            }
                        } else {
                            // Đánh đơn
                            if (cboHeaderSingles.getSelectedItem() != null && !cboHeaderSingles.getSelectedItem()
                                    .toString().contains("— Chọn nội dung đơn —")) {
                                session.header = cboHeaderSingles.getSelectedItem().toString();
                            }
                        }
                    } catch (Exception e) {
                        // Nếu không thể lấy header, giữ nguyên header cũ
                        System.err.println("Không thể lấy header từ BadmintonControlPanel: " + e.getMessage());
                    }

                } catch (Exception e) {
                    // Nếu có lỗi, sử dụng thông tin từ BadmintonMatch
                    System.err.println("Không thể lấy thông tin từ BadmintonControlPanel: " + e.getMessage());
                }
            }

            // Xác định đã chọn tên (không phải placeholder)
            hasNames = (names != null
                    && names.length >= 2
                    && names[0] != null && !names[0].isBlank() && !names[0].equals("Team A")
                    && !names[0].contains("— Chọn")
                    && names[1] != null && !names[1].isBlank() && !names[1].equals("Team B")
                    && !names[1].contains("— Chọn"));

            status.put(courtId, new CourtStatus(
                    courtId,
                    session.header,
                    session.display != null || session.controlPanel != null,
                    names,
                    score,
                    games,
                    gameNumber,
                    bestOf,
                    server,
                    doubles,
                    session.pinCode,
                    isPlaying,
                    isPaused,
                    isFinished,
                    hasNames));
        }
        return status;
    }

    /**
     * Class chứa thông tin trạng thái của một sân
     */
    public static class CourtStatus {
        public final String courtId;
        public final String header;
        public final boolean isDisplayOpen;
        public final String[] names;
        public final int[] score;
        public final int[] games;
        public final int gameNumber;
        public final int bestOf;
        public final int server;
        public final boolean doubles;
        public final String pinCode; // Mã PIN của sân
        public final boolean isPlaying; // Đang thi đấu
        public final boolean isPaused; // Tạm dừng (nghỉ giữa ván)
        public final boolean isFinished; // Kết thúc trận
        public final boolean hasNames; // Đã chọn tên hai bên

        public CourtStatus(String courtId, String header, boolean isDisplayOpen,
                String[] names, int[] score, int[] games, int gameNumber,
                int bestOf, int server, boolean doubles, String pinCode,
                boolean isPlaying, boolean isPaused, boolean isFinished, boolean hasNames) {
            this.courtId = courtId;
            this.header = header;
            this.isDisplayOpen = isDisplayOpen;
            this.names = names;
            this.score = score;
            this.games = games;
            this.gameNumber = gameNumber;
            this.bestOf = bestOf;
            this.server = server;
            this.doubles = doubles;
            this.pinCode = pinCode;
            this.isPlaying = isPlaying;
            this.isPaused = isPaused;
            this.isFinished = isFinished;
            this.hasNames = hasNames;
        }
    }
}
