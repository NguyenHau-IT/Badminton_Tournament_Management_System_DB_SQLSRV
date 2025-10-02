package com.example.btms.model.match;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Luật cầu lông hiện hành (rally point):
 * - Mỗi game đến 21, hơn 2, chốt trần 30
 * - Best of 3 (có thể chỉnh 1/3/5)
 * - Đổi sân: giữa các game; game 3 đổi khi một bên chạm 11
 * - Giao bóng thuộc về người thắng rally; đơn: chẵn=phải (R), lẻ=trái (L)
 */
public class BadmintonMatch {
    // ====== Cập nhật điểm số từ API/web ======
    public void setScore(int scoreA, int scoreB) {
        int oldA = this.score[0];
        int oldB = this.score[1];
        this.score[0] = Math.max(0, scoreA);
        this.score[1] = Math.max(0, scoreB);
        pcs.firePropertyChange("scoreA", oldA, this.score[0]);
        pcs.firePropertyChange("scoreB", oldB, this.score[1]);
        pcs.firePropertyChange("score", null, snapshot());
    }

    // ====== Ảnh chụp trạng thái ======
    public static class Snapshot {
        public final String[] names; // tên 2 bên
        public final String[] clubs; // tên CLB 2 bên
        public final int[] score; // điểm game hiện tại
        public final int[] games; // số game đã thắng
        public final int gameNumber; // 1..bestOf
        public final int server; // 0 hoặc 1
        public final boolean doubles; // chế độ đôi
        public final boolean betweenGamesInterval; // đang nghỉ giữa game
        public final boolean changedEndsThisGame; // đã đổi sân trong game này (game 3 @11)
        public final boolean matchFinished;
        public final int bestOf;
        public final long elapsedSec;
        public final int[][] gameScores; // điểm chi tiết của từng ván đã hoàn thành

        Snapshot(String[] names, String[] clubs, int[] score, int[] games, int gameNumber, int server, boolean doubles,
                boolean betweenGamesInterval, boolean changedEndsThisGame, boolean matchFinished, int bestOf,
                long elapsedSec, int[][] gameScores) {
            this.names = new String[] { names[0], names[1] };
            this.clubs = new String[] { clubs[0], clubs[1] };
            this.score = new int[] { score[0], score[1] };
            this.games = new int[] { games[0], games[1] };
            this.gameNumber = gameNumber;
            this.server = server;
            this.doubles = doubles;
            this.betweenGamesInterval = betweenGamesInterval;
            this.changedEndsThisGame = changedEndsThisGame;
            this.matchFinished = matchFinished;
            this.bestOf = bestOf;
            this.elapsedSec = elapsedSec;
            this.gameScores = gameScores;
        }
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private final String[] names = new String[] { "Team A", "Team B" };
    private final String[] clubs = new String[] { "", "" };
    private final int[] score = new int[] { 0, 0 };
    private final int[] games = new int[] { 0, 0 };
    private int gameNumber = 1;
    private int bestOf = 3;
    private int server = 0; // 0 hoặc 1
    private boolean doubles = false;
    private boolean betweenGamesInterval = false;
    private boolean changedEndsThisGame = false;
    private boolean matchFinished = false;
    // Tạm dừng thủ công trong ván (khác với nghỉ giữa ván)
    private boolean manualPaused = false;

    private Instant startTime = Instant.now();
    private Instant gameStartTime = Instant.now();

    private static class Action {
        final int prevServer;
        final int[] prevScore = new int[2];
        final int[] prevGames = new int[2];
        final int prevGameNumber;
        final boolean prevBetween;
        final boolean prevChangedEnds;
        final boolean prevFinished;

        Action(int prevServer, int[] score, int[] games, int prevGameNumber,
                boolean prevBetween, boolean prevChangedEnds, boolean prevFinished) {
            this.prevServer = prevServer;
            this.prevScore[0] = score[0];
            this.prevScore[1] = score[1];
            this.prevGames[0] = games[0];
            this.prevGames[1] = games[1];
            this.prevGameNumber = prevGameNumber;
            this.prevBetween = prevBetween;
            this.prevChangedEnds = prevChangedEnds;
            this.prevFinished = prevFinished;
        }
    }

