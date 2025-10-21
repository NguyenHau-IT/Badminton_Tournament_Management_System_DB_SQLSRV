package com.example.btms.util.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.example.btms.model.match.BadmintonMatch;

/**
 * Log hợp nhất cho toàn bộ ứng dụng:
 * - Hỗ trợ log thường & có timestamp
 * - Tiện ích log theo hành động trận đấu (điểm số, sang ván, đổi sân, v.v.)
 * - Có thể gắn/đổi JTextArea đích; thread-safe với Swing (append trên EDT)
 */
public class Log {

    /* ================== Config & target ================== */

    private volatile JTextArea target; // có thể attach từ UI
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** (Tương thích cũ) – nếu chưa attach, tạo sẵn 1 JTextArea nội bộ. */
    private JTextArea ensureTarget() {
        if (target == null) {
            synchronized (this) {
                if (target == null)
                    target = new JTextArea();
            }
        }
        return target;
    }

    /** Lấy JTextArea hiện tại (tạo mới nếu chưa có). */
    public JTextArea getLogArea() {
        return ensureTarget();
    }

    /** Gắn JTextArea từ UI để log đổ về đó. */
    public void attach(JTextArea area) {
        this.target = Objects.requireNonNull(area, "log area must not be null");
    }

    /* ================== Core logging ================== */

    /** Append raw (không tự thêm timestamp). */
    public void log(String fmt, Object... args) {
        String line = String.format(fmt, args) + "\n";
        append(line);
    }

    /** Append có timestamp ở đầu. */
    public void logTs(String fmt, Object... args) {
        String line = String.format("[%s] ", now()) + String.format(fmt, args) + "\n";
        append(line);
    }

    /** Trả về chuỗi đã có timestamp (utility nếu bạn muốn tự ghép). */
    public static String withTimestamp(String msg) {
        return String.format("[%s] %s", now(), msg);
    }

    private static String now() {
        return LocalDateTime.now().format(FMT);
    }

    private void append(String text) {
        JTextArea area = ensureTarget();
        if (SwingUtilities.isEventDispatchThread()) {
            area.append(text);
            area.setCaretPosition(area.getDocument().getLength());
        } else {
            SwingUtilities.invokeLater(() -> {
                area.append(text);
                area.setCaretPosition(area.getDocument().getLength());
            });
        }
    }

    /* ================== Log viewer utilities ================== */

    /** Lấy tất cả nội dung log hiện tại */
    public String getAllLogs() {
        JTextArea area = ensureTarget();
        return area.getText();
    }

    /** Xóa tất cả nội dung log */
    public void clearConsole() {
        JTextArea area = ensureTarget();
        if (SwingUtilities.isEventDispatchThread()) {
            area.setText("");
        } else {
            SwingUtilities.invokeLater(() -> area.setText(""));
        }
    }

    /* ================== Match-oriented helpers ================== */

    /** In tỉ số + ván hiện tại từ Snapshot. */
    public void logScore(BadmintonMatch.Snapshot s) {
        logTs("Tỉ số: %d - %d  |  Ván %d / BO%d", s.score[0], s.score[1], s.gameNumber, s.bestOf);
    }

    /** Sau khi cộng điểm A. Gọi sau khi state đã cập nhật. */
    public void plusA(BadmintonMatch m) {
        logTs("+1 A");
        logScore(m.snapshot());
    }

    /** Sau khi cộng điểm B. */
    public void plusB(BadmintonMatch m) {
        logTs("+1 B");
        logScore(m.snapshot());
    }

    /** Sau khi trừ điểm A. */
    public void minusA(BadmintonMatch m) {
        logTs("-1 A");
        logScore(m.snapshot());
    }

    /** Sau khi trừ điểm B. */
    public void minusB(BadmintonMatch m) {
        logTs("-1 B");
        logScore(m.snapshot());
    }

    /** Sau khi hoàn tác. */
    public void undo(BadmintonMatch m) {
        logTs("Hoàn tác");
        logScore(m.snapshot());
    }

    /** Sau khi chuyển sang ván tiếp theo. */
    public void nextGame(BadmintonMatch m) {
        var s = m.snapshot();
        logTs("Sang ván %d (BO%d) — Ván thắng: %d - %d", s.gameNumber, s.bestOf, s.games[0], s.games[1]);
    }

    /** Sau khi đổi sân. */
    public void swapEnds(BadmintonMatch m) {
        logTs("Đổi sân");
        logScore(m.snapshot());
    }

    /** Sau khi đổi giao cầu. */
    public void toggleServe(BadmintonMatch m) {
        var s = m.snapshot();
        logTs("Đổi giao cầu → %s", s.server == 0 ? "A" : "B");
    }

    /** Khi bắt đầu trận ĐƠN. */
    public void startSingles(String header, String nameA, int idA, String nameB, int idB, int bo) {
        logTs("Bắt đầu ĐƠN: \"%s\"  |  A=%s (NNR=%d) vs B=%s (NNR=%d)  |  BO%d",
                safe(header), safe(nameA), idA, safe(nameB), idB, bo);
    }

    /** Khi bắt đầu trận ĐÔI. */
    public void startDoubles(String header, String teamA, int teamAId, String teamB, int teamBId, int bo) {
        logTs("Bắt đầu ĐÔI: \"%s\"  |  TEAM A=%s (TEAMID=%d) vs TEAM B=%s (TEAMID=%d)  |  BO%d",
                safe(header), safe(teamA), teamAId, safe(teamB), teamBId, bo);
    }

    /** Khi kết thúc trận. */
    public void finishMatch() {
        logTs("— KẾT THÚC TRẬN —");
        append("\n");
    }

    /** Khi xác định người thắng. */
    public void logWinner(String winnerName) {
        logTs("Người thắng: %s", safe(winnerName));
        append("\n");
    }

    /** Khi chọn header đơn/đôi. */
    public void chooseSinglesHeader(String header, int knr) {
        logTs("Nội dung ĐƠN: \"%s\" (KNR=%d)", safe(header), knr);
    }

    public void chooseDoublesHeader(String header, int knr) {
        logTs("Nội dung ĐÔI: \"%s\" (KNR=%d)", safe(header), knr);
    }

    /** Khi chọn đội/VĐV. */
    public void chooseTeamA(String label, int teamId) {
        logTs("Chọn đội A: %s (TEAMID=%d)", safe(label), teamId);
    }

    public void chooseTeamB(String label, int teamId) {
        logTs("Chọn đội B: %s (TEAMID=%d)", safe(label), teamId);
    }

    public void choosePlayerA(String name, int nnr) {
        logTs("Chọn VĐV A: %s (NNR=%d)", safe(name), nnr);
    }

    public void choosePlayerB(String name, int nnr) {
        logTs("Chọn VĐV B: %s (NNR=%d)", safe(name), nnr);
    }

    /** Các thao tác hiển thị bảng điểm. */
    public void openDisplayVertical() {
        logTs("Mở bảng điểm dọc");
    }

    public void openDisplayHorizontal() {
        logTs("Mở bảng điểm ngang");
    }

    public void closeDisplays() {
        logTs("Đóng bảng điểm");
    }

    /* ================== Small helpers ================== */

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
