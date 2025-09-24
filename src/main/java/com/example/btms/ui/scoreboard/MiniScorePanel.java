package com.example.btms.ui.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.example.btms.model.match.BadmintonMatch;
import static com.example.btms.util.text.UiTextUtil.processDoublesName;

public class MiniScorePanel extends JPanel implements PropertyChangeListener {
    private final BadmintonMatch match;

    private final JLabel matchInfo = new JLabel("TRẬN ĐẤU", SwingConstants.LEFT);

    // Mỗi đội: 2 label tên cho đánh đôi (1 VĐV/label)
    private final JLabel playerA1 = new JLabel("TEAM A", SwingConstants.LEFT);
    private final JLabel playerA2 = new JLabel("", SwingConstants.LEFT);
    private final JLabel playerB1 = new JLabel("TEAM B", SwingConstants.LEFT);
    private final JLabel playerB2 = new JLabel("", SwingConstants.LEFT);

    // Dãy ô game
    private final JPanel gamesBarA = new JPanel(new GridLayout(1, 3, 8, 0)); // Thay đổi từ 5 xuống 3
    private final JPanel gamesBarB = new JPanel(new GridLayout(1, 3, 8, 0)); // Thay đổi từ 5 xuống 3
    private final JLabel[] gameCellsA = new JLabel[3]; // Thay đổi từ 5 xuống 3
    private final JLabel[] gameCellsB = new JLabel[3]; // Thay đổi từ 5 xuống 3

    // Lưu tỉ số game đã xong (A,B); -1 là chưa có
    private final int[][] gameScore = new int[3][2]; // Thay đổi từ 5 xuống 3

    // ---- Style ----
    private static final int CELL_W = 55; // Giảm từ 70 xuống 55
    private static final int CELL_H = 40;
    private static final int NAME_COL_W = 320; // Tăng từ 280 lên 320 để phù hợp với max-width

    private static final int FONT_SIZE_HEADER = 28;
    private static final int FONT_SIZE_NAME = 20;
    private static final int FONT_SIZE_CELL = 25;

    private static final EmptyBorder PADDING_NORMAL = new EmptyBorder(6, 12, 6, 12);
    private static final EmptyBorder PADDING_LIVE = new EmptyBorder(4, 10, 4, 10);
    private static final LineBorder HILITE_LINE = new LineBorder(Color.YELLOW, 2, true);

    private String headerText = "TRẬN ĐẤU";