    private final Deque<Action> history = new ArrayDeque<>();

    // Lưu điểm cuối cùng của từng ván đã hoàn thành
    private final int[][] completedGameScores = new int[5][2]; // Hỗ trợ tối đa 5 ván

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public Snapshot snapshot() {
        long elapsed = Duration.between(startTime, Instant.now()).getSeconds();

        // Tạo mảng điểm các ván đã hoàn thành
        int[][] gameScores = new int[gameNumber - 1][2];
        for (int i = 0; i < gameNumber - 1; i++) {
            // Lấy điểm từ mảng completedGameScores nếu có
            if (completedGameScores[i][0] >= 0) {
                gameScores[i][0] = completedGameScores[i][0];
                gameScores[i][1] = completedGameScores[i][1];
            } else {
                // Nếu không có trong completedGameScores, sử dụng điểm mặc định
                gameScores[i][0] = -1;
                gameScores[i][1] = -1;
            }
        }

        return new Snapshot(names, clubs, score, games, gameNumber, server, doubles, betweenGamesInterval,
                changedEndsThisGame,
                matchFinished, bestOf, elapsed, gameScores);
    }

    public void setNames(String a, String b) {
        names[0] = a == null || a.isBlank() ? "Team A" : a.trim();
        names[1] = b == null || b.isBlank() ? "Team B" : b.trim();
        pcs.firePropertyChange("names", null, snapshot());
    }

    public void setClubs(String clubA, String clubB) {
        clubs[0] = clubA == null ? "" : clubA.trim();
        clubs[1] = clubB == null ? "" : clubB.trim();
        pcs.firePropertyChange("clubs", null, snapshot());
    }

    public void setBestOf(int bo) {
        if (bo != 1 && bo != 3 && bo != 5)
            bo = 3;
        bestOf = bo;
        pcs.firePropertyChange("bestOf", null, snapshot());
    }

    public void setDoubles(boolean d) {
        doubles = d;
        pcs.firePropertyChange("doubles", null, snapshot());
    }

    public void startMatch(int initialServer) {
        resetAll();
        server = (initialServer == 1) ? 1 : 0;
        pcs.firePropertyChange("start", null, snapshot());
    }

    public void pointTo(int side) {
        if (matchFinished || betweenGamesInterval || manualPaused)
            return;
        saveState();
        score[side]++;
        server = side; // rally winner serves next

        // game 3: đổi sân khi một bên chạm 11
        if (gameNumber == 3 && !changedEndsThisGame && (score[0] == 11 || score[1] == 11)) {
            changedEndsThisGame = true;
            pcs.firePropertyChange("changeEndsAt11", null, snapshot());
        }

        // kiểm tra kết thúc game
        int winner = gameWinner();
        if (winner >= 0) {
            games[winner]++;
            betweenGamesInterval = true;

            // Lưu điểm cuối cùng của ván này vào mảng completedGameScores
            int gameIndex = gameNumber - 1;
            if (gameIndex >= 0 && gameIndex < completedGameScores.length) {
                completedGameScores[gameIndex][0] = score[0];
                completedGameScores[gameIndex][1] = score[1];
            }

            pcs.firePropertyChange("gameEnd", null, snapshot());

            // kiểm tra kết thúc trận
            int need = (bestOf / 2) + 1;
            if (games[winner] >= need) {
                matchFinished = true;
                betweenGamesInterval = false;
                pcs.firePropertyChange("matchEnd", null, snapshot());
            }
        }

        pcs.firePropertyChange("score", null, snapshot());
    }

    public void pointDown(int side, int delta) {
        if (matchFinished || betweenGamesInterval || manualPaused || delta == 0)
            return;
        saveState();
        score[side] += delta;
        if (score[side] < 0)
            score[side] = 0; // không cho âm
        pcs.firePropertyChange("score", null, snapshot());
    }

