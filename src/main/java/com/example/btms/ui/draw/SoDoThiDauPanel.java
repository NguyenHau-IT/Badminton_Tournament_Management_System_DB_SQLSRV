package com.example.btms.ui.draw;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Trang "Sơ đồ thi đấu" hiển thị bracket loại trực tiếp 16 -> 1 (5 cột)
 * theo các công thức được yêu cầu:
 * TOA_DO_X = 35 + (COL - 1) * 160
 * TOA_DO_Y = 62 + THU_TU * (40 * 2^(COL-1))
 * VI_TRI = THU_TU + 1 (số thứ tự hiển thị)
 *
 * Cột 1: 16 chỗ (vòng 1)
 * Cột 2: 8 chỗ (tứ kết)
 * Cột 3: 4 chỗ (bán kết)
 * Cột 4: 2 chỗ (chung kết)
 * Cột 5: 1 chỗ (vô địch)
 *
 * Nguồn tên đội lấy từ bảng BOC_THAM_DOI (thứ tự bắt đầu 0) nếu có; nếu thiếu
 * thì hiển thị "Slot n".
 */
public class SoDoThiDauPanel extends JPanel {

    private final BracketCanvas canvas = new BracketCanvas();
    private final JButton btnReset = new JButton("Reset 16 đội");
    private final JButton btnShuffle = new JButton("Trộn ngẫu nhiên");
    private final JButton btnExport = new JButton("Xuất tọa độ");

    public SoDoThiDauPanel(java.sql.Connection ignoredConn) { // giữ signature cũ để MainFrame không phải đổi nhiều
        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        SwingUtilities.invokeLater(this::resetDefaultTeams);
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(new JLabel("Sơ đồ 16 đội (tĩnh)"));
        line.add(btnReset);
        line.add(btnShuffle);
        line.add(btnExport);
        p.add(line, BorderLayout.CENTER);

        btnReset.addActionListener(e -> resetDefaultTeams());
        btnShuffle.addActionListener(e -> shuffleTeams());
        btnExport.addActionListener(e -> exportCoordinatesToFile());
        return p;
    }

