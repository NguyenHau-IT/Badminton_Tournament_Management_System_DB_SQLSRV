package com.example.btms.ui.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

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
                    java.io.BufferedReader in = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
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
                } catch (Exception ignored) {
                    // Có thể log lỗi nếu cần
                }
            }
        }, 0, 1000); // mỗi 1 giây
    }

    private final BadmintonMatch match;

    private final JLabel matchInfo = new JLabel("TRẬN ĐẤU", SwingConstants.CENTER);
    private final JLabel playerA = new JLabel("VĐV 1", SwingConstants.LEFT);
    private final JLabel playerB = new JLabel("VĐV 2", SwingConstants.LEFT);

    // KHÔNG có khoảng cách giữa các ô (hgap=0, vgap=0)
    private final JPanel gamesBarA = new JPanel(new GridLayout(1, 5, 0, 0));
    private final JPanel gamesBarB = new JPanel(new GridLayout(1, 5, 0, 0));
    private final JLabel[] gameCellsA = new JLabel[5];
    private final JLabel[] gameCellsB = new JLabel[5];

    private final int[][] gameScore = new int[5][2];

    private String headerText = "TRẬN ĐẤU";

    // Giữ để tương thích với code cũ, nhưng không sử dụng nữa.
    private String partnerA = "";
    private String partnerB = "";

    private static final int CELL_H = 80; // Tăng chiều cao ô điểm từ 44 lên 80
    private static final int NAME_COL_W = 600; // rộng hơn để chứa 2 dòng
    private static final int FONT_SIZE = 40;

    // Style
    private static final Color BG = Color.BLACK;
    private static final Color FG = Color.WHITE;
    private static final Color BG_CELL = new Color(24, 24, 24);
    private static final Color SEP_V_COLOR = new Color(70, 70, 70); // đường kẻ dọc giữa các ô
    private static final Color SEP_H_COLOR = new Color(70, 70, 70); // đường kẻ ngang giữa 2 phần
    private static final int SEP_H_THICK = 2; // độ dày đường kẻ ngang
    private static final EmptyBorder CELL_PADDING = new EmptyBorder(12, 24, 12, 24); // Tăng padding

    // Font cố định cho ô điểm (không auto-scale)
    private static final int SCORE_FONT_SIZE = 120; // Font size cố định cho ô điểm

    public BadmintonDisplayHorizontalFrame(BadmintonMatch match) {
        super("Badminton Scoreboard - Horizontal");
        this.match = match;

        // Khởi tạo mảng điểm và khôi phục điểm của các ván đã hoàn thành
        for (int i = 0; i < 3; i++) {
            gameScore[i][0] = -1;
            gameScore[i][1] = -1;
        }

        // Khôi phục điểm của các ván đã hoàn thành từ trạng thái hiện tại
        BadmintonMatch.Snapshot currentState = match.snapshot();
        if (currentState.gameScores != null) {
            for (int i = 0; i < currentState.gameScores.length && i < 5; i++) {
                if (currentState.gameScores[i][0] >= 0 && currentState.gameScores[i][1] >= 0) {
                    gameScore[i][0] = currentState.gameScores[i][0];
                    gameScore[i][1] = currentState.gameScores[i][1];
                }
            }
        }

        Font infoFont = new Font("SansSerif", Font.BOLD, FONT_SIZE);
        Font nameFont = new Font("SansSerif", Font.BOLD, 48); // Tăng font size tên người chơi từ 36 lên 48

        setLayout(new GridLayout(3, 1));
        getContentPane().setBackground(BG);

        // Dòng 1: Header
        matchInfo.setFont(infoFont);
        matchInfo.setForeground(FG);
        matchInfo.setBackground(BG);
        matchInfo.setOpaque(true);
        matchInfo.setBorder(new EmptyBorder(16, 24, 16, 24)); // Tăng padding header
        add(matchInfo);

        // Helper tạo ô điểm (không border bao quanh)
        java.util.function.Supplier<JLabel> makeCell = () -> {
            JLabel cell = new JLabel("-", SwingConstants.CENTER);
            cell.setForeground(FG);
            cell.setBackground(BG_CELL);
            cell.setOpaque(true);
            // đặt font cố định cho ô điểm
            cell.setFont(new Font("SansSerif", Font.BOLD, SCORE_FONT_SIZE));
            return cell;
        };

        // Dòng 2: Team A
        JPanel row2 = new JPanel(new BorderLayout(12, 0));
        row2.setBackground(BG);
        playerA.setFont(nameFont);
        playerA.setForeground(FG);
        playerA.setBackground(BG);
        playerA.setOpaque(true);
        playerA.setPreferredSize(new Dimension(NAME_COL_W, CELL_H));
        gamesBarA.setBackground(BG);
        for (int i = 0; i < 3; i++) {
            gameCellsA[i] = makeCell.get();
            applyMiddleSeparator(gameCellsA, i, SEP_V_COLOR);
            gamesBarA.add(gameCellsA[i]);
        }
        row2.add(playerA, BorderLayout.WEST);
        row2.add(gamesBarA, BorderLayout.CENTER);
        add(row2);

        // Dòng 3: Team B (có đường kẻ NGANG phía trên để chia 2 phần)
        JPanel row3 = new JPanel(new BorderLayout(12, 0));
        row3.setBackground(BG);
        row3.setBorder(BorderFactory.createMatteBorder(SEP_H_THICK, 0, 0, 0, SEP_H_COLOR));

        playerB.setFont(nameFont);
        playerB.setForeground(FG);
        playerB.setBackground(BG);
        playerB.setOpaque(true);
        playerB.setPreferredSize(new Dimension(NAME_COL_W, CELL_H));
        gamesBarB.setBackground(BG);
        for (int i = 0; i < 3; i++) {
            gameCellsB[i] = makeCell.get();
            applyMiddleSeparator(gameCellsB, i, SEP_V_COLOR);
            gamesBarB.add(gameCellsB[i]);
        }
        row3.add(playerB, BorderLayout.WEST);
        row3.add(gamesBarB, BorderLayout.CENTER);
        add(row3);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        setAlwaysOnTop(true); // Luôn hiển thị trên cùng

        refresh();
        this.match.addPropertyChangeListener(this);

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

    public void refresh() {
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

        // Render: improved two-line display for doubles
        if (s.doubles) {
            // Hiển thị mỗi tên VĐV 1 hàng riêng biệt
            playerA.setText(createTwoLineDisplay(aName, true));
            playerB.setText(createTwoLineDisplay(bName, true));
        } else {
            playerA.setText(aName);
            playerB.setText(bName);
        }

        // Màu vàng cho người giao cầu với cải thiện
        playerA.setForeground(Color.WHITE);
        playerB.setForeground(Color.WHITE);
        if (!s.matchFinished && !s.betweenGamesInterval) {
            if (s.server == 0) {
                playerA.setForeground(Color.YELLOW);
                playerA.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
            } else {
                playerB.setForeground(Color.YELLOW);
                playerB.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
            }
        } else {
            // Reset border khi không giao cầu
            playerA.setBorder(new EmptyBorder(4, 8, 4, 8));
            playerB.setBorder(new EmptyBorder(4, 8, 4, 8));
        }

        // Ghi nhận điểm game đã xong khi nghỉ giữa game
        if (s.betweenGamesInterval) {
            int gi = s.gameNumber - 1;
            if (gi >= 0 && gi < gameScore.length && gameScore[gi][0] < 0 && gameScore[gi][1] < 0) {
                gameScore[gi][0] = s.score[0];
                gameScore[gi][1] = s.score[1];
            }
        }

        // Cập nhật 5 ô game (dính nhau + chỉ có đường kẻ giữa)
        for (int i = 0; i < 5; i++) {
            boolean active = (i < s.bestOf);
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
        // Không cần fit font nữa vì đã cố định
    }

    private void refitToCurrentText(JLabel label) {
        // Không cần refit font nữa vì đã cố định
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
    private String createTwoLineDisplay(String name, boolean uppercase) {
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
                // Tự động điều chỉnh font size và thêm marquee nếu cần
                String firstLine = createPlayerDisplay(firstPlayer);
                String secondLine = createPlayerDisplay(secondPlayer);

                return "<html><div style='text-align: left; line-height: 1.2;'>" +
                        firstLine + "<br>" + secondLine + "</div></html>";
            }
        }

        // Nếu không có dấu gạch ngang, tìm dấu cách
        int spaceIndex = processed.indexOf(' ');
        if (spaceIndex > 0 && spaceIndex < processed.length() - 1) {
            String firstPlayer = processed.substring(0, spaceIndex).trim();
            String secondPlayer = processed.substring(spaceIndex + 1).trim();

            if (!firstPlayer.isEmpty() && !secondPlayer.isEmpty()) {
                // Tự động điều chỉnh font size và thêm marquee nếu cần
                String firstLine = createPlayerDisplay(firstPlayer);
                String secondLine = createPlayerDisplay(secondPlayer);

                return "<html><div style='text-align: left; line-height: 1.2;'>" +
                        firstLine + "<br>" + secondLine + "</div></html>";
            }
        }

        // Nếu không thể chia, hiển thị 1 dòng với điều chỉnh font
        return createPlayerDisplay(processed);
    }

    /**
     * Tạo hiển thị cho tên VĐV với font size tự động điều chỉnh
     * Tối ưu cho trang giám sát với hiệu ứng marquee cho tên dài
     */
    private String createPlayerDisplay(String text) {
        if (text.length() <= 12) {
            // Tên ngắn: font size bình thường
            return "<span style='font-size: 32px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else if (text.length() <= 18) {
            // Tên vừa: giảm font size
            return "<span style='font-size: 26px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else if (text.length() <= 24) {
            // Tên dài: font size nhỏ hơn
            return "<span style='font-size: 22px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else if (text.length() <= 30) {
            // Tên rất dài: font size nhỏ nhất
            return "<span style='font-size: 18px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else {
            // Tên cực dài: hiệu ứng chữ chạy với font nhỏ
            return "<marquee behavior='scroll' direction='left' scrollamount='2' width='380' style='font-size: 16px; font-weight: bold;'>"
                    +
                    htmlPreserveSpaces(text) + "</marquee>";
        }
    }

    /**
     * Tìm điểm chia tối ưu cho tên đội
     */
    private int findOptimalSplitPoint(String name) {
        if (name.length() <= 15) {
            // Tên ngắn, chia ở giữa
            return name.length() / 2;
        }

        // Tìm dấu cách đầu tiên
        int firstSpace = name.indexOf(' ');
        if (firstSpace > 0 && firstSpace < name.length() - 1) {
            return firstSpace;
        }

        // Tìm dấu gạch ngang
        int firstDash = name.indexOf('-');
        if (firstDash > 0 && firstDash < name.length() - 1) {
            return firstDash;
        }

        // Tìm điểm chia cân bằng (khoảng 60% chiều dài)
        return (int) (name.length() * 0.6);
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
}
