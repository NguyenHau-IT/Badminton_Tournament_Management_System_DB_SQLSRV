package com.example.btms.ui.scoreboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.example.btms.model.match.BadmintonMatch;

public class BadmintonDisplayFrame extends JFrame implements PropertyChangeListener {
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

                    // Đọc JSON điểm số
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
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        match.setScore(finalA, finalB);
                    });
                } catch (Exception e) {
                    // Có thể log lỗi nếu cần
                }
            }
        }, 0, 1000); // mỗi 1 giây
    }

    private final BadmintonMatch match;

    private final JLabel nameA = new JLabel("TEAM A", SwingConstants.CENTER);
    private final JLabel nameB = new JLabel("TEAM B", SwingConstants.CENTER);
    private final JLabel scoreA = new JLabel("0", SwingConstants.CENTER);
    private final JLabel scoreB = new JLabel("0", SwingConstants.CENTER);
    private final JLabel gamesA = new JLabel("0", SwingConstants.CENTER);
    private final JLabel gamesB = new JLabel("0", SwingConstants.CENTER);
    private final JLabel gameInfo = new JLabel("Game 1", SwingConstants.CENTER);
    private final JLabel serveA = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel serveB = new JLabel(" ", SwingConstants.CENTER);

    public BadmintonDisplayFrame(BadmintonMatch match) {
        super("Badminton Scoreboard");
        this.match = match;
        this.match.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(1, 3));
        top.add(new JLabel(" "));
        top.add(gameInfo);
        top.add(new JLabel(" "));
        add(top, BorderLayout.NORTH);

        Font big = new Font("SansSerif", Font.BOLD, 120);
        Font mid = new Font("SansSerif", Font.BOLD, 36);

        nameA.setFont(mid);
        nameB.setFont(mid);
        scoreA.setFont(big);
        scoreB.setFont(big);
        gamesA.setFont(mid);
        gamesB.setFont(mid);
        gameInfo.setFont(mid);
        serveA.setFont(mid);
        serveB.setFont(mid);

        JPanel center = new JPanel(new GridLayout(2, 3, 20, 10));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.add(nameA);
        center.add(new JLabel(" "));
        center.add(nameB);

        JPanel left = new JPanel(new BorderLayout());
        left.add(scoreA, BorderLayout.CENTER);
        left.add(serveA, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout());
        right.add(scoreB, BorderLayout.CENTER);
        right.add(serveB, BorderLayout.SOUTH);

        center.add(left);
        center.add(new JLabel(" "));
        center.add(right);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 5));
        bottom.add(new JLabel("Games", SwingConstants.RIGHT));
        bottom.add(gamesA);
        bottom.add(new JLabel("-", SwingConstants.CENTER));
        bottom.add(gamesB);
        bottom.add(new JLabel(" ", SwingConstants.LEFT));
        add(bottom, BorderLayout.SOUTH);

        // Theme tối
        Color bg = Color.BLACK;
        Color fg = Color.WHITE;
        getContentPane().setBackground(bg);
        top.setBackground(bg);
        center.setBackground(bg);
        bottom.setBackground(bg);

        JLabel[] allLabels = { nameA, nameB, scoreA, scoreB, gamesA, gamesB,
                gameInfo, serveA, serveB };
        for (JLabel lbl : allLabels) {
            lbl.setForeground(fg);
            lbl.setBackground(bg);
            lbl.setOpaque(true); // quan trọng để nền label có màu
        }
        serveA.setForeground(Color.YELLOW);
        serveB.setForeground(Color.YELLOW);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở rộng toàn màn hình khi chạy
        setLocationRelativeTo(null);
        setAlwaysOnTop(true); // Luôn hiển thị trên cùng
        refresh();
    }

    public void refresh() {
        BadmintonMatch.Snapshot s = match.snapshot();

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

        // Hiển thị tên với xử lý thông minh
        if (s.doubles) {
            // Hiển thị mỗi tên VĐV 1 hàng riêng biệt
            nameA.setText(createTwoLineDisplay(aName, true));
            nameB.setText(createTwoLineDisplay(bName, true));
        } else {
            nameA.setText(aName.toUpperCase());
            nameB.setText(bName.toUpperCase());
        }

        scoreA.setText(Integer.toString(s.score[0]));
        scoreB.setText(Integer.toString(s.score[1]));
        gamesA.setText(Integer.toString(s.games[0]));
        gamesB.setText(Integer.toString(s.games[1]));
        gameInfo.setText("GAME " + s.gameNumber + " / BO" + s.bestOf);

        // Cải thiện hiển thị giao cầu
        serveA.setText(s.server == 0 ? "● " + (s.score[0] % 2 == 0 ? "R" : "L") : " ");
        serveB.setText(s.server == 1 ? "● " + (s.score[1] % 2 == 0 ? "R" : "L") : " ");

        // Highlight đội đang giao cầu
        if (!s.matchFinished && !s.betweenGamesInterval) {
            if (s.server == 0) {
                nameA.setForeground(Color.YELLOW);
                serveA.setForeground(Color.YELLOW);
            } else {
                nameB.setForeground(Color.YELLOW);
                serveB.setForeground(Color.YELLOW);
            }
        } else {
            nameA.setForeground(Color.WHITE);
            nameB.setForeground(Color.WHITE);
            serveA.setForeground(Color.YELLOW);
            serveB.setForeground(Color.YELLOW);
        }

        repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refresh();
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

                return "<html><div style='text-align: center; line-height: 1.2;'>" +
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

                return "<html><div style='text-align: center; line-height: 1.2;'>" +
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
        if (text.length() <= 15) {
            // Tên ngắn: font size bình thường
            return "<span style='font-size: 32px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else if (text.length() <= 22) {
            // Tên vừa: giảm font size
            return "<span style='font-size: 26px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else if (text.length() <= 28) {
            // Tên dài: font size nhỏ hơn
            return "<span style='font-size: 22px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else if (text.length() <= 35) {
            // Tên rất dài: font size nhỏ nhất
            return "<span style='font-size: 18px; font-weight: bold;'>" + htmlPreserveSpaces(text) + "</span>";
        } else {
            // Tên cực dài: hiệu ứng chữ chạy với font nhỏ
            return "<marquee behavior='scroll' direction='left' scrollamount='3' width='480' style='font-size: 16px; font-weight: bold;'>"
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
     * Hỗ trợ HTML để giữ nguyên khoảng trắng trong marquee
     */
    private String htmlPreserveSpaces(String text) {
        return text.replace(" ", "&nbsp;");
    }
}
