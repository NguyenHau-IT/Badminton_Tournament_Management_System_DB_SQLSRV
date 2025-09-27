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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.example.btms.config.Prefs;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.draw.BocThamDoi;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.bracket.SoDoDoiRepository;
import com.example.btms.repository.draw.BocThamDoiRepository;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.bracket.SoDoDoiService;
import com.example.btms.service.draw.BocThamDoiService;
import com.example.btms.service.team.DoiService;

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
    private final JButton btnSave = new JButton("Lưu");

    // New: load from DB (BocThamDoi)
    private final JComboBox<NoiDung> cboNoiDung = new JComboBox<>();
    private final JButton btnLoadDb = new JButton("Tải từ DB");
    private final JLabel lblGiai = new JLabel();

    // Services
    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final BocThamDoiService bocThamService;
    private final SoDoDoiService soDoDoiService;
    private final DoiService doiService;

    public SoDoThiDauPanel(Connection conn) { // giữ signature cũ để MainFrame không phải đổi nhiều
        Objects.requireNonNull(conn, "Connection null");
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.bocThamService = new BocThamDoiService((Connection) conn, new BocThamDoiRepository((Connection) conn));
        this.soDoDoiService = new SoDoDoiService(new SoDoDoiRepository((Connection) conn));
        this.doiService = new DoiService(conn);

        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        SwingUtilities.invokeLater(() -> {
            loadNoiDungOptions();
            updateGiaiLabel();
            loadFromDb();
        });
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(new JLabel("Sơ đồ 16 đội"));
        line.add(lblGiai);
        line.add(new JLabel(" | Nội dung:"));
        cboNoiDung.setPrototypeDisplayValue(new NoiDung(0, "XXXXXXXXXXXXXXXXXXXXX", null, null, null, true));
        line.add(cboNoiDung);
        line.add(btnLoadDb);
        line.add(btnSave);
        p.add(line, BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveBracket());
        btnLoadDb.addActionListener(e -> loadFromDb());
        cboNoiDung.addActionListener(e -> loadFromDb());
        return p;
    }

    private void saveBracket() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        if (idGiai <= 0 || nd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idNoiDung = nd.getId();
        try {
            // Xóa dữ liệu cũ của (Giải, Nội dung)
            for (var row : soDoDoiService.list(idGiai, idNoiDung)) {
                soDoDoiService.delete(idGiai, idNoiDung, row.getViTri());
            }
            // Lưu các ô đang hiển thị (chỉ lưu ô có tên)
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (BracketCanvas.Slot s : canvas.getSlots()) {
                if (s.text != null && !s.text.isBlank()) {
                    int idClb = doiService.getIdClbByTeamName(s.text.trim(), idNoiDung, idGiai);
                    int soDo = bocThamService.getSoDo(idGiai, idNoiDung, idClb);
                    System.out.println("DEBUG: Lưu sơ đồ: " + idGiai + "," + idNoiDung + "," + idClb + ","
                            + s.text.trim() + "," + s.x + "," + s.y + "," + s.order + "," + soDo);
                    soDoDoiService.create(
                            idGiai,
                            idNoiDung,
                            idClb, // ID_CLB đã xác định
                            s.text.trim(),
                            s.x,
                            s.y,
                            s.order, // VI_TRI là số thứ tự liên tục của slot
                            soDo, // SO_DO: lưu cột để tham chiếu vòng/nhánh
                            now);
                }
            }
            JOptionPane.showMessageDialog(this, "Đã lưu sơ đồ vào CSDL", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu sơ đồ: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // xuất ra file đã loại bỏ; hiện tại nút Lưu sẽ lưu trực tiếp vào CSDL

    // Removed: resetDefaultTeams and shuffleTeams — no longer used after UI change

    /* ===================== DB wiring ===================== */
    private void loadNoiDungOptions() {
        try {
            List<NoiDung> all = noiDungService.getAllNoiDung();
            java.util.List<NoiDung> doublesOnly = new java.util.ArrayList<>();
            for (NoiDung nd : all) {
                if (Boolean.TRUE.equals(nd.getTeam()))
                    doublesOnly.add(nd);
            }
            cboNoiDung.removeAllItems();
            for (NoiDung nd : doublesOnly)
                cboNoiDung.addItem(nd);
            if (cboNoiDung.getItemCount() > 0)
                cboNoiDung.setSelectedIndex(0);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nội dung: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGiaiLabel() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        String ten = prefs.get("selectedGiaiDauName", null);
        if (idGiai > 0) {
            lblGiai.setText("Giải: " + (ten != null && !ten.isBlank() ? ten : ("ID=" + idGiai)));
        } else {
            lblGiai.setText("Giải: (chưa chọn)");
        }
    }

    private void loadFromDb() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = (NoiDung) cboNoiDung.getSelectedItem();
        if (idGiai <= 0 || nd == null) {
            return;
        }
        List<BocThamDoi> list;
        try {
            list = bocThamService.list(idGiai, nd.getId());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ từ DB: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Decide seeding column and block size based on number of participants
        int N = list.size();
        int M; // block size in that column
        int seedCol; // 1..5
        if (N >= 9) {
            M = 16;
            seedCol = 1;
        } else if (N >= 5) {
            M = 8;
            seedCol = 2;
        } else if (N >= 3) {
            M = 4;
            seedCol = 3;
        } else if (N >= 2) {
            M = 2;
            seedCol = 4;
        } else {
            M = 1;
            seedCol = 4;
        } // 1 người: không có trận; hiển thị ở nhánh gần cuối

        if (N > 16) {
            JOptionPane.showMessageDialog(this, "Số đội vượt quá 16 – chưa hỗ trợ trong sơ đồ 16.", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            N = 16;
            M = 16;
            seedCol = 1;
        }

        List<Integer> pos = computeTopHeavyPositionsWithinBlock(Math.min(N, M), M);
        List<String> namesByT = new ArrayList<>();
        for (int t = 0; t < M; t++)
            namesByT.add(null);
        for (int i = 0; i < Math.min(N, M); i++) {
            int t = pos.get(i);
            namesByT.set(t, list.get(i).getTenTeam());
        }
        // BYE auto-advance for first round: scan all pairs in the seeded column.
        // For each pair that has exactly one participant, clear both leaves and
        // place the winner into the next column at the corresponding parent index.
        canvas.clearTextOverrides();
        if (M >= 2) {
            int pairCount = M / 2;
            for (int p = 0; p < pairCount; p++) {
                String a = namesByT.get(2 * p);
                String b = namesByT.get(2 * p + 1);
                boolean hasA = a != null && !a.isBlank();
                boolean hasB = b != null && !b.isBlank();
                if (hasA ^ hasB) { // exactly one participant present -> BYE
                    String winner = hasA ? a : b;
                    // Clear outer leaves
                    namesByT.set(2 * p, null);
                    namesByT.set(2 * p + 1, null);
                    // Place winner into inner column slot matching this pair index
                    if (seedCol < 5) {
                        canvas.setTextOverride(seedCol + 1, p, winner);
                    }
                }
            }
        }
        canvas.setParticipantsForColumn(namesByT, seedCol);
        canvas.repaint();
    }

    // Top-heavy fill: always put ceil(n/2) to top half first, then bottom half
    private static List<Integer> computeTopHeavyPositionsWithinBlock(int N, int M) {
        List<Integer> out = new ArrayList<>(N);
        fillTopHeavy(0, M, N, out);
        return out;
    }

    private static void fillTopHeavy(int start, int block, int n, List<Integer> out) {
        if (n <= 0)
            return;
        if (block == 1) {
            out.add(start);
            return;
        }
        int half = block / 2;
        int nTop = (n + 1) / 2; // ceil
        int nBot = n - nTop; // floor
        fillTopHeavy(start, half, nTop, out);
        fillTopHeavy(start + half, half, nBot, out);
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

        private List<String> participants = new ArrayList<>(); // tên tại cột seedColumn
        private int seedColumn = 1; // cột sẽ hiển thị danh sách ban đầu (1..5)
        private final java.util.Map<Integer, String> textOverrides = new java.util.HashMap<>();

        private static class Slot {
            int col; // 1..5
            int thuTu; // 0-based among spots in that column
            int x;
            int y;
            String text;
            int order; // 1-based continuous numbering across columns left->right, top->bottom
        }

        private final List<Slot> slots = new ArrayList<>();

        BracketCanvas() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setFont(getFont().deriveFont(Font.PLAIN, 12f));
            rebuildSlots();
        }

        void setParticipantsForColumn(List<String> names, int seedCol) {
            this.participants = names != null ? names : new ArrayList<>();
            if (seedCol < 1 || seedCol > COLUMNS)
                seedCol = 1;
            this.seedColumn = seedCol;
            rebuildSlots();
        }

        void clearTextOverrides() {
            textOverrides.clear();
        }

        void setTextOverride(int col, int thuTu, String text) {
            int key = (col << 16) | (thuTu & 0xFFFF);
            if (text == null || text.isBlank()) {
                textOverrides.remove(key);
            } else {
                textOverrides.put(key, text);
            }
        }

        List<Slot> getSlots() {
            return slots;
        }

        private void rebuildSlots() {
            slots.clear();
            int orderCounter = 1; // continuous numbering
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
                    s.order = orderCounter++;
                    if (s.col == seedColumn) {
                        s.text = (t < participants.size() && participants.get(t) != null) ? participants.get(t) : "";
                    } else if (s.col == COLUMNS) {
                        s.text = "";
                    } else {
                        s.text = "";
                    }
                    // apply text override if any
                    String ovr = textOverrides.get((s.col << 16) | (s.thuTu & 0xFFFF));
                    if (ovr != null) {
                        s.text = ovr;
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
                // Số thứ tự liên tục, ghi trong ô (bên trái, màu xám)
                g2.setColor(new Color(90, 90, 90));
                g2.drawString(String.valueOf(s.order), s.x + 8, s.y + 18);
                // Text
                if (s.text != null && !s.text.isBlank()) {
                    g2.setColor(Color.BLACK);
                    String txt = truncate(s.text, 28); // cho phép tên dài hơn vì ô rộng hơn
                    // đẩy text sang phải một chút để không chồng lên số thứ tự
                    g2.drawString(txt, s.x + 26, s.y + 18);
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