    public void undo() {
        if (history.isEmpty())
            return;
        Action a = history.pop();
        server = a.prevServer;
        score[0] = a.prevScore[0];
        score[1] = a.prevScore[1];
        games[0] = a.prevGames[0];
        games[1] = a.prevGames[1];
        gameNumber = a.prevGameNumber;
        betweenGamesInterval = a.prevBetween;
        changedEndsThisGame = a.prevChangedEnds;
        matchFinished = a.prevFinished;
        pcs.firePropertyChange("undo", null, snapshot());
    }

    public void nextGame() {
        if (!betweenGamesInterval || matchFinished)
            return;

        // Lưu điểm cuối cùng của ván hiện tại vào mảng completedGameScores
        int gameIndex = gameNumber - 1;
        if (gameIndex >= 0 && gameIndex < completedGameScores.length) {
            completedGameScores[gameIndex][0] = score[0];
            completedGameScores[gameIndex][1] = score[1];
        }

        saveState();
        gameNumber++;
        score[0] = score[1] = 0;
        betweenGamesInterval = false;
        manualPaused = false; // bỏ tạm dừng thủ công khi sang ván mới
        changedEndsThisGame = false;
        // đổi sân giữa game (nếu cần, bật dòng dưới)
        // swapEnds();
        gameStartTime = Instant.now();
        pcs.firePropertyChange("nextGame", null, snapshot());
    }

    public void forfeit(int side) {
        if (matchFinished)
            return;
        saveState();
        games[side == 0 ? 1 : 0] = (bestOf / 2) + 1;
        matchFinished = true;
        betweenGamesInterval = false;
        manualPaused = false;
        pcs.firePropertyChange("matchEnd", null, snapshot());
    }

    public void resetAll() {
        saveState();
        score[0] = score[1] = 0;
        games[0] = games[1] = 0;
        gameNumber = 1;
        server = 0;
        betweenGamesInterval = false;
        manualPaused = false;
        changedEndsThisGame = false;
        matchFinished = false;
        startTime = Instant.now();
        gameStartTime = Instant.now();

        // Reset mảng điểm các ván đã hoàn thành
        for (int i = 0; i < completedGameScores.length; i++) {
            completedGameScores[i][0] = -1;
            completedGameScores[i][1] = -1;
        }

        pcs.firePropertyChange("reset", null, snapshot());
    }

    /** Đổi sân (đảo A/B) */
    public void swapEnds() {
        // Log trước khi đổi sân
        System.out.println("=== SWAP ENDS - TRƯỚC KHI ĐỔI ===");
        System.out.println("Tên VĐV: A='" + names[0] + "', B='" + names[1] + "'");
        System.out.println("Điểm hiện tại: A=" + score[0] + ", B=" + score[1]);
        System.out.println("Số ván thắng: A=" + games[0] + ", B=" + games[1]);
        System.out.println("Server: " + (server == 0 ? "A" : "B"));

        // Log điểm các ván đã hoàn thành
        for (int i = 0; i < completedGameScores.length; i++) {
            if (completedGameScores[i][0] >= 0) {
                System.out.println("Ván " + (i + 1) + " đã hoàn thành: A=" + completedGameScores[i][0] + ", B="
                        + completedGameScores[i][1]);
            }
        }

        // Đổi tên VĐV
        String tmpName = names[0];
        names[0] = names[1];
        names[1] = tmpName;

        // Đổi CLB
        String tmpClub = clubs[0];
        clubs[0] = clubs[1];
        clubs[1] = tmpClub;

        // Đổi điểm hiện tại
        int tmpScore = score[0];
        score[0] = score[1];
        score[1] = tmpScore;

        // Đổi số ván đã thắng
        int tmpGames = games[0];
        games[0] = games[1];
        games[1] = tmpGames;

        // Đổi server
        server = server == 0 ? 1 : 0;

        // Đổi điểm các ván đã hoàn thành
        for (int i = 0; i < completedGameScores.length; i++) {
            if (completedGameScores[i][0] >= 0) {
                int tmpGameScore = completedGameScores[i][0];
                completedGameScores[i][0] = completedGameScores[i][1];
                completedGameScores[i][1] = tmpGameScore;
            }
        }

        // Log sau khi đổi sân
        System.out.println("=== SWAP ENDS - SAU KHI ĐỔI ===");
        System.out.println("Tên VĐV: A='" + names[0] + "', B='" + names[1] + "'");
        System.out.println("Điểm hiện tại: A=" + score[0] + ", B=" + score[1]);
        System.out.println("Số ván thắng: A=" + games[0] + ", B=" + games[1]);
        System.out.println("Server: " + (server == 0 ? "A" : "B"));

        // Log điểm các ván đã hoàn thành sau khi đổi
        for (int i = 0; i < completedGameScores.length; i++) {
            if (completedGameScores[i][0] >= 0) {
                System.out.println("Ván " + (i + 1) + " đã hoàn thành: A=" + completedGameScores[i][0] + ", B="
                        + completedGameScores[i][1]);
            }
        }
        System.out.println("=====================================");

        pcs.firePropertyChange("swap", null, snapshot());
    }

