package com.example.btms.ui.bracket;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.example.btms.config.Prefs;
import com.example.btms.model.bracket.SoDoDoi;
import com.example.btms.model.category.NoiDung;
import com.example.btms.model.draw.BocThamDoi;
import com.example.btms.model.result.KetQuaDoi;
import com.example.btms.repository.bracket.SoDoCaNhanRepository;
import com.example.btms.repository.bracket.SoDoDoiRepository;
import com.example.btms.repository.category.NoiDungRepository;
import com.example.btms.repository.club.CauLacBoRepository;
import com.example.btms.repository.draw.BocThamCaNhanRepository;
import com.example.btms.repository.draw.BocThamDoiRepository;
import com.example.btms.repository.result.KetQuaDoiRepository;
import com.example.btms.service.bracket.SoDoCaNhanService;
import com.example.btms.service.bracket.SoDoDoiService;
import com.example.btms.service.category.NoiDungService;
import com.example.btms.service.club.CauLacBoService;
import com.example.btms.service.draw.BocThamCaNhanService;
import com.example.btms.service.draw.BocThamDoiService;
import com.example.btms.service.result.KetQuaDoiService;
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
    private final JButton btnSeedFromDraw = new JButton("Gán theo bốc thăm");
    private final JButton btnReloadSaved = new JButton("Tải sơ đồ đã lưu");
    private final JButton btnClearAll = new JButton("Xóa tất cả ô");
    private final JToggleButton btnEdit = new JToggleButton("Chế độ sửa");
    private final JButton btnSaveResults = new JButton("Lưu kết quả");
    // Medals table (EAST)
    private final JTable medalTable = new JTable();
    private final DefaultTableModel medalModel = new DefaultTableModel(new Object[] { "Kết quả" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    // Track last saved medals to avoid redundant writes during auto-save
    private String lastSavedMedalKey = null;

    // Nội dung được chọn (không dùng combobox nữa)
    private final java.util.List<NoiDung> noiDungList = new java.util.ArrayList<>();
    private NoiDung selectedNoiDung = null;
    private final JLabel lblGiai = new JLabel();
    // Remember a pending selection when combo items haven't loaded yet
    private Integer pendingSelectNoiDungId = null;
    private final JLabel lblNoiDungValue = new JLabel(); // hiển thị tên nội dung khi dùng chế độ label

    // Services
    private final Prefs prefs = new Prefs();
    private final NoiDungService noiDungService;
    private final BocThamDoiService bocThamService;
    private final BocThamCaNhanService bocThamCaNhanService;
    private final SoDoCaNhanService soDoCaNhanService;
    private final SoDoDoiService soDoDoiService;
    private final DoiService doiService;
    private final CauLacBoService clbService;
    private final KetQuaDoiService ketQuaDoiService;

    public SoDoThiDauPanel(Connection conn) { // giữ signature cũ để MainFrame không phải đổi nhiều
        Objects.requireNonNull(conn, "Connection null");
        this.noiDungService = new NoiDungService(new NoiDungRepository(conn));
        this.bocThamService = new BocThamDoiService((Connection) conn, new BocThamDoiRepository((Connection) conn));
        this.soDoDoiService = new SoDoDoiService(new SoDoDoiRepository((Connection) conn));
        this.doiService = new DoiService(conn);
        this.clbService = new CauLacBoService(new CauLacBoRepository((Connection) conn));
        this.ketQuaDoiService = new KetQuaDoiService(new KetQuaDoiRepository((Connection) conn));
        this.bocThamCaNhanService = new BocThamCaNhanService(new BocThamCaNhanRepository((Connection) conn));
        this.soDoCaNhanService = new SoDoCaNhanService(new SoDoCaNhanRepository((Connection) conn));

        setLayout(new BorderLayout(8, 8));
        add(buildTop(), BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        SwingUtilities.invokeLater(() -> {
            loadNoiDungOptions();
            updateGiaiLabel();
            loadBestAvailable();
        });
    }

    /**
     * Programmatically select a specific Nội dung by its id in the combo box.
     * If found, it will trigger the existing action listener to reload the bracket.
     */
    public void selectNoiDungById(Integer id) {
        if (id == null)
            return;
        for (NoiDung it : noiDungList) {
            if (it != null && it.getId() != null && it.getId().equals(id)) {
                selectedNoiDung = it;
                updateNoiDungLabelText();
                loadBestAvailable();
                return;
            }
        }
        // Not found now; remember for when options are (re)loaded
        pendingSelectNoiDungId = id;
    }

    /**
     * Bật/tắt chế độ chỉ-hiển-thị bằng label cho trường Nội dung.
     * Khi bật, ẩn combobox và hiện label với tên nội dung đang chọn.
     */
    public void setNoiDungLabelMode(boolean on) {
        lblNoiDungValue.setVisible(true);
        updateNoiDungLabelText();
        revalidate();
        repaint();
    }

    /**
     * Public method for parent containers to force data reload (e.g., when tab is
     * selected).
     */
    public void reloadData() {
        try {
            updateGiaiLabel();
            loadBestAvailable();
        } catch (Exception ignore) {
        }
    }

    private void updateNoiDungLabelText() {
        NoiDung nd = selectedNoiDung;
        String name = (nd != null && nd.getTenNoiDung() != null) ? nd.getTenNoiDung().trim() : "";
        lblNoiDungValue.setText(name);
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        line.add(lblGiai);
        line.add(new JLabel(" | Nội dung:"));
        // Luôn dùng label để hiển thị tên nội dung; không hiển thị combobox
        lblNoiDungValue.setVisible(true);
        line.add(lblNoiDungValue);
        line.add(btnReloadSaved);
        line.add(btnSeedFromDraw);
        line.add(btnClearAll);
        line.add(btnEdit);
        line.add(btnSaveResults);
        line.add(btnSave);
        p.add(line, BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveBracket());
        btnSaveResults.addActionListener(e -> saveMedalResults());
        btnReloadSaved.addActionListener(e -> loadSavedSoDo());
        btnSeedFromDraw.addActionListener(e -> loadFromBocTham());
        btnClearAll.addActionListener(e -> clearAllSlots());
        btnEdit.addActionListener(e -> canvas.setEditMode(btnEdit.isSelected()));
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Kết quả"));
        medalTable.setModel(medalModel);
        medalTable.setTableHeader(null); // ẩn header để đúng 1 cột đơn giản
        medalTable.setRowHeight(26);
        medalTable.setShowGrid(false);
        JScrollPane sp = new JScrollPane(medalTable);
        sp.setPreferredSize(new Dimension(240, 140));
        p.add(sp, BorderLayout.CENTER);
        // Khởi tạo 4 dòng rỗng
        refreshMedalTable("", "", "", "");
        return p;
    }

    private void saveBracket() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idNoiDung = nd.getId();
        try {
            // Xóa toàn bộ sơ đồ cũ cho đúng trạng thái hiện tại (idGiai, idNoiDung)
            List<SoDoDoi> olds = soDoDoiService.list(idGiai, idNoiDung);
            for (SoDoDoi r : olds) {
                try {
                    soDoDoiService.delete(idGiai, idNoiDung, r.getViTri());
                } catch (Exception ignore) {
                }
            }
            // Lưu các ô đang hiển thị (chỉ lưu ô có tên)
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            for (BracketCanvas.Slot s : canvas.getSlots()) {
                if (s.text != null && !s.text.isBlank()) {
                    Integer idClb = null;
                    try {
                        idClb = doiService.getIdClbByTeamName(s.text.trim(), idNoiDung, idGiai);
                    } catch (RuntimeException ignored) {
                    }
                    Integer soDo = null;
                    try {
                        if (idClb != null)
                            soDo = bocThamService.getSoDo(idGiai, idNoiDung, idClb);
                    } catch (RuntimeException ignored) {
                        // fallback: dùng số cột làm soDo nếu cần
                        soDo = s.col;
                    }
                    soDoDoiService.create(
                            idGiai,
                            idNoiDung,
                            idClb, // có thể null nếu không xác định được
                            s.text.trim(),
                            s.x,
                            s.y,
                            s.order, // VI_TRI là số thứ tự liên tục của slot
                            soDo, // SO_DO: lưu vòng/nhánh nếu có
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
            Integer idGiai = prefs.getInt("selectedGiaiDauId", -1);
            List<NoiDung> all = noiDungService.getNoiDungByTuornament(idGiai);
            noiDungList.clear();
            if (all != null)
                noiDungList.addAll(all);
            if (!noiDungList.isEmpty())
                selectedNoiDung = noiDungList.get(0);
            if (pendingSelectNoiDungId != null) {
                for (NoiDung it : noiDungList) {
                    if (it != null && it.getId() != null && it.getId().equals(pendingSelectNoiDungId)) {
                        selectedNoiDung = it;
                        break;
                    }
                }
                pendingSelectNoiDungId = null;
            }
            updateNoiDungLabelText();
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

    private void loadFromBocTham() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
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
            BocThamDoi row = list.get(i);
            String team = row.getTenTeam() != null ? row.getTenTeam().trim() : "";
            String club = "";
            try {
                Integer idClb = row.getIdClb();
                if (idClb == null && team != null && !team.isBlank()) {
                    // Fallback: try resolve club by team name for this tournament/category
                    idClb = doiService.getIdClbByTeamName(team, nd.getId(), idGiai);
                }
                if (idClb != null) {
                    var c = clbService.findOne(idClb);
                    if (c != null && c.getTenClb() != null)
                        club = c.getTenClb().trim();
                }
            } catch (RuntimeException ignore) {
            }
            String display = club.isBlank() ? team : (team + " - " + club);
            namesByT.set(t, display);
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
        updateMedalsFromCanvas();
    }

    private boolean loadSavedSoDo() {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null)
            return false;
        List<SoDoDoi> list;
        try {
            list = soDoDoiService.list(idGiai, nd.getId());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sơ đồ đã lưu: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (list == null || list.isEmpty())
            return false;
        // Reset về trắng và gán theo VI_TRI -> Slot.order
        List<String> blanks = new ArrayList<>();
        for (int i = 0; i < 16; i++)
            blanks.add("");
        canvas.setParticipantsForColumn(blanks, 1);
        canvas.clearTextOverrides();
        for (SoDoDoi r : list) {
            BracketCanvas.Slot slot = canvas.findByOrder(r.getViTri());
            if (slot != null) {
                canvas.setTextOverride(slot.col, slot.thuTu, r.getTenTeam());
            }
        }
        canvas.repaint();
        updateMedalsFromCanvas();
        return true;
    }

    private void loadBestAvailable() {
        if (!loadSavedSoDo()) {
            loadFromBocTham();
        }
        updateMedalsFromCanvas();
    }

    private void clearAllSlots() {
        // Giữ cấu trúc, xóa text
        List<String> blanks = new ArrayList<>();
        for (int i = 0; i < 16; i++)
            blanks.add("");
        canvas.setParticipantsForColumn(blanks, 1);
        canvas.clearTextOverrides();
        canvas.repaint();
        updateMedalsFromCanvas();
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

    // ===== Medal logic =====
    private void updateMedalsFromCanvas() {
        // Gold: cột 5, ô duy nhất
        String gold = getTextAt(5, 0);
        // Silver: cột 4, ô có parent là gold; chọn ô nào có text khác gold
        // Thực tế, ở vòng chung kết (cột 4, có 2 ô), silver là đội còn lại không phải
        // gold
        String semiA = getTextAt(4, 0);
        String semiB = getTextAt(4, 1);
        String silver = null;
        if (gold != null && !gold.isBlank()) {
            if (semiA != null && semiA.equals(gold))
                silver = semiB;
            else if (semiB != null && semiB.equals(gold))
                silver = semiA;
        }
        // Bronzes: 2 đội thua ở bán kết: cột 3 có 4 ô, nhưng bán kết là cột 4 đầu vào
        // từ cột 3.
        // Ở đây cấu trúc đã mapping: cột 3 => 4 chỗ, cột 4 => 2 chỗ.
        // Lấy 2 đội vào chung kết (semiA, semiB), mỗi đội có parent từ cột 3:
        // cột 3 chỉ chứa text nếu người dùng đã điền/seed; nếu rỗng dùng textOverrides
        // hiện có.
        // Ta tìm 2 cặp bán kết: (col3 thuTu 0,1)->semiA; (col3 thuTu 2,3)->semiB
        String semiA1 = getTextAt(3, 0);
        String semiA2 = getTextAt(3, 1);
        String semiB1 = getTextAt(3, 2);
        String semiB2 = getTextAt(3, 3);
        // Người thắng mỗi bán kết là semiA và semiB; bronze là người còn lại trong mỗi
        // cặp
        String bronze1 = null;
        String bronze2 = null;
        if (semiA != null && !semiA.isBlank()) {
            if (semiA.equals(semiA1))
                bronze1 = semiA2;
            else if (semiA.equals(semiA2))
                bronze1 = semiA1;
        }
        if (semiB != null && !semiB.isBlank()) {
            if (semiB.equals(semiB1))
                bronze2 = semiB2;
            else if (semiB.equals(semiB2))
                bronze2 = semiB1;
        }
        String g = safe(gold).trim();
        String s = safe(silver).trim();
        String b1 = safe(bronze1).trim();
        String b2 = safe(bronze2).trim();
        refreshMedalTable(g, s, b1, b2);

        // Auto-save medals when a champion exists and the snapshot changed
        String snapshotKey = String.join("|", g, s, b1, b2);
        if (!g.isBlank() && !snapshotKey.equals(lastSavedMedalKey)) {
            lastSavedMedalKey = snapshotKey;
            persistMedals(true); // silent
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String getTextAt(int col, int thuTu) {
        // Truy vấn slot hiện tại từ canvas
        for (var s : canvas.getSlots()) {
            if (s.col == col && s.thuTu == thuTu)
                return s.text;
        }
        return null;
    }

    private void refreshMedalTable(String gold, String silver, String bronze1, String bronze2) {
        medalModel.setRowCount(0);
        medalModel.addRow(new Object[] { "Vàng: " + gold });
        medalModel.addRow(new Object[] { "Bạc: " + silver });
        medalModel.addRow(new Object[] { "Đồng: " + bronze1 });
        medalModel.addRow(new Object[] { "Đồng: " + bronze2 });
    }

    // ===== Persist medals =====
    private void saveMedalResults() {
        persistMedals(false);
    }

    // Core implementation for persisting medals. When silent=true, suppress
    // dialogs.
    private void persistMedals(boolean silent) {
        int idGiai = prefs.getInt("selectedGiaiDauId", -1);
        NoiDung nd = selectedNoiDung;
        if (idGiai <= 0 || nd == null) {
            if (!silent) {
                JOptionPane.showMessageDialog(this, "Chưa chọn giải hoặc nội dung", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        int idNoiDung = nd.getId();
        try {
            String vang = extractNameFromMedalRow(0);
            String bac = extractNameFromMedalRow(1);
            String dong1 = extractNameFromMedalRow(2);
            String dong2 = extractNameFromMedalRow(3);

            List<KetQuaDoi> items = new ArrayList<>();
            if (!isBlank(vang))
                items.add(buildKetQua(idGiai, idNoiDung, vang, 1));
            if (!isBlank(bac))
                items.add(buildKetQua(idGiai, idNoiDung, bac, 2));
            if (!isBlank(dong1))
                items.add(buildKetQua(idGiai, idNoiDung, dong1, 3));
            if (!isBlank(dong2))
                items.add(buildKetQua(idGiai, idNoiDung, dong2, 3));

            ketQuaDoiService.replaceMedals(idGiai, idNoiDung, items);
            if (!silent) {
                JOptionPane.showMessageDialog(this, "Đã lưu kết quả huy chương", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RuntimeException ex) {
            if (!silent) {
                JOptionPane.showMessageDialog(this, "Lỗi lưu kết quả: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                // best-effort silent mode: mark snapshot as unsaved so a next change can retry
                lastSavedMedalKey = null;
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String extractNameFromMedalRow(int row) {
        if (row < 0 || row >= medalModel.getRowCount())
            return "";
        Object v = medalModel.getValueAt(row, 0);
        if (v == null)
            return "";
        String s = v.toString();
        int idx = s.indexOf(":");
        if (idx >= 0 && idx + 1 < s.length())
            return s.substring(idx + 1).trim();
        return s.trim();
    }

    private KetQuaDoi buildKetQua(int idGiai, int idNoiDung, String teamDisplay, int thuHang) {
        String teamName = teamDisplay;
        int sep = teamDisplay.indexOf(" - ");
        if (sep >= 0)
            teamName = teamDisplay.substring(0, sep).trim();
        Integer idClb = null;
        try {
            idClb = doiService.getIdClbByTeamName(teamName, idNoiDung, idGiai);
        } catch (RuntimeException ignore) {
        }
        if (idClb == null || idClb <= 0) {
            throw new IllegalStateException("Không xác định được CLB cho đội: " + teamDisplay);
        }
        return new KetQuaDoi(idGiai, idNoiDung, idClb, teamDisplay, thuHang);
    }

    /* ===================== Canvas ===================== */
    private class BracketCanvas extends JPanel {
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
        private boolean editMode = false;

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
            // simple edit: double-click a slot to edit text when edit mode is on
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!editMode)
                        return;
                    if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                        Slot s = getSlotAt(e.getPoint());
                        if (s != null) {
                            String newText = JOptionPane.showInputDialog(BracketCanvas.this, "Sửa tên đội:", s.text);
                            if (newText != null) {
                                setTextOverride(s.col, s.thuTu, newText.trim());
                                // cập nhật ngay cả trong cache slots
                                s.text = newText.trim();
                                repaint();
                                SoDoThiDauPanel.this.updateMedalsFromCanvas();
                            }
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowContextMenu(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowContextMenu(e);
                }

                private void maybeShowContextMenu(MouseEvent e) {
                    if (!editMode)
                        return;
                    if (!e.isPopupTrigger())
                        return;
                    Slot s = getSlotAt(e.getPoint());
                    if (s == null)
                        return;
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem mAdvance = new JMenuItem("Vẽ bản ghi");
                    JMenuItem mBack = new JMenuItem("Xoá bản ghi");
                    mAdvance.setEnabled(s.col < COLUMNS && s.text != null && !s.text.isBlank());
                    mBack.setEnabled(s.col > 1 && s.text != null && !s.text.isBlank());
                    mAdvance.addActionListener(ev -> {
                        Slot parent = parentOf(s);
                        if (parent != null) {
                            setTextOverride(parent.col, parent.thuTu, s.text);
                            parent.text = s.text;
                            repaint();
                            SoDoThiDauPanel.this.updateMedalsFromCanvas();
                            // lưu vào db theo trạng thái hiện tại của panel
                            int idGiai = SoDoThiDauPanel.this.prefs.getInt("selectedGiaiDauId", -1);
                            NoiDung ndSel = SoDoThiDauPanel.this.selectedNoiDung;
                            if (idGiai > 0 && ndSel != null) {
                                int idNoiDung = ndSel.getId();
                                Integer idClb = null;
                                try {
                                    idClb = SoDoThiDauPanel.this.doiService.getIdClbByTeamName(s.text.trim(), idNoiDung,
                                            idGiai);
                                } catch (RuntimeException ignored) {
                                }
                                Integer soDo = null;
                                try {
                                    if (idClb != null)
                                        soDo = SoDoThiDauPanel.this.bocThamService.getSoDo(idGiai, idNoiDung, idClb);
                                } catch (RuntimeException ignored) {
                                    // fallback: dùng số cột làm soDo nếu cần
                                    soDo = parent.col;
                                }
                                SoDoThiDauPanel.this.soDoDoiService.create(
                                        idGiai,
                                        idNoiDung,
                                        idClb,
                                        s.text.trim(),
                                        parent.x,
                                        parent.y,
                                        parent.order,
                                        soDo,
                                        java.time.LocalDateTime.now());
                            }
                        }
                    });
                    mBack.addActionListener(ev -> {
                        Slot[] ch = childrenOf(s);
                        if (ch == null)
                            return;
                        Slot left = ch[0];
                        Slot right = ch[1];
                        // chọn vị trí con để lùi: ưu tiên ô trống, nếu cả hai trống chọn trái
                        Slot target = null;
                        boolean leftEmpty = (left != null) && (left.text == null || left.text.isBlank());
                        boolean rightEmpty = (right != null) && (right.text == null || right.text.isBlank());
                        if (leftEmpty && rightEmpty)
                            target = left;
                        else if (leftEmpty)
                            target = left;
                        else if (rightEmpty)
                            target = right;
                        else
                            target = left; // nếu đều có dữ liệu, ghi đè trái
                        if (target != null) {
                            setTextOverride(target.col, target.thuTu, s.text);
                            target.text = s.text;
                            // xóa ở ô hiện tại
                            setTextOverride(s.col, s.thuTu, "");
                            s.text = "";
                            // Xoá bản ghi trong DB cho ô hiện tại (theo VI_TRI = s.order)
                            int idGiai = SoDoThiDauPanel.this.prefs.getInt("selectedGiaiDauId", -1);
                            NoiDung ndSel = SoDoThiDauPanel.this.selectedNoiDung;
                            if (idGiai > 0 && ndSel != null) {
                                try {
                                    SoDoThiDauPanel.this.soDoDoiService.delete(idGiai, ndSel.getId(), s.order);
                                } catch (RuntimeException ignore) {
                                }
                            }
                            repaint();
                            SoDoThiDauPanel.this.updateMedalsFromCanvas();
                        }
                    });
                    menu.add(mAdvance);
                    menu.add(mBack);
                    menu.show(BracketCanvas.this, e.getX(), e.getY());
                }
            };
            addMouseListener(ma);
        }

        void setEditMode(boolean on) {
            this.editMode = on;
            setCursor(on ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
                    : java.awt.Cursor.getDefaultCursor());
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

        Slot findByOrder(int order) {
            for (Slot s : slots) {
                if (s.order == order)
                    return s;
            }
            return null;
        }

        private Slot getSlotAt(Point p) {
            for (Slot s : slots) {
                int w = CELL_WIDTH, h = CELL_HEIGHT;
                if (p.x >= s.x && p.x <= s.x + w && p.y >= s.y && p.y <= s.y + h)
                    return s;
            }
            return null;
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

        private Slot parentOf(Slot s) {
            if (s == null)
                return null;
            if (s.col >= COLUMNS)
                return null;
            return find(s.col + 1, s.thuTu / 2);
        }

        private Slot[] childrenOf(Slot s) {
            if (s == null)
                return null;
            if (s.col <= 1)
                return null;
            Slot left = find(s.col - 1, s.thuTu * 2);
            Slot right = find(s.col - 1, s.thuTu * 2 + 1);
            return new Slot[] { left, right };
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
                // Text (ẩn số thứ tự slot để gọn gàng)
                if (s.text != null && !s.text.isBlank()) {
                    g2.setColor(Color.BLACK);
                    String txt = truncate(s.text, 28); // cho phép tên dài hơn vì ô rộng hơn
                    // Căn trái vừa phải trong ô
                    g2.drawString(txt, s.x + 10, s.y + 18);
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
