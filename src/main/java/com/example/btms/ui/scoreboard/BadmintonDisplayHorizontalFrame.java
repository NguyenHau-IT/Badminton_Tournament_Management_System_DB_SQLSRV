package com.example.btms.ui.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.example.btms.model.match.BadmintonMatch;

public class BadmintonDisplayHorizontalFrame extends JFrame implements PropertyChangeListener {
    private java.util.Timer syncTimer;

    // Khởi động đồng bộ điểm số từ API
    public void startScoreSync(String apiUrl) {
        if (syncTimer != null)
            syncTimer.cancel();
        syncTimer = new java.util.Timer(true);
        syncTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    java.net.URL url = new java.net.URL(apiUrl);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    StringBuilder content;
                    try (java.io.BufferedReader in = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()))) {
                        content = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            content.append(inputLine);
                        }
                    }
                    conn.disconnect();

                    // Đọc JSON điểm số (parsing đơn giản)
                    String json = content.toString();
                    int scoreA = 0, scoreB = 0;
                    if (json.contains("teamAScore") && json.contains("teamBScore")) {
                        int idxA = json.indexOf("teamAScore");
                        int idxB = json.indexOf("teamBScore");
                        scoreA = Integer
                                .parseInt(json.substring(json.indexOf(':', idxA) + 1, json.indexOf(',', idxA)).trim());
                        int endB = json.indexOf('}', idxB);
                        if (endB == -1)
                            endB = json.length();
                        scoreB = Integer.parseInt(
                                json.substring(json.indexOf(':', idxB) + 1, endB).replaceAll("[^0-9]", "").trim());
                    }
                    final int finalA = scoreA, finalB = scoreB;
                    javax.swing.SwingUtilities.invokeLater(() -> match.setScore(finalA, finalB));
                } catch (IOException | NumberFormatException ignored) {
                    // Có thể log lỗi nếu cần
                }
            }
        }, 0, 1000); // mỗi 1 giây
    }

    private final BadmintonMatch match;

    private final JLabel matchInfo = new JLabel("TRẬN ĐẤU", SwingConstants.CENTER);
    private final JLabel playerA = new JLabel("VĐV 1", SwingConstants.LEFT);
    private final JLabel playerB = new JLabel("VĐV 2", SwingConstants.LEFT);
    // Ô hiển thị bên đang giao (đặt giữa tên và dãy điểm)
    private final JLabel serveIndicatorA = new JLabel("", SwingConstants.CENTER);
    private final JLabel serveIndicatorB = new JLabel("", SwingConstants.CENTER);

    // Số ván tối đa hiển thị: BO3
    private static final int MAX_GAMES = 3;
    // KHÔNG có khoảng cách giữa các ô (hgap=0, vgap=0)
    private final JPanel gamesBarA = new JPanel(new GridLayout(1, MAX_GAMES, 0, 0));
    private final JPanel gamesBarB = new JPanel(new GridLayout(1, MAX_GAMES, 0, 0));
    private final JLabel[] gameCellsA = new JLabel[MAX_GAMES];
    private final JLabel[] gameCellsB = new JLabel[MAX_GAMES];

    private final int[][] gameScore = new int[MAX_GAMES][2];

    private String headerText = "TRẬN ĐẤU";

    // Giữ để tương thích với code cũ, nhưng không sử dụng nữa.
    @SuppressWarnings("unused")
    private String partnerA = "";
    @SuppressWarnings("unused")
    private String partnerB = "";

    private static final int CELL_H = 130; // tăng để chắc chắn chứa 2 dòng tên + 1 dòng CLB
    private static final int NAME_COL_W = 600; // rộng hơn để chứa 2 dòng
    private static final int FONT_SIZE = 40;

    // Style
    private static final Color BG = Color.BLACK;
    private static final Color FG = Color.WHITE;
    private static final Color BG_CELL = new Color(24, 24, 24);
    private static final Color SEP_V_COLOR = new Color(70, 70, 70); // đường kẻ dọc giữa các ô
    private static final Color SEP_H_COLOR = new Color(70, 70, 70); // đường kẻ ngang giữa 2 phần
    private static final int SEP_H_THICK = 2; // độ dày đường kẻ ngang
    private static final EmptyBorder CELL_PADDING = new EmptyBorder(16, 24, 16, 24); // padding cân đối cho 1-2 chữ số

    // Font cố định cho ô điểm (không auto-scale)
    private static final int SCORE_FONT_SIZE = 160; // tăng font size cố định cho ô điểm
    private static final int SCORE_CELL_W = 250; // độ rộng cố định mỗi ô điểm (phù hợp 1-2 chữ số)
    // Tệp icon cầu (nếu có). Ưu tiên tìm trong classpath /images/shuttlecock.png,
    // nếu không có sẽ thử đường dẫn tương đối trong project.
    private static final String SERVE_ICON_CLASSPATH = "/icons/shuttlecock.png";
    private static final String SERVE_ICON_FALLBACK_FILE = "screenshots/shuttlecock.png";
    private static final int SERVE_ICON_SIZE = 150; // tăng kích thước icon giao cầu

    public BadmintonDisplayHorizontalFrame(BadmintonMatch match) {
        super("Badminton Scoreboard - Horizontal");
        this.match = match;

        // Khởi tạo mảng điểm và khôi phục điểm của các ván đã hoàn thành (tối đa BO3)
        for (int[] gameScore1 : gameScore) {
            gameScore1[0] = -1;
            gameScore1[1] = -1;
        }

        // Khôi phục điểm của các ván đã hoàn thành từ trạng thái hiện tại
        BadmintonMatch.Snapshot currentState = match.snapshot();
        if (currentState.gameScores != null) {
            for (int i = 0; i < currentState.gameScores.length && i < MAX_GAMES; i++) {
                if (currentState.gameScores[i][0] >= 0 && currentState.gameScores[i][1] >= 0) {
                    gameScore[i][0] = currentState.gameScores[i][0];
                    gameScore[i][1] = currentState.gameScores[i][1];
                }
            }
        }

        Font infoFont = new Font("SansSerif", Font.BOLD, FONT_SIZE);
        Font nameFont = new Font("SansSerif", Font.BOLD, 52); // Tên cố định, không auto-scale

        // Bố cục tổng: Header (NORTH) + nội dung 2 hàng (CENTER)
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG);

        // Dòng 1: Header
        matchInfo.setFont(infoFont);
        matchInfo.setForeground(FG);
        matchInfo.setBackground(BG);
        matchInfo.setOpaque(true);
        matchInfo.setBorder(new EmptyBorder(6, 16, 6, 16)); // giảm padding để header thấp hơn
        matchInfo.setPreferredSize(new Dimension(1, 64)); // giảm chiều cao phần top
        add(matchInfo, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        centerPanel.setBackground(BG);

        // Helper tạo ô điểm (không border bao quanh)
        java.util.function.Supplier<JLabel> makeCell = () -> {
            JLabel cell = new JLabel("-", SwingConstants.CENTER);
            cell.setForeground(FG);
            cell.setBackground(BG_CELL);
            cell.setOpaque(true);
            // đặt font cố định cho ô điểm
            cell.setFont(new Font("SansSerif", Font.BOLD, SCORE_FONT_SIZE));
            // đặt kích thước cố định cho bề ngang mỗi ô điểm
            Dimension pref = new Dimension(SCORE_CELL_W, CELL_H);
            cell.setPreferredSize(pref);
            cell.setMinimumSize(pref);
            cell.setMaximumSize(new Dimension(SCORE_CELL_W, Integer.MAX_VALUE));
            return cell;
        };

        // Dòng 2: Team A
        JPanel row2 = new JPanel(new BorderLayout(0, 0));
        row2.setBackground(BG);
        playerA.setFont(nameFont);
        playerA.setForeground(FG);
        playerA.setBackground(BG);
        playerA.setOpaque(true);
        playerA.setPreferredSize(new Dimension(NAME_COL_W, CELL_H));
        gamesBarA.setBackground(BG);
        for (int i = 0; i < MAX_GAMES; i++) {
            gameCellsA[i] = makeCell.get();
            applyMiddleSeparator(gameCellsA, i, SEP_V_COLOR);
            gamesBarA.add(gameCellsA[i]);
        }
        // Panel ô điểm dùng tổng độ rộng cố định để BorderLayout EAST bám theo
        Dimension barSize = new Dimension(SCORE_CELL_W * MAX_GAMES, CELL_H);
        gamesBarA.setPreferredSize(barSize);
        gamesBarA.setMinimumSize(barSize);
        gamesBarA.setMaximumSize(new Dimension(SCORE_CELL_W * MAX_GAMES, Integer.MAX_VALUE));
        // Cấu hình ô chỉ báo giao cầu A
        serveIndicatorA.setOpaque(true);
        serveIndicatorA.setBackground(BG);
        serveIndicatorA.setForeground(Color.YELLOW);
        serveIndicatorA.setFont(new Font("SansSerif", Font.PLAIN, 84));
        Dimension serveSize = new Dimension(120, CELL_H);
        serveIndicatorA.setPreferredSize(serveSize);
        serveIndicatorA.setMinimumSize(serveSize);
        serveIndicatorA.setMaximumSize(new Dimension(serveSize.width, Integer.MAX_VALUE));
        assignServeIcon(serveIndicatorA);
        serveIndicatorA.setVisible(false);

        // Gói phần bên phải: [serveIndicator] [gamesBar]
        JPanel rightA = new JPanel(new BorderLayout(8, 0));
        rightA.setBackground(BG);
        rightA.add(serveIndicatorA, BorderLayout.WEST);
        rightA.add(gamesBarA, BorderLayout.EAST);

        row2.add(playerA, BorderLayout.WEST);
        row2.add(rightA, BorderLayout.EAST);
        centerPanel.add(row2);

        // Dòng 3: Team B (có đường kẻ NGANG phía trên để chia 2 phần)
        JPanel row3 = new JPanel(new BorderLayout(0, 0));
        row3.setBackground(BG);
        row3.setBorder(BorderFactory.createMatteBorder(SEP_H_THICK, 0, 0, 0, SEP_H_COLOR));

        playerB.setFont(nameFont);
        playerB.setForeground(FG);
        playerB.setBackground(BG);
        playerB.setOpaque(true);
        playerB.setPreferredSize(new Dimension(NAME_COL_W, CELL_H));
        gamesBarB.setBackground(BG);
        for (int i = 0; i < MAX_GAMES; i++) {
            gameCellsB[i] = makeCell.get();
            applyMiddleSeparator(gameCellsB, i, SEP_V_COLOR);
            gamesBarB.add(gameCellsB[i]);
        }
        gamesBarB.setPreferredSize(barSize);
        gamesBarB.setMinimumSize(barSize);
        gamesBarB.setMaximumSize(new Dimension(SCORE_CELL_W * MAX_GAMES, Integer.MAX_VALUE));
        // Cấu hình ô chỉ báo giao cầu B
        serveIndicatorB.setOpaque(true);
        serveIndicatorB.setBackground(BG);
        serveIndicatorB.setForeground(Color.YELLOW);
        serveIndicatorB.setFont(new Font("SansSerif", Font.PLAIN, 84));
        serveIndicatorB.setPreferredSize(serveSize);
        serveIndicatorB.setMinimumSize(serveSize);
        serveIndicatorB.setMaximumSize(new Dimension(serveSize.width, Integer.MAX_VALUE));
        assignServeIcon(serveIndicatorB);
        serveIndicatorB.setVisible(false);

        JPanel rightB = new JPanel(new BorderLayout(8, 0));
        rightB.setBackground(BG);
        rightB.add(serveIndicatorB, BorderLayout.WEST);
        rightB.add(gamesBarB, BorderLayout.EAST);

        row3.add(playerB, BorderLayout.WEST);
        row3.add(rightB, BorderLayout.EAST);
        centerPanel.add(row3);
        add(centerPanel, BorderLayout.CENTER);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        // Đóng riêng cửa sổ này, không ảnh hưởng các bảng điểm khác
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true); // Luôn hiển thị trên cùng

        refresh();
        // Register listener after construction to avoid 'this' escaping during constructor
        SwingUtilities.invokeLater(() -> this.match.addPropertyChangeListener(this));

        // Đảm bảo lần đầu có kích thước, re-fit text sau khi render
        SwingUtilities.invokeLater(this::refresh);
    }

    /** Giữ để tương thích với code cũ (không dùng nữa). */
    public void setPartners(String a2, String b2) {
        this.partnerA = a2 == null ? "" : a2.trim();
        this.partnerB = b2 == null ? "" : b2.trim();
        refresh();
    }

    public void detach() {
        try {
            match.removePropertyChangeListener(this);
        } catch (Exception ignore) {
        }
        if (syncTimer != null) {
            try {
                syncTimer.cancel();
            } catch (Exception ignore) {
            }
            syncTimer = null;
        }
    }

    public void setHeader(String text) {
        this.headerText = (text == null || text.isBlank()) ? "TRẬN ĐẤU" : text.trim();
        refresh();
    }

    /* ===================== Rendering ===================== */

    public final void refresh() {
        if (gameCellsA[0] == null || gameCellsB[0] == null)
            return;

        // Luôn lấy snapshot mới nhất để đảm bảo tên và điểm được cập nhật đúng
        // Đặc biệt quan trọng khi swap ends
        BadmintonMatch.Snapshot s = match.snapshot();

        matchInfo.setText(String.format(
                "%s  •  Ván %d / BO%d  •  Ván %d - %d",
                headerText, s.gameNumber, s.bestOf, s.games[0], s.games[1]));

        // === Cải thiện xử lý tên cho ĐÁNH ĐÔI ===
        String aNameRaw = (s.names[0] == null) ? "" : s.names[0];
        String bNameRaw = (s.names[1] == null) ? "" : s.names[1];

        String aName, bName;
        if (s.doubles) {
            // Doubles: improved name processing for better display
            aName = processDoublesName(aNameRaw);
            bName = processDoublesName(bNameRaw);
        } else {
            // Singles: keep names as is
            aName = aNameRaw;
            bName = bNameRaw;
        }

        // Tự động suy ra tên CLB từ chuỗi tên nếu UI chưa set clubs
        String clubA = (s.clubs != null && s.clubs.length > 0) ? safeStr(s.clubs[0]) : "";
        String clubB = (s.clubs != null && s.clubs.length > 1) ? safeStr(s.clubs[1]) : "";
        if (clubA.isBlank())
            clubA = aggregateClubFromRawName(aNameRaw);
        if (clubB.isBlank())
            clubB = aggregateClubFromRawName(bNameRaw);

        // Render: improved two-line display for doubles
        // Debug: in case user still doesn't see club line
        // System.out.println("CLUB A='" + clubA + "' | CLUB B='" + clubB + "'");

        if (s.doubles) {
            // Đôi: hai dòng tên + 1 dòng CLB (nếu có)
            System.out.println("Doubles mode: A='" + aName + "' | B='" + bName + "'");
            System.out.println("clbubA='" + clubA + "' | clubB='" + clubB + "'");
            playerA.setText(createTwoLineDisplay(aName, clubA, true));
            playerB.setText(createTwoLineDisplay(bName, clubB, true));
        } else {
            // Đơn: 1 dòng tên + 1 dòng CLB (nếu có)
            playerA.setText(createSingleDisplay(aName, clubA, true));
            playerB.setText(createSingleDisplay(bName, clubB, true));
        }

        // Bỏ khung vàng ở ô tên, chỉ dùng biểu tượng giao cầu
        playerA.setForeground(Color.WHITE);
        playerB.setForeground(Color.WHITE);
        playerA.setBorder(new EmptyBorder(4, 8, 4, 8));
        playerB.setBorder(new EmptyBorder(4, 8, 4, 8));
        if (!s.matchFinished && !s.betweenGamesInterval) {
            if (s.server == 0) {
                serveIndicatorA.setVisible(true);
                serveIndicatorB.setVisible(false);
            } else {
                serveIndicatorA.setVisible(false);
                serveIndicatorB.setVisible(true);
            }
        } else {
            serveIndicatorA.setVisible(false);
            serveIndicatorB.setVisible(false);
        }

        // Ghi nhận điểm game đã xong khi nghỉ giữa game
        if (s.betweenGamesInterval) {
            int gi = s.gameNumber - 1;
            if (gi >= 0 && gi < gameScore.length && gameScore[gi][0] < 0 && gameScore[gi][1] < 0) {
                gameScore[gi][0] = s.score[0];
                gameScore[gi][1] = s.score[1];
            }
        }

        // Cập nhật 3 ô game (dính nhau + chỉ có đường kẻ giữa)
        for (int i = 0; i < MAX_GAMES; i++) {
            boolean active = (i < Math.min(s.bestOf, MAX_GAMES));
            gameCellsA[i].setVisible(active);
            gameCellsB[i].setVisible(active);
            if (!active)
                continue;

            // nền & chữ mặc định
            gameCellsA[i].setBackground(BG_CELL);
            gameCellsB[i].setBackground(BG_CELL);
            gameCellsA[i].setForeground(Color.WHITE);
            gameCellsB[i].setForeground(Color.WHITE);

            // LUÔN đảm bảo chỉ có "đường kẻ giữa", không viền bao
            applyMiddleSeparator(gameCellsA, i, SEP_V_COLOR);
            applyMiddleSeparator(gameCellsB, i, SEP_V_COLOR);

            if (i < s.gameNumber - 1) {
                if (gameScore[i][0] >= 0) {
                    setCellTextFit(gameCellsA[i], Integer.toString(gameScore[i][0]), false);
                    setCellTextFit(gameCellsB[i], Integer.toString(gameScore[i][1]), false);
                    gameCellsA[i].setForeground(new Color(120, 255, 120));
                    gameCellsB[i].setForeground(new Color(120, 255, 120));
                } else {
                    setCellTextFit(gameCellsA[i], "-", false);
                    setCellTextFit(gameCellsB[i], "-", false);
                }
            } else if (i == s.gameNumber - 1) {
                // Ô đang chơi: nhấn bằng màu nền + chữ, KHÔNG vẽ viền ngoài
                setCellTextFit(gameCellsA[i], Integer.toString(s.score[0]), true);
                setCellTextFit(gameCellsB[i], Integer.toString(s.score[1]), true);
                gameCellsA[i].setForeground(Color.YELLOW);
                gameCellsB[i].setForeground(Color.YELLOW);
                gameCellsA[i].setBackground(new Color(45, 45, 0));
                gameCellsB[i].setBackground(new Color(45, 45, 0));
            } else {
                setCellTextFit(gameCellsA[i], "-", false);
                setCellTextFit(gameCellsB[i], "-", false);
                gameCellsA[i].setForeground(new Color(150, 150, 150));
                gameCellsB[i].setForeground(new Color(150, 150, 150));
            }
        }

        gamesBarA.revalidate();
        gamesBarA.repaint();
        gamesBarB.revalidate();
        gamesBarB.repaint();
        repaint();

        // Không cần refit font nữa vì đã cố định kích thước
    }

    // Gán icon hình cầu nếu tìm thấy, nếu không thì dùng emoji như fallback.
    private void assignServeIcon(JLabel label) {
        ImageIcon icon = loadServeIcon(SERVE_ICON_SIZE);
        if (icon != null) {
            label.setIcon(icon);
            label.setText("");
        } else {
            label.setText("🏸");
        }
    }

    // Tải icon từ classpath hoặc file fallback. Trả về null nếu không có.
    private ImageIcon loadServeIcon(int size) {
        try {
            java.net.URL url = getClass().getResource(SERVE_ICON_CLASSPATH);
            if (url != null) {
                ImageIcon raw = new ImageIcon(url);
                Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignore) {
        }
        try {
            java.io.File f = new java.io.File(SERVE_ICON_FALLBACK_FILE);
            if (f.exists()) {
                ImageIcon raw = new ImageIcon(f.getAbsolutePath());
                Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    // Kẻ đường phân cách dọc ở giữa (ô đầu không có đường kẻ)
    private void applyMiddleSeparator(JLabel[] arr, int index, Color sep) {
        int left = (index == 0) ? 0 : 2; // chỉ kẻ ở cạnh trái từ ô thứ 2 trở đi
        arr[index].setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, left, 0, 0, sep),
                new EmptyBorder(CELL_PADDING.getBorderInsets())));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Đảm bảo refresh khi có bất kỳ thay đổi nào từ match
        // Đặc biệt quan trọng khi swap ends để cập nhật tên
        SwingUtilities.invokeLater(this::refresh);
    }

    /* ===================== Auto-fit Font cho ô điểm ===================== */

    private void setCellTextFit(JLabel label, String text, boolean highlight) {
        // Đặt text với font size cố định
        label.setText(text);
        // Sử dụng tham số highlight để tránh cảnh báo "variable never read"
        // và để có thể nhấn mạnh ô đang chơi (thay đổi style chữ).
        try {
            if (highlight) {
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            } else {
                label.setFont(label.getFont().deriveFont(Font.PLAIN));
            }
        } catch (Exception ignore) {
            // Nếu deriveFont không thành công thì bỏ qua, text đã được cập nhật.
        }
    }

    /**
     * Xử lý tên đội đánh đôi để hiển thị tối ưu
     * Giữ nguyên dấu tiếng Việt, chỉ xóa ký tự không cần thiết
     */
    private String processDoublesName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "";
        }

        // Xóa dấu ngoặc và nội dung bên trong
        String cleaned = rawName.replaceAll("\\s*\\([^)]*\\)", "");

        // Xóa các ký tự đặc biệt không cần thiết (giữ nguyên dấu tiếng Việt)
        cleaned = cleaned.replaceAll("[\\[\\]{}]", "");

        // Xóa các ký tự đặc biệt khác nhưng giữ nguyên dấu tiếng Việt
        // Giữ lại: chữ cái, số, dấu cách, dấu gạch ngang, dấu tiếng Việt
        cleaned = cleaned.replaceAll(
                "[^\\w\\s\\-ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂẾưăạảấầẩẫậắằẳẵặẹẻẽềềểếỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ]",
                "");

        // Chuẩn hóa khoảng trắng
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * Tạo hiển thị 2 dòng thông minh cho tên đội
     * Mỗi tên VĐV sẽ hiển thị trên 1 hàng riêng biệt
     * Tự động điều chỉnh font size và thêm hiệu ứng marquee khi cần
     */
    private String createTwoLineDisplay(String name, String club, boolean uppercase) {
        if (name == null || name.isBlank()) {
            return "";
        }

        String processed = uppercase ? name.toUpperCase() : name;

        // Tìm dấu gạch ngang để tách tên 2 VĐV
        int dashIndex = processed.indexOf('-');
        if (dashIndex > 0 && dashIndex < processed.length() - 1) {
            String firstPlayer = processed.substring(0, dashIndex).trim();
            String secondPlayer = processed.substring(dashIndex + 1).trim();

            // Đảm bảo cả 2 tên VĐV đều có nội dung
            if (!firstPlayer.isEmpty() && !secondPlayer.isEmpty()) {
                String firstLine = "<span style='font-size: 34px; font-weight: bold;'>"
                        + htmlPreserveSpaces(firstPlayer) + "</span>";
                String secondLine = "<span style='font-size: 34px; font-weight: bold;'>"
                        + htmlPreserveSpaces(secondPlayer) + "</span>";
                int wrapWidth = NAME_COL_W - 20;
                // Dòng 1: CLB (nếu trống thì vẫn chừa hàng cho cân đối)
                String clubContent = (club != null && !club.isBlank()) ? htmlPreserveSpaces(club.trim()) : "&nbsp;";
                // CLB: in đậm và to hơn tên VĐV
                String clubLine = "<span style='color: #FFFFFF; font-size: 60px; font-weight: bold;'>" +
                        clubContent + "</span>";
                // Trả về 3 hàng: 1) CLB 2) VĐV1 3) VĐV2
                return "<html><div style='text-align: left; line-height: 1.15; width: " + wrapWidth + "px;'>" +
                        clubLine + "<br>" + firstLine + "<br>" + secondLine + "</div></html>";
            }
        }

        // Nếu không tách được, hiển thị như đơn với wrap
        return createSingleDisplay(processed, club, false);
    }

    // Hiển thị tên đơn với cỡ chữ cố định và tự wrap nếu dài
    private String createSingleDisplay(String text, String club, boolean uppercase) {
        String content = (text == null) ? "" : (uppercase ? text.toUpperCase() : text);
        int wrapWidth = NAME_COL_W - 20;
        // 3 hàng: 1) CLB 2) Tên VĐV 3) hàng trống để cân layout với đôi
        String clubContent = (club != null && !club.isBlank()) ? htmlPreserveSpaces(club.trim()) : "&nbsp;";
        // CLB: in đậm và to hơn tên VĐV
        String clubLine = "<span style='color: #FFFFFF; font-size: 40px; font-weight: bold;'>" + clubContent
                + "</span>";
        String nameLine = "<span style='font-size: 34px; font-weight: bold;'>" + htmlPreserveSpaces(content)
                + "</span>";
        String emptyLine = "<span style='font-size: 34px; font-weight: bold;'>&nbsp;</span>";
        return "<html><div style='text-align: left; line-height: 1.15; width: " + wrapWidth + "px;'>" +
                clubLine + "<br>" + nameLine + "<br>" + emptyLine + "</div></html>";
    }

    /**
     * Escape HTML và giữ nguyên khoảng trắng, dấu tiếng Việt
     */
    private String htmlPreserveSpaces(String s) {
        if (s == null)
            return "";

        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case ' ' -> sb.append("&nbsp;");
                case '-' -> sb.append("&#8209;"); // Non-breaking hyphen
                default -> sb.append(ch); // Giữ nguyên tất cả ký tự khác, bao gồm dấu tiếng Việt
            }
        }
        return sb.toString();
    }

    // ====== Helpers: Suy ra tên CLB từ chuỗi tên gốc (tìm nội dung trong ngoặc)
    // ======
    private String aggregateClubFromRawName(String raw) {
        if (raw == null || raw.isBlank())
            return "";
        // Tìm tất cả nội dung trong ngoặc tròn, gom lại theo thứ tự xuất hiện, loại
        // trùng
        Pattern p = Pattern.compile("\\(([^)]{1,50})\\)");
        Matcher m = p.matcher(raw);
        Set<String> unique = new LinkedHashSet<>();
        while (m.find()) {
            String c = m.group(1).trim();
            if (!c.isEmpty())
                unique.add(c);
        }
        if (unique.isEmpty())
            return "";
        // Nếu có nhiều hơn 1, nối bằng "/"
        StringBuilder sb = new StringBuilder();
        for (String c : unique) {
            if (sb.length() > 0)
                sb.append(" / ");
            sb.append(c);
        }
        return sb.toString();
    }

    private String safeStr(String s) {
        return s == null ? "" : s.trim();
    }
}