    /** Sân giao bóng (đơn): chẵn=R, lẻ=L */
    public String serviceCourtFor(int side) {
        return (score[side] % 2 == 0) ? "R" : "L";
    }

    private int gameWinner() {
        for (int i = 0; i < 2; i++) {
            if (score[i] >= 21 && (score[i] - score[1 - i]) >= 2)
                return i;
            if (score[i] == 30 && score[1 - i] == 29)
                return i;
        }
        return -1;
    }

    private void saveState() {
        history.push(
                new Action(server, score, games, gameNumber, betweenGamesInterval, changedEndsThisGame, matchFinished));
    }

    public void toggleServer() {
        setServer(snapshot().server == 0 ? 1 : 0);
    }

    public void changeServer() {
        toggleServer();
    }

    public void setServer(int s) {
        if (s != 0 && s != 1)
            return;
        saveState();
        server = s;
        pcs.firePropertyChange("server", null, snapshot());
    }

    // ====== GETTERS phục vụ UI/log ======
    /** Trả về bản sao tỉ số hiện tại [A,B]. */
    public int[] getScore() {
        return new int[] { score[0], score[1] };
    }

    /** Trả về bản sao số ván đã thắng [A,B]. */
    public int[] getGames() {
        return new int[] { games[0], games[1] };
    }

    /** Trả về bản sao tên đội/vđv [A,B]. */
    public String[] getNames() {
        return new String[] { names[0], names[1] };
    }

    /** Trả về bản sao tên CLB [A,B]. */
    public String[] getClubs() {
        return new String[] { clubs[0], clubs[1] };
    }

    public int getServer() {
        return server;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public int getBestOf() {
        return bestOf;
    }

    public boolean isBetweenGamesInterval() {
        return betweenGamesInterval;
    }

    /** Trạng thái tạm dừng thủ công trong lúc đang chơi ván */
    public boolean isManualPaused() {
        return manualPaused;
    }

    /** Bật tạm dừng thủ công (không phải nghỉ giữa ván) */
    public void pauseManual() {
        if (matchFinished || betweenGamesInterval || manualPaused)
            return;
        saveState();
        manualPaused = true;
        pcs.firePropertyChange("manualPaused", false, true);
        pcs.firePropertyChange("status", null, snapshot());
    }

    /** Tắt tạm dừng thủ công (tiếp tục thi đấu) */
    public void resumeManual() {
        if (!manualPaused)
            return;
        saveState();
        manualPaused = false;
        pcs.firePropertyChange("manualPaused", true, false);
        pcs.firePropertyChange("status", null, snapshot());
    }

    public boolean isMatchFinished() {
        return matchFinished;
    }

    public boolean isDoubles() {
        return doubles;
    }

    /** Số giây đã trôi qua từ khi bắt đầu trận. */
    public long getElapsedSeconds() {
        return Duration.between(startTime, Instant.now()).getSeconds();
    }
}
