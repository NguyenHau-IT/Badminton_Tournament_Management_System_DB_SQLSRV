package com.example.btms.service.scoreboard;

import java.util.ArrayList;
import java.util.List;

import com.example.btms.model.match.BadmintonMatch;
import com.example.btms.util.threading.SwingThreadingUtils;

public final class ScoreboardHub {

    public enum DisplayKind {
        VERTICAL, HORIZONTAL
    }

    public static final class Board {
        public final BadmintonMatch match;
        public String header;
        public String partnerA; // tên VĐV (2) đội A (nếu đánh đôi), để MiniScorePanel hiển thị
        public String partnerB; // tên VĐV (2) đội B
        public DisplayKind kind;

        Board(BadmintonMatch m, String header, String a2, String b2, DisplayKind k) {
            this.match = m;
            this.header = header;
            this.partnerA = a2 == null ? "" : a2;
            this.partnerB = b2 == null ? "" : b2;
            this.kind = k == null ? DisplayKind.HORIZONTAL : k;
        }
    }

    private static final ScoreboardHub INSTANCE = new ScoreboardHub();

    public static ScoreboardHub get() {
        return INSTANCE;
    }

    private final List<Board> boards = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    private ScoreboardHub() {
    }

    /** Đăng ký (hoặc cập nhật) một bảng điểm theo BadmintonMatch */
    public synchronized void addOrUpdate(BadmintonMatch match, String header, String a2, String b2, DisplayKind kind) {
        Board b = findByMatch(match);
        if (b == null) {
            b = new Board(match, header, a2, b2, kind);
            boards.add(b);
        } else {
            b.header = header;
            b.partnerA = a2 == null ? "" : a2;
            b.partnerB = b2 == null ? "" : b2;
            b.kind = (kind == null ? b.kind : kind);
        }
        fireChanged();
    }

    /** Chỉ cập nhật header/partner khi đang đánh đôi */
    public synchronized void updateMeta(BadmintonMatch match, String header, String a2, String b2) {
        Board b = findByMatch(match);
        if (b != null) {
            if (header != null)
                b.header = header;
            if (a2 != null)
                b.partnerA = a2;
            if (b2 != null)
                b.partnerB = b2;
            fireChanged();
        }
    }

    public synchronized void removeByMatch(BadmintonMatch match) {
        boards.removeIf(b -> b.match == match);
        fireChanged();
    }

    /** Dashboard dùng để lấy danh sách hiển thị */
    public synchronized List<Board> snapshot() {
        return new ArrayList<>(boards);
    }

    public synchronized void addListener(Runnable r) {
        if (r != null && !listeners.contains(r))
            listeners.add(r);
    }

    public synchronized void removeListener(Runnable r) {
        listeners.remove(r);
    }

    private Board findByMatch(BadmintonMatch m) {
        for (Board b : boards)
            if (b.match == m)
                return b;
        return null;
    }

    private void fireChanged() {
        // 🚀 Sử dụng enhanced threading utils cho UI safety
        for (Runnable r : listeners) {
            SwingThreadingUtils.runOnEDT(r);
        }
    }
}