    /**
     * Ghi danh sách toạ độ các ô (col, index, x, y, text) ra file so_do.txt ở thư
     * mục làm việc hiện tại.
     */
    private void exportCoordinatesToFile() {
        java.io.File out = new java.io.File("so_do.txt");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(out), java.nio.charset.StandardCharsets.UTF_8))) {
            pw.println("# Danh sách toạ độ các ô bracket 16 -> 1");
            pw.println("# Định dạng: col;thu_tu;X;Y;Text");
            for (BracketCanvas.Slot s : canvas.getSlots()) {
                pw.printf("%d;%d;%d;%d;%s%n", s.col, s.thuTu, s.x, s.y, s.text == null ? "" : s.text.replace(';', ' '));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void resetDefaultTeams() {
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            names.add("Team " + i);
        }
        canvas.setParticipants(names);
        canvas.repaint();
    }

    private void shuffleTeams() {
        List<String> current = new ArrayList<>(canvas.getParticipants());
        if (current.isEmpty()) {
            resetDefaultTeams();
            return;
        }
        java.util.Collections.shuffle(current, new java.util.Random());
        canvas.setParticipants(current);
        canvas.repaint();
    }

    /* ===================== Canvas ===================== */
    private static class BracketCanvas extends JPanel {
        private static final int COLUMNS = 5; // 16 -> 1
        private static final int[] SPOTS = { 16, 8, 4, 2, 1 };
        private static final int CELL_WIDTH = 180; // tăng chiều ngang ô
        private static final int CELL_HEIGHT = 30;
        // Độ dịch lên cho các vòng trong (cột > 1) để nằm hơi cao hơn so với chính giữa
        private static final int INNER_UP_OFFSET = 20; // px
        // Độ dịch sang phải cơ sở cho mỗi mức sâu hơn (cột > 1). Tổng offset =
        // (col-1)*BASE
        private static final int BASE_INNER_RIGHT_OFFSET = 40; // px mỗi cột
        // Giảm khoảng trống phía trên (trước đây dùng 62)
        private static final int START_Y = 10; // px

        private List<String> participants = new ArrayList<>(); // tên cột 1

        private static class Slot {
            int col; // 1..5
            int thuTu; // 0-based among spots in that column
            int x;
            int y;
            String text;
        }

        private final List<Slot> slots = new ArrayList<>();

        BracketCanvas() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setFont(getFont().deriveFont(Font.PLAIN, 12f));
            rebuildSlots();
        }

        void setParticipants(List<String> names) {
            this.participants = names != null ? names : new ArrayList<>();
            rebuildSlots();
        }

        List<String> getParticipants() {
            return participants;
        }

        List<Slot> getSlots() {
            return slots;
        }

        private void rebuildSlots() {
            slots.clear();
            for (int col = 1; col <= COLUMNS; col++) {
                int spotCount = SPOTS[col - 1];
                int verticalStep = (int) (40 * Math.pow(2, col - 1)); // bước của cột hiện tại
                for (int t = 0; t < spotCount; t++) {
                    int x = 35 + (col - 1) * 160 + (col > 1 ? (col - 1) * BASE_INNER_RIGHT_OFFSET : 0); // dịch phải
                    int baseY = START_Y + t * verticalStep;
                    // Cột 1 giữ nguyên. Cột >1: giữa hai ô con rồi đẩy lên một chút.
                    int y;
                    if (col == 1) {
                        y = baseY;
                    } else {
                        y = baseY + verticalStep / 2 - INNER_UP_OFFSET;
                        if (y < 0)
                            y = 0; // an toàn
                    }
                    Slot s = new Slot();
                    s.col = col;
                    s.thuTu = t;
                    s.x = x;
                    s.y = y;
                    switch (s.col) {
                        case 1 -> s.text = (t < participants.size()) ? participants.get(t) : "Slot " + (t + 1);
                        case COLUMNS -> s.text = "Vô địch";
                        default -> s.text = "";
                    }
                    slots.add(s);
                }
            }
            // Cập nhật preferred size dựa trên xa nhất
            int maxX = 35 + (COLUMNS - 1) * 160 + CELL_WIDTH + 40;
            int lastColSpots = SPOTS[0];
            int maxY = START_Y + (lastColSpots) * (40) + 400; // dư chút để scroll
            setPreferredSize(new Dimension(maxX, maxY));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Vẽ nhãn cột
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            g2.setFont(getFont());
            // Vẽ ô
            for (Slot s : slots) {
                int w = CELL_WIDTH;
                int h = CELL_HEIGHT;
                g2.setColor(new Color(250, 250, 250));
                g2.fillRoundRect(s.x, s.y, w, h, 8, 8);
                g2.setColor(new Color(120, 120, 120));
                g2.drawRoundRect(s.x, s.y, w, h, 8, 8);
                // Vị trí hiển thị (VI_TRI = THU_TU + 1)
                if (s.col == 1 && !participants.isEmpty()) {
                    g2.setColor(new Color(90, 90, 90));
                    g2.drawString(String.valueOf(s.thuTu + 1), s.x - 18, s.y + 18);
                }
                // Text
                if (s.text != null && !s.text.isBlank()) {
                    g2.setColor(Color.BLACK);
                    String txt = truncate(s.text, 28); // cho phép tên dài hơn vì ô rộng hơn
                    g2.drawString(txt, s.x + 8, s.y + 18);
                }
            }

            // Vẽ đường nối giữa các cột (pair -> parent)
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(170, 170, 170));
            for (int col = 1; col < COLUMNS; col++) {
                int spotCount = SPOTS[col - 1];
                for (int t = 0; t < spotCount; t += 2) {
                    Slot a = find(col, t);
                    Slot b = find(col, t + 1);
                    Slot parent = find(col + 1, t / 2);
                    if (a == null || b == null || parent == null)
                        continue;
                    int ax = a.x + CELL_WIDTH;
                    int ay = a.y + CELL_HEIGHT / 2;
                    int bx = b.x + CELL_WIDTH;
                    int by = b.y + CELL_HEIGHT / 2;
                    int px = parent.x;
                    int py = parent.y + CELL_HEIGHT / 2;
                    // ngang phải của a -> mid -> xuống/up -> ngang tới parent
                    int midX = ax + 20;
                    g2.drawLine(ax, ay, midX, ay);
                    g2.drawLine(bx, by, midX, by);
                    // dọc nối hai nhánh
                    g2.drawLine(midX, ay, midX, by);
                    // sang parent
                    g2.drawLine(midX, (ay + by) / 2, px, py);
                }
            }

            g2.dispose();
        }

        private Slot find(int col, int thuTu) {
            for (Slot s : slots) {
                if (s.col == col && s.thuTu == thuTu)
                    return s;
            }
            return null;
        }

        private String truncate(String s, int max) {
            if (s == null)
                return "";
            return s.length() <= max ? s : s.substring(0, max - 1) + "…";
        }
    }
}