    public MiniScorePanel(BadmintonMatch match) {
        this.match = match;
        this.match.addPropertyChangeListener(this);

        setBackground(Color.BLACK);
        setBorder(new EmptyBorder(6, 8, 6, 8));
        setLayout(new BorderLayout(0, 6));

        // Header
        matchInfo.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE_HEADER));
        matchInfo.setForeground(Color.WHITE);
        matchInfo.setBackground(Color.BLACK);
        matchInfo.setOpaque(true);
        matchInfo.setBorder(new EmptyBorder(4, 8, 4, 8));
        add(matchInfo, BorderLayout.NORTH);

        // Trung tâm gồm 2 hàng: A & B
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        centerPanel.setBackground(Color.BLACK);
        add(centerPanel, BorderLayout.CENTER);

        // Helper tạo ô điểm
        java.util.function.Supplier<JLabel> makeCell = () -> {
            JLabel cell = new JLabel("-", SwingConstants.CENTER);
            cell.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE_CELL));
            applyNormal(cell);
            return cell;
        };

        // ===== Hàng A =====
        JPanel rowA = new JPanel(new BorderLayout(10, 0));
        rowA.setBackground(Color.BLACK);
        JPanel nameWrapA = buildNameWrap(playerA1, playerA2);
        rowA.add(nameWrapA, BorderLayout.WEST);

        gamesBarA.setBackground(Color.BLACK);
        for (int i = 0; i < 3; i++) {
            gameCellsA[i] = makeCell.get();
            gamesBarA.add(gameCellsA[i]);
        }
        rowA.add(gamesBarA, BorderLayout.CENTER);
        centerPanel.add(rowA);

        // ===== Hàng B =====
        JPanel rowB = new JPanel(new BorderLayout(10, 0));
        rowB.setBackground(Color.BLACK);
        JPanel nameWrapB = buildNameWrap(playerB1, playerB2);
        rowB.add(nameWrapB, BorderLayout.WEST);

        gamesBarB.setBackground(Color.BLACK);
        for (int i = 0; i < 3; i++) {
            gameCellsB[i] = makeCell.get();
            gamesBarB.add(gameCellsB[i]);
        }
        rowB.add(gamesBarB, BorderLayout.CENTER);
        centerPanel.add(rowB);

        // Khởi tạo cache tỉ số
        for (int i = 0; i < 3; i++) {
            gameScore[i][0] = -1;
            gameScore[i][1] = -1;
        }

        refresh();
    }

    /** Khung tên 2 dòng cho đánh đôi (JLabel) */
    private JPanel buildNameWrap(JLabel main1, JLabel main2) {
        // Set font cố định cho cả 2 label
        main1.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE_NAME));
        main2.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE_NAME));
        main1.setForeground(Color.WHITE);
        main2.setForeground(Color.WHITE);
        main1.setBackground(Color.BLACK);
        main2.setBackground(Color.BLACK);
        main1.setOpaque(true);
        main2.setOpaque(true);

        JPanel names = new JPanel(new GridLayout(2, 1, 0, 2));
        names.setBackground(Color.BLACK);
        names.add(main1);
        names.add(main2);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.BLACK);
        wrap.add(names, BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(NAME_COL_W, CELL_H));
        return wrap;
    }

    private static void lockSize(JLabel c) {
        Dimension d = new Dimension(CELL_W, CELL_H);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }

    private void applyNormal(JLabel cell) {
        cell.setForeground(Color.WHITE);
        cell.setBackground(new Color(24, 24, 24));
        cell.setBorder(PADDING_NORMAL);
        cell.setOpaque(true);
        lockSize(cell);
    }

    private void applyLive(JLabel cell) {
        cell.setForeground(Color.YELLOW);
        cell.setBackground(new Color(45, 45, 0));
        cell.setBorder(BorderFactory.createCompoundBorder(HILITE_LINE, PADDING_LIVE));
        cell.setOpaque(true);
        lockSize(cell);
    }

    public void refresh() {
        BadmintonMatch.Snapshot s = match.snapshot();
        matchInfo.setText(headerText);

        // Debug: Log tên VĐV để kiểm tra
        System.out.println("MiniScorePanel refresh - Names: A='" + s.names[0] + "', B='" + s.names[1] + "'");

        // Cập nhật mảng gameScore từ snapshot để đảm bảo đồng bộ với match
        System.out.println("Snapshot gameScores length: " + s.gameScores.length);
        for (int i = 0; i < s.gameScores.length && i < gameScore.length; i++) {
            System.out.println("Snapshot gameScores[" + i + "]: A=" + s.gameScores[i][0] + ", B=" + s.gameScores[i][1]);
            if (s.gameScores[i][0] >= 0 && s.gameScores[i][1] >= 0) {
                gameScore[i][0] = s.gameScores[i][0];
                gameScore[i][1] = s.gameScores[i][1];
                System.out.println("Updated gameScore[" + i + "]: A=" + gameScore[i][0] + ", B=" + gameScore[i][1]);
            }
        }

        boolean serveA = !s.matchFinished && !s.betweenGamesInterval && s.server == 0;
        boolean serveB = !s.matchFinished && !s.betweenGamesInterval && s.server == 1;

        // === Cải thiện xử lý tên cho ĐÁNH ĐÔI ===
        String aNameRaw = (s.names[0] == null) ? "" : s.names[0];
        String bNameRaw = (s.names[1] == null) ? "" : s.names[1];

        // Assign names based on singles/doubles with improved processing
        String aName;
        String bName;

        if (s.doubles) {
            // Doubles: improved name processing for better display
            aName = processDoublesName(aNameRaw);
            bName = processDoublesName(bNameRaw);
        } else {
            // Singles: keep names as is but still process for safety
            aName = processDoublesName(aNameRaw);
            bName = processDoublesName(bNameRaw);
        }

        // Render: sử dụng 2 JLabel cho đánh đôi
        if (s.doubles) {
            // Doubles: hiển thị tên đã xử lý với 2 dòng
            String processedA = processDoublesName(aName);
            String processedB = processDoublesName(bName);

            // Tách tên theo dấu "-"
            String[] namesA = splitPlayerNames(processedA);
            String[] namesB = splitPlayerNames(processedB);

            playerA1.setText(namesA[0]);
            playerA2.setText(namesA[1]);
            playerB1.setText(namesB[0]);
            playerB2.setText(namesB[1]);
        } else {
            // Singles: hiển thị tên đã xử lý trên dòng đầu, dòng 2 để trống
            playerA1.setText(processDoublesName(aName));
            playerA2.setText("");
            playerB1.setText(processDoublesName(bName));
            playerB2.setText("");
        }

        // Highlight đội giao cầu với màu sắc cải thiện
        // nếu đánh đôi thì highlight cả 2 dòng
        if (s.doubles) {
            playerA1.setForeground(serveA ? Color.YELLOW : Color.WHITE);
            playerA2.setForeground(serveA ? Color.YELLOW : Color.WHITE);
            playerB1.setForeground(serveB ? Color.YELLOW : Color.WHITE);
            playerB2.setForeground(serveB ? Color.YELLOW : Color.WHITE);
        } else {
            playerA1.setForeground(serveA ? Color.YELLOW : Color.WHITE);
            playerB1.setForeground(serveB ? Color.YELLOW : Color.WHITE);
        }

        // Cải thiện hiển thị khi giao cầu
        // Nếu đánh đôi thì highlight cả 2 dòng

        if (serveA) {
            if (s.doubles) {
                playerA1.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
                playerA2.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
            } else {
                playerA1.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
            }
        } else {
            playerA1.setBorder(new EmptyBorder(4, 8, 4, 8));
            playerA2.setBorder(new EmptyBorder(4, 8, 4, 8));
        }

        if (serveB) {
            if (s.doubles) {
                playerB1.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
                playerB2.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
            } else {
                playerB1.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(Color.YELLOW, 2, true),
                        new EmptyBorder(4, 8, 4, 8)));
            }
        } else {
            playerB1.setBorder(new EmptyBorder(4, 8, 4, 8));
            playerB2.setBorder(new EmptyBorder(4, 8, 4, 8));
        }

        if (s.betweenGamesInterval) {
            int gi = s.gameNumber - 1;
            if (gi >= 0 && gi < gameScore.length && gameScore[gi][0] < 0 && gameScore[gi][1] < 0) {
                gameScore[gi][0] = s.score[0];
                gameScore[gi][1] = s.score[1];
            }
        }

        for (int i = 0; i < 3; i++) {
            applyNormal(gameCellsA[i]);
            applyNormal(gameCellsB[i]);

            boolean enabled = (i < s.bestOf);
            if (!enabled) {
                gameCellsA[i].setText("");
                gameCellsB[i].setText("");
                gameCellsA[i].setForeground(new Color(90, 90, 90));
                gameCellsB[i].setForeground(new Color(90, 90, 90));
                continue;
            }

            if (i < s.gameNumber - 1) {
                if (gameScore[i][0] >= 0) {
                    gameCellsA[i].setText(Integer.toString(gameScore[i][0]));
                    gameCellsB[i].setText(Integer.toString(gameScore[i][1]));
                    gameCellsA[i].setForeground(new Color(120, 255, 120));
                    gameCellsB[i].setForeground(new Color(120, 255, 120));
                } else {
                    gameCellsA[i].setText("-");
                    gameCellsB[i].setText("-");
                    gameCellsA[i].setForeground(new Color(150, 150, 150));
                    gameCellsB[i].setForeground(new Color(150, 150, 150));
                }
            } else if (i == s.gameNumber - 1) {
                gameCellsA[i].setText(Integer.toString(s.score[0]));
                gameCellsB[i].setText(Integer.toString(s.score[1]));
                applyLive(gameCellsA[i]);
                applyLive(gameCellsB[i]);
                // Debug: Log điểm ván hiện tại
                System.out.println("Current game " + s.gameNumber + " score: A=" + s.score[0] + ", B=" + s.score[1]);
            } else {
                gameCellsA[i].setText("-");
                gameCellsB[i].setText("-");
                gameCellsA[i].setForeground(new Color(150, 150, 150));
                gameCellsB[i].setForeground(new Color(150, 150, 150));
            }
        }

        revalidate();
        repaint();
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
     * Tách tên đội đánh đôi theo dấu "-"
     * Trả về mảng 2 phần tử: [tên VĐV 1, tên VĐV 2]
     */
    private String[] splitPlayerNames(String name) {
        String[] result = new String[2];

        // Tìm dấu gạch ngang để tách tên 2 VĐV
        int dashIndex = name.indexOf('-');
        if (dashIndex > 0 && dashIndex < name.length() - 1) {
            String firstPlayer = name.substring(0, dashIndex).trim();
            String secondPlayer = name.substring(dashIndex + 1).trim();

            // Đảm bảo cả 2 tên VĐV đều có nội dung
            if (!firstPlayer.isEmpty() && !secondPlayer.isEmpty()) {
                // Cắt tên quá dài
                if (firstPlayer.length() > 25) {
                    firstPlayer = firstPlayer.substring(0, 22) + "...";
                }
                if (secondPlayer.length() > 25) {
                    secondPlayer = secondPlayer.substring(0, 22) + "...";
                }

                result[0] = firstPlayer;
                result[1] = secondPlayer;
                return result;
            }
        }

        // Nếu không có dấu gạch ngang hoặc không thể tách, trả về tên gốc ở dòng đầu
        result[0] = name;
        result[1] = "";
        return result;
    }

    public void setHeader(String text) {
        this.headerText = (text == null || text.isBlank()) ? "TRẬN ĐẤU" : text.trim();
        refresh();
    }

    /**
     * Force refresh hoàn toàn, bỏ qua cache local
     */
    public void forceRefresh() {
        // Reset mảng gameScore
        for (int i = 0; i < gameScore.length; i++) {
            gameScore[i][0] = -1;
            gameScore[i][1] = -1;
        }
        refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Đảm bảo refresh được gọi trên EDT thread
        SwingUtilities.invokeLater(() -> {
            // Debug: Log sự kiện để kiểm tra
            System.out.println("MiniScorePanel received event: " + evt.getPropertyName());

            // Nếu là sự kiện swap, reset mảng gameScore để đảm bảo cập nhật đúng
            if ("swap".equals(evt.getPropertyName())) {
                System.out.println("=== MINI SCORE PANEL - SWAP EVENT DETECTED ===");
                System.out.println("Resetting gameScore array to force refresh from snapshot");
                for (int i = 0; i < gameScore.length; i++) {
                    gameScore[i][0] = -1;
                    gameScore[i][1] = -1;
                }
                System.out.println("gameScore array reset completed");
            }

            refresh();
        });
    }
}
